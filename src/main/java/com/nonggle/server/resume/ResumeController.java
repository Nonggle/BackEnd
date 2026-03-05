package com.nonggle.server.resume;

import com.nonggle.server.auth.JwtAuthenticationToken;
import com.nonggle.server.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes") // API 버전 관리 및 경로 설정
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ResumeIdResponse> createResume(
            @AuthenticationPrincipal Long userId,
            @RequestPart("data") @Valid ResumeCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile profileImage) {

        if (profileImage != null) {
            System.out.println("DEBUG: File received - Name: " + profileImage.getName());
            System.out.println("DEBUG: File received - OriginalName: " + profileImage.getOriginalFilename());
            System.out.println("DEBUG: File received - Size: " + profileImage.getSize());
        } else {
            System.out.println("DEBUG: No file received (profileImage is null)");
        }

        Long resumeId = resumeService.createResume(userId, request, profileImage);
        return ApiResponse.success(new ResumeIdResponse(resumeId));
    }

    @GetMapping
    public ApiResponse<List<ResumeResponse>> getMyResumes(
            @AuthenticationPrincipal Long userId) {

        List<ResumeResponse> resumes = resumeService.findMyResumes(userId);
        return ApiResponse.success(resumes);
    }

    @GetMapping("/{id}")
    public ApiResponse<ResumeResponse> getMyResume(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long resumeId) {

        ResumeResponse resume = resumeService.findMyResume(userId, resumeId);
        return ApiResponse.success(resume);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResume(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long resumeId) {

        resumeService.deleteResume(userId, resumeId);
        return ApiResponse.success(null);
    }
}
