package com.zyd.ecmall.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductImageService {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private final ProductImageStorage storage;

    public ProductImageService(ProductImageStorage storage) { this.storage = storage; }

    public record UploadResult(String imageUrl, String key) {}

    public UploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("画像ファイルを選択してください。");
        if (file.getSize() > MAX_BYTES) throw tooLarge();
        byte[] bytes;
        try (var input = file.getInputStream()) {
            bytes = input.readNBytes(MAX_BYTES + 1);
        } catch (IOException e) { throw invalid("画像ファイルを読み込めません。"); }
        if (bytes.length > MAX_BYTES) throw tooLarge();
        String extension = validate(bytes, file.getContentType());
        String key = "products/" + UUID.randomUUID() + "." + extension;
        String type = extension.equals("jpg") ? "image/jpeg" : "image/" + extension;
        return new UploadResult(storage.store(key, type, bytes), key);
    }

    private String validate(byte[] bytes, String declaredType) {
        // 拡張子・申告MIMEだけで判断せず、画像のヘッダーと画素をデコードして検証する。
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalid("JPEG・PNG・WebPの画像を選択してください。");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                String extension = switch (format) {
                    case "jpeg", "jpg" -> "jpg";
                    case "png" -> "png";
                    case "webp" -> "webp";
                    default -> throw invalid("JPEG・PNG・WebPの画像を選択してください。");
                };
                String type = extension.equals("jpg") ? "image/jpeg" : "image/" + extension;
                if (!type.equalsIgnoreCase(declaredType)) throw invalid("画像形式とファイルの種類が一致していません。");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 8192 || height > 8192
                        || (long) width * height > 16_000_000) {
                    throw invalid("画像は各辺8192px以下、合計1600万画素以下にしてください。");
                }
                BufferedImage decoded = reader.read(0);
                if (decoded == null) throw invalid("画像ファイルが破損しています。");
                decoded.flush();
                return extension;
            } finally { reader.dispose(); }
        } catch (ResponseStatusException e) { throw e; }
        catch (IOException | RuntimeException e) { throw invalid("画像ファイルが破損しているか、対応していない形式です。"); }
    }

    private ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException tooLarge() {
        return new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "画像は5 MB以下にしてください。");
    }
}
