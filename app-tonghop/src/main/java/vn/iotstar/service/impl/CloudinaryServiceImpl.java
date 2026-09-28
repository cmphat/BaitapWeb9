package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    @Value("${cloudinary.cloud-name:demo_cloud}")
    private String cloudName;

    @Value("${cloudinary.api-key:demo_key}")
    private String apiKey;

    @Value("${cloudinary.api-secret:demo_secret}")
    private String apiSecret;

    @Override
    public Map<String, String> uploadImage(MultipartFile file) {
        Map<String, String> result = new HashMap<>();

        if (file == null || file.isEmpty()) {
            return result;
        }

        // Kiểm tra loại file (chỉ cho phép định dạng ảnh hợp lệ)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file định dạng hình ảnh (JPEG, PNG, GIF, WEBP)!");
        }

        // 1. Thử upload lên Cloudinary nếu thông tin credentials không phải là placeholder
        boolean hasValidCloudinaryCredentials = !cloudName.equals("demo_cloud") && !cloudName.equals("your_cloud_name")
                && !apiKey.equals("demo_key") && !apiSecret.equals("demo_secret");

        if (hasValidCloudinaryCredentials) {
            try {
                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", apiKey,
                        "api_secret", apiSecret,
                        "secure", true
                ));

                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", "baitapweb9_products"
                ));

                String secureUrl = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");

                result.put("url", secureUrl);
                result.put("public_id", publicId);
                logger.info("Upload Cloudinary thành công: {}", secureUrl);
                return result;
            } catch (Exception e) {
                logger.warn("Upload lên Cloudinary thất bại, tự động chuyển sang lưu trữ cục bộ: {}", e.getMessage());
            }
        }

        // 2. Fallback: Lưu vào thư mục uploads cục bộ (để test và chạy offline luôn luôn thành công)
        try {
            String uploadDir = "uploads/products";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String localUrl = "/uploads/products/" + fileName;
            result.put("url", localUrl);
            result.put("public_id", "local_" + fileName);
            logger.info("Upload lưu trữ cục bộ thành công: {}", localUrl);
            return result;
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi lưu file ảnh cục bộ: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return;
        }

        if (publicId.startsWith("local_")) {
            String fileName = publicId.substring(6);
            File file = new File("uploads/products/" + fileName);
            if (file.exists()) {
                file.delete();
            }
            return;
        }

        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Xóa ảnh Cloudinary thành công: {}", publicId);
        } catch (Exception e) {
            logger.warn("Không thể xóa ảnh trên Cloudinary: {}", e.getMessage());
        }
    }
}
