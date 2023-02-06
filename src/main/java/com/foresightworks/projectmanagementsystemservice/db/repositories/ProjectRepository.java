package com.foresightworks.projectmanagementsystemservice.db.repositories;

import com.foresightworks.projectmanagementsystemservice.db.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByUid(String uid);

    List<Project> findAllByParentUid(String uid);
}
