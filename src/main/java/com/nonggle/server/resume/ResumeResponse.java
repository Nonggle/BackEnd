package com.nonggle.server.resume;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

public record ResumeResponse(
        Long id,
        Long userId, // 소유자 ID 추가
        String userName,
        @Nullable String birthDate,
        String userAge,
        String userGender,
        @Nullable List<String> certificationList,
        @Nullable List<CareerResponseData> careerList,
        String totalCareer,
        @Nullable String introduce,
        @Nullable String introduceDetail,
        @Nullable List<String> personalityList,
        @Nullable String profileImageUrl,
        @Nullable String createdAt,
        @Nullable String updatedAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getUser() != null ? resume.getUser().getId() : null,
                resume.getUserName(),
                resume.getBirthDate(),
                resume.getUserAge(),
                resume.getGender(),
                resume.getCertificationTitles() != null ? resume.getCertificationTitles() : new java.util.ArrayList<>(),
                resume.getCareerList() != null ?
                        resume.getCareerList().stream()
                                .filter(c -> c != null)
                                .map(CareerResponseData::from)
                                .collect(Collectors.toList()) : new java.util.ArrayList<>(),
                resume.getTotalCareer(),
                resume.getIntroduce(),
                resume.getIntroduceDetail(),
                resume.getPersonalityTags() != null ? resume.getPersonalityTags() : new java.util.ArrayList<>(),
                resume.getProfileImageUrl(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }

    public record CareerResponseData(
            String careerStartDate,
            String careerEndDate,
            String careerPeriod,
            String careerDescription,
            String careerDetail
    ) {
        public static CareerResponseData from(Resume.CareerData careerData) {
            return new CareerResponseData(
                    careerData.getCareerStartDate(),
                    careerData.getCareerEndDate(),
                    careerData.getCareerPeriod(),
                    careerData.getCareerDescription(),
                    careerData.getCareerDetail()
            );
        }
    }
}
