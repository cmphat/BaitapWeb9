package vn.iotstar.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    Map<String, String> uploadImage(MultipartFile file);
    void deleteImage(String publicId);
}
