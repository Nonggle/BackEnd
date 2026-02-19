package com.nonggle.server.file;

import com.nonggle.server.common.ApiException;
import com.nonggle.server.common.ErrorDefine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final String baseUrl = "http://localhost:8080/uploads/"; // TODO: 실제 배포 시에는 CDN 주소 등으로 변경 필요

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new ApiException(ErrorDefine.INTERNAL_ERROR, "파일을 저장할 디렉토리를 생성할 수 없습니다.");
        }
    }

    public String storeFile(MultipartFile file) {
        // 파일명에 부적절한 문자가 있는지 확인
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "");

        try {
            // 파일명에 ".."와 같은 경로 시퀀스가 포함되어 있는지 확인
            if (fileName.contains("..")) {
                throw new ApiException(ErrorDefine.BAD_REQUEST, "파일명에 부적절한 경로 시퀀스가 포함되어 있습니다.");
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            return baseUrl + fileName;
        } catch (IOException ex) {
            throw new ApiException(ErrorDefine.INTERNAL_ERROR, "파일을 저장할 수 없습니다. " + originalFileName);
        }
    }
}
