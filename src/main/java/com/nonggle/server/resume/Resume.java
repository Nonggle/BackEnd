package com.nonggle.server.resume;

import com.nonggle.server.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 이력서 소유자

    private String userName; // ResumeCreateRequest의 userName

    private String userAge; // ResumeCreateRequest의 userAge

    private String birthDate; // ResumeCreateRequest의 birthDate

    private String gender; // ResumeCreateRequest의 gender

    @ElementCollection
    @CollectionTable(name = "resume_certification_tags", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "certification_title")
    private List<String> certificationTitles; // ResumeCreateRequest의 certificationList (간소화)

    @ElementCollection
    @CollectionTable(name = "resume_career_data", joinColumns = @JoinColumn(name = "resume_id"))
    private List<CareerData> careerList; // ResumeCreateRequest의 careerList

    private String totalCareer; // ResumeCreateRequest의 totalCareer

    @Column(length = 1000)
    private String introduce; // ResumeCreateRequest의 introduce

    @Column(length = 2000)
    private String introduceDetail; // ResumeCreateRequest의 introduceDetail

    @ElementCollection
    @CollectionTable(name = "resume_personality_tags", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "personality_tag")
    private List<String> personalityTags; // ResumeCreateRequest의 personalityList (간소화)

    private String profileImageUrl; // 프로필 사진 URL (nullable)

    private String createdAt;
    private String updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CareerData {
        private String careerStartDate;
        private String careerEndDate;
        private String careerPeriod;
        @Column(length = 500)
        private String careerDescription;
        @Column(length = 1000)
        private String careerDetail;
    }
}
