package com.zyd.ecmall;

import com.zyd.ecmall.config.ProductImageConfiguration;
import com.zyd.ecmall.config.ProductImageProperties;
import com.zyd.ecmall.service.ProductImageService;
import com.zyd.ecmall.service.ProductImageStorage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductImageTest {
    private final ProductImageStorage storage = mock(ProductImageStorage.class);
    private final ProductImageService images = new ProductImageService(storage);

    private byte[] image(String format, int width) throws Exception {
        var output = new ByteArrayOutputStream();
        var image = new BufferedImage(width, 2, BufferedImage.TYPE_INT_RGB);
        assertTrue(ImageIO.write(image, format, output));
        image.flush();
        return output.toByteArray();
    }

    @Test
    void jpegPngAndWebpAreDecodedAndUseUniqueKeysNotClientFilenames() throws Exception {
        byte[][] contents = { image("jpeg", 2), image("png", 2), Base64.getDecoder().decode(
                "UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEADsD+JaQAA3AAAAAA") };
        String[] types = { "image/jpeg", "image/png", "image/webp" };
        String[] extensions = { "jpg", "png", "webp" };
        when(storage.store(anyString(), anyString(), any())).thenAnswer(call -> "https://images.example.com/" + call.getArgument(0));
        for (int i = 0; i < contents.length; i++) {
            var file = new MockMultipartFile("file", "../../untrusted.svg", types[i], contents[i]);
            var result = images.upload(file);
            assertTrue(result.key().matches("products/[0-9a-f-]{36}\\." + extensions[i]));
            assertEquals("https://images.example.com/" + result.key(), result.imageUrl());
            assertNotEquals(result.key(), images.upload(file).key());
        }
    }

    @Test
    void rejectsEmptyFakeUnsupportedMismatchedAndTruncatedImagesWithoutS3Calls() throws Exception {
        MultipartFile[] invalid = {
                new MockMultipartFile("file", new byte[0]),
                new MockMultipartFile("file", "fake.png", "image/png", "<svg></svg>".getBytes()),
                new MockMultipartFile("file", "file.gif", "image/gif", image("gif", 2)),
                new MockMultipartFile("file", "file.jpg", "image/jpeg", image("png", 2)),
                new MockMultipartFile("file", "broken.png", "image/png", Arrays.copyOf(image("png", 2), 24))
        };
        for (var file : invalid) {
            assertEquals(400, assertThrows(ResponseStatusException.class, () -> images.upload(file)).getStatusCode().value());
        }
        verifyNoInteractions(storage);
    }

    @Test
    void rejectsOversizedFilesAndDimensionsBeforeStorage() throws Exception {
        var large = new MockMultipartFile("file", "large.png", "image/png", new byte[ProductImageService.MAX_BYTES + 1]);
        assertEquals(413, assertThrows(ResponseStatusException.class, () -> images.upload(large)).getStatusCode().value());
        var wide = new MockMultipartFile("file", "wide.png", "image/png", image("png", 8193));
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> images.upload(wide)).getStatusCode().value());
        verifyNoInteractions(storage);
    }

    @Test
    void oversizedPixelCountIsRejectedBeforeDecoding() throws Exception {
        byte[] bytes = image("png", 2);
        java.nio.ByteBuffer.wrap(bytes).putInt(16, 4096).putInt(20, 4096);
        var crc = new java.util.zip.CRC32();
        crc.update(bytes, 12, 17);
        java.nio.ByteBuffer.wrap(bytes).putInt(29, (int) crc.getValue());
        var file = new MockMultipartFile("file", "large.png", "image/png", bytes);
        var error = assertThrows(ResponseStatusException.class, () -> images.upload(file));
        assertTrue(error.getReason().contains("1600万画素"));
        verifyNoInteractions(storage);
    }

    @Test
    void enabledConfigurationCanCreateClientWithoutContactingAws() {
        new ApplicationContextRunner().withUserConfiguration(ProductImageConfiguration.class)
                .withPropertyValues("app.product-images.enabled=true", "app.product-images.bucket=images-test",
                        "app.product-images.frontend-bucket=frontend-test", "app.product-images.region=us-east-1",
                        "app.product-images.public-base-url=https://images.example.com")
                .run(context -> { assertNull(context.getStartupFailure()); assertEquals(1, context.getBeansOfType(S3Client.class).size()); });
    }

    private ProductImageProperties settings(boolean enabled) {
        return new ProductImageProperties(enabled, "product-images-test", "us-east-1", "https://images.example.com/", "frontend-test");
    }

    @Test
    void disabledUploadDoesNotBuildAnAwsClientOrResolveCredentials() {
        new ApplicationContextRunner().withUserConfiguration(ProductImageConfiguration.class).run(context -> {
            assertFalse(context.getBean(ProductImageProperties.class).enabled());
            assertTrue(context.getBeansOfType(S3Client.class).isEmpty());
        });
        var provider = mock(ObjectProvider.class);
        var service = new ProductImageStorage(settings(false), provider);
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> service.store("products/a.png", "image/png", new byte[1])).getStatusCode().value());
        verifyNoInteractions(provider);
    }

    @Test
    void rejectsFrontendBucketAndInvalidPublicUrlConfiguration() {
        assertEquals("https://images.example.com", settings(true).checkedBaseUrl());
        assertThrows(IllegalStateException.class, () -> new ProductImageProperties(true, "same-bucket", "us-east-1", "https://images.example.com", "same-bucket").checkedBaseUrl());
        for (String url : new String[] { "http://example.com", "https://user:password@example.com", "https://example.com/path", "https://example.com?token=secret", "https://example.com/#fragment", "https://" + "a".repeat(220) + ".com" }) {
            assertThrows(IllegalStateException.class, () -> new ProductImageProperties(true, "images-test", "us-east-1", url, "frontend-test").checkedBaseUrl());
        }
    }

    @Test
    void s3PutPreservesBytesAndUsesPrivateObjectWithCorrectMetadata() throws Exception {
        S3Client client = mock(S3Client.class);
        ObjectProvider<S3Client> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(client);
        var service = new ProductImageStorage(settings(true), provider);
        byte[] bytes = image("png", 2);
        assertEquals("https://images.example.com/products/test.png", service.store("products/test.png", "image/png", bytes));
        var request = ArgumentCaptor.forClass(PutObjectRequest.class);
        var body = ArgumentCaptor.forClass(RequestBody.class);
        verify(client).putObject(request.capture(), body.capture());
        assertEquals("product-images-test", request.getValue().bucket());
        assertEquals("products/test.png", request.getValue().key());
        assertEquals("image/png", request.getValue().contentType());
        assertNull(request.getValue().acl());
        assertTrue(request.getValue().cacheControl().contains("immutable"));
        try (var input = body.getValue().contentStreamProvider().newStream()) { assertArrayEquals(bytes, input.readAllBytes()); }
    }

    @Test
    void storageFailureReturnsSafeJapaneseMessageNotAwsDetails() {
        S3Client client = mock(S3Client.class);
        ObjectProvider<S3Client> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(client);
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().statusCode(403).message("private AWS details").build());
        var service = new ProductImageStorage(settings(true), provider);
        var error = assertThrows(ResponseStatusException.class, () -> service.store("products/a.png", "image/png", new byte[1]));
        assertEquals(502, error.getStatusCode().value());
        assertFalse(error.getReason().contains("private AWS details"));
    }
}
