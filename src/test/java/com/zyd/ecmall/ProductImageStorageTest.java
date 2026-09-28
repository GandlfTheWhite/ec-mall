package com.zyd.ecmall;

import com.zyd.ecmall.service.ProductImageStorage;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductImageStorageTest {
    private final S3Client s3 = mock(S3Client.class);
    private final ProductImageStorage storage = new ProductImageStorage(s3, "media-bucket", "https://media.example.com");
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 1};

    @Test
    void uploadsImageWithGeneratedKeyAndStableCloudFrontUrl() {
        String url = storage.upload(new MockMultipartFile("file", "../../photo.png", "image/png", PNG));
        assertTrue(url.matches("https://media\\.example\\.com/products/[0-9a-f-]{36}\\.png"));
        verify(s3).putObject(argThat((PutObjectRequest request) ->
                request.bucket().equals("media-bucket")
                        && url.endsWith(request.key())
                        && request.contentType().equals("image/png")), any(RequestBody.class));
    }

    @Test
    void rejectsSpoofedTypeAndOversizedFilesWithoutCallingS3() {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> storage.upload(
                new MockMultipartFile("file", "fake.png", "image/png", "not an image".getBytes())))
                .getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> storage.upload(
                new MockMultipartFile("file", "photo.png", "image/jpeg", PNG)))
                .getStatusCode().value());
        assertEquals(413, assertThrows(ResponseStatusException.class, () -> storage.upload(
                new MockMultipartFile("file", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1])))
                .getStatusCode().value());
        verifyNoInteractions(s3);
    }
}
