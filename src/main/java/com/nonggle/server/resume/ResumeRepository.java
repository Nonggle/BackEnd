package com.nonggle.server.resume;

import com.nonggle.server.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findAllByUser(User user);
}
