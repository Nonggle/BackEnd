package com.nonggle.server.resume;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;

public record ResumeResponse(
        Long id,
        Long userId, // 소유자 ID 추가
        String userName,
        String birthDate,
        String introduction,
        String gender,
        @Nullable List<String> certificationList,
        List<CareerResponseData> careerList,
        String totalCareer,
        String introduce,
        String introduceDetail,
        List<String> personalityList,
        String profileImageUrl,
        String createdAt,
        String updatedAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getUser().getId(),
                resume.getUserName(),
                resume.getBirthDate(),
                resume.getIntroduction(),
                resume.getGender(),
                resume.getCertificationTitles(),
                resume.getCareerList().stream()
                        .map(CareerResponseData::from)
                        .collect(Collectors.toList()),
                resume.getTotalCareer(),
                resume.getIntroduce(),
                resume.getIntroduceDetail(),
                resume.getPersonalityTags(),
                resume.getProfileImageUrl(),
                resume.getCreatedAt() != null ? resume.getCreatedAt().toString() : null,
                resume.getUpdatedAt() != null ? resume.getUpdatedAt().toString() : null
        );
    }

    public record CareerResponseData(
            String careerStartDate,
            String careerEndDate,
            String careerDescription,
            String careerDetail
    ) {
        public static CareerResponseData from(Resume.CareerData careerData) {
            return new CareerResponseData(
                    careerData.getCareerStartDate(),
                    careerData.getCareerEndDate(),
                    careerData.getCareerDescription(),
                    careerData.getCareerDetail()
            );
        }
    }
}
