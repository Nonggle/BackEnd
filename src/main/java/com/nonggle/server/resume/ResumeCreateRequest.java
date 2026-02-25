package com.nonggle.server.resume;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 이력서 생성/수정 요청 DTO
 * - 파일(사진)은 MultipartFile로 별도 파트(file)로 받는다.
 * - 이 DTO는 multipart의 "data" 파트(JSON)로 전달된다.
 */
public record ResumeCreateRequest(
        @NotBlank String userName,
        String userAge,
        String birthDate,
        String gender,

        @Nullable List<CertificationTag> certificationList,
        List<CareerFormData> careerList,

        /**
         * 총 경력 표현 문자열
         * 예: "27m" (총 개월수), "2y3m", 또는 "P2Y3M" 등
         * - 클라이언트/서버에서 파싱 규칙을 한 가지로 고정 권장
         */
        @NotBlank String totalCareer,

        String introduce,
        String introduceDetail,

        List<PersonalityTag> personalityList
) {
    public record CertificationTag(
            @NotBlank String certificationTitle
    ) {}

    public record CareerFormData(
            String careerStartDate,
            String careerEndDate,
            String careerPeriod,
            String careerDescription,
            String careerDetail
    ) {}

    public record PersonalityTag(
            @NotBlank String personality
    ) {}
}
