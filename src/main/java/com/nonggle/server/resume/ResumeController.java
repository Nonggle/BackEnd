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
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            @RequestPart("data") @Valid ResumeCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile profileImage) {

        Long userId = (Long) authentication.getPrincipal();
        Long resumeId = resumeService.createResume(userId, request, profileImage);
        return ApiResponse.success(new ResumeIdResponse(resumeId));
    }

    @GetMapping
    public ApiResponse<List<ResumeResponse>> getMyResumes(
            @AuthenticationPrincipal JwtAuthenticationToken authentication) {

        Long userId = (Long) authentication.getPrincipal();
        List<ResumeResponse> resumes = resumeService.findMyResumes(userId);
        return ApiResponse.success(resumes);
    }

    @GetMapping("/{id}")
    public ApiResponse<ResumeResponse> getMyResume(
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            @PathVariable("id") Long resumeId) {

        Long userId = (Long) authentication.getPrincipal();
        ResumeResponse resume = resumeService.findMyResume(userId, resumeId);
        return ApiResponse.success(resume);
    }
}
