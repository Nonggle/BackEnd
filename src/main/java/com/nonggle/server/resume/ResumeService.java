package com.nonggle.server.resume;

import com.nonggle.server.common.ApiException;
import com.nonggle.server.common.ErrorDefine;
import com.nonggle.server.file.FileStorageService;
import com.nonggle.server.user.User;
import com.nonggle.server.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository; // User 엔티티를 찾기 위함
    private final FileStorageService fileStorageService;

    @Transactional
    public Long createResume(Long userId, ResumeCreateRequest request, MultipartFile profileImage) {
        System.out.println("DEBUG: Creating resume for user - ID: " + userId + ", Name: " + request.userName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorDefine.UNAUTHORIZED));

        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = fileStorageService.storeFile(profileImage);
        }

        // null-safe mapping for lists
        List<String> certificationTitles = request.certificationList() == null ? new java.util.ArrayList<>() :
                request.certificationList().stream()
                        .map(ResumeCreateRequest.CertificationTag::certificationTitle)
                        .collect(Collectors.toList());

        List<Resume.CareerData> careerList = request.careerList() == null ? new java.util.ArrayList<>() :
                request.careerList().stream()
                        .map(careerData -> new Resume.CareerData(
                                careerData.careerStartDate(),
                                careerData.careerEndDate(),
                                careerData.careerPeriod(),
                                careerData.careerDescription(),
                                careerData.careerDetail()
                        ))
                        .collect(Collectors.toList());

        List<String> personalityTags = request.personalityList() == null ? new java.util.ArrayList<>() :
                request.personalityList().stream()
                        .map(ResumeCreateRequest.PersonalityTag::personality)
                        .collect(Collectors.toList());

        Resume resume = Resume.builder()
                .user(user)
                .userName(request.userName())
                .userAge(request.userAge())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .certificationTitles(certificationTitles)
                .careerList(careerList)
                .totalCareer(request.totalCareer())
                .introduce(request.introduce())
                .introduceDetail(request.introduceDetail())
                .personalityTags(personalityTags)
                .profileImageUrl(profileImageUrl)
                .build();

        resumeRepository.save(resume);
        System.out.println("DEBUG: Resume saved successfully with ID: " + resume.getId());
        return resume.getId();
    }

    public List<ResumeResponse> findMyResumes(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorDefine.UNAUTHORIZED));

        List<Resume> resumes = resumeRepository.findAllByUser(user);
        return resumes.stream()
                .map(ResumeResponse::from)
                .collect(Collectors.toList());
    }

    public ResumeResponse findMyResume(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorDefine.RESUME_NOT_FOUND));

        if (!resume.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorDefine.FORBIDDEN);
        }
        return ResumeResponse.from(resume);
    }

    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException(ErrorDefine.RESUME_NOT_FOUND));

        if (!resume.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorDefine.FORBIDDEN);
        }

        resumeRepository.delete(resume);
    }
}
