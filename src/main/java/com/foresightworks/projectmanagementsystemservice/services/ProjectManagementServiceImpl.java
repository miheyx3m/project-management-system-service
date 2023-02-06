package com.foresightworks.projectmanagementsystemservice.services;

import com.foresightworks.projectmanagementsystemservice.api.models.Items;
import com.foresightworks.projectmanagementsystemservice.data.AppData;
import com.foresightworks.projectmanagementsystemservice.data.models.ProjectTree;
import com.foresightworks.projectmanagementsystemservice.db.models.Project;
import com.foresightworks.projectmanagementsystemservice.db.repositories.ProjectRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProjectManagementServiceImpl implements ProjectManagementService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private AppData appData;

    @Override
    public List<Project> uploadProjectFromFile(byte[] fileDataBytes) {
        String strBytes = new String(fileDataBytes);
        Type byteType = new TypeToken<Items>() {
        }.getType();
        Items projectRequest = null;
        try {
            projectRequest = new Gson().fromJson(strBytes, byteType);
        } catch (JsonSyntaxException e) {
            log.error("Wrong json file");
            return null;
        }
        return uploadProjectData(projectRequest);
    }

    @Override
    public List<Project> getProjectsFromDB() {
        return projectRepository.findAll();
    }

    @Override
    public Project getProjectFromDB(String uid) {
        Optional<Project> project = projectRepository.findByUid(uid);
        if (!project.isPresent()) {
            return null;
        }
        return project.get();
    }

    @Override
    public List<Project> uploadProjectData(Items projects) {
        List<Project> duplicateProjects = new ArrayList<>();
        List<Project> projectList = projects.getItems();
        appData.getCurrentProjectMap().values().forEach(project -> {
            if (projectList.contains(project)) {
                duplicateProjects.add(project);
                projectList.remove(project);
            }
        });// are uids already in the project, the duplicate uids won't add in the project
        log.warn("Duplicate uids {}", duplicateProjects);
        projectRepository.saveAll(projectList);
        projectList.forEach(project -> {
            calculateDates(project);
            appData.getCurrentProjectMap().put(project.getUid(), project);
        });
        return projectList;
    }

    /*
    update dates at bottom of the project hierarchy
     */
    private void calculateDates(Project project) {
        Date startDate = project.getStartDate();
        Date endDate = project.getEndDate();

        List<Project> childrenList = projectRepository.findAllByParentUid(project.getUid());
        for (Project child : childrenList) {
            calculateDates(child);
            if (startDate == null || child.getStartDate().before(startDate)) {
                startDate = child.getStartDate();
            }
            if (endDate == null || child.getEndDate().after(endDate)) {
                endDate = child.getEndDate();
            }
        }
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        projectRepository.save(project);
    }

    /*
    update dates at top of the project hierarchy
     */
    private void updateDates(Project project) {
        if (project.getParentUid() == null) {
            return;
        }
        Date startDate = project.getStartDate();
        Date endDate = project.getEndDate();

        Optional<Project> parent = projectRepository.findByUid(project.getParentUid());
        if (!parent.isPresent()) {
            return;
        }
        if (startDate == null || parent.get().getStartDate().before(startDate)) {
            startDate = parent.get().getStartDate();
        }
        if (endDate == null || parent.get().getEndDate().after(endDate)) {
            endDate = parent.get().getEndDate();
        }
        parent.get().setStartDate(startDate);
        parent.get().setEndDate(endDate);
        projectRepository.save(project);
        updateDates(parent.get());
    }

    @Override
    public Optional<Project> addTaskOrSubproject(Project newProject) {
        String parentUid = newProject.getParentUid();
        Optional<Project> parentProject = projectRepository.findByUid(parentUid);
        if (!parentProject.isPresent()) {
            return Optional.empty();
        }
        Project savedProject = projectRepository.save(newProject);
        appData.getCurrentProjectMap().put(newProject.getUid(), newProject);
        updateDates(newProject);
        return Optional.of(savedProject);
    }

    @Override
    public Project deleteTaskOrSubproject(String uid) {
        Optional<Project> project = projectRepository.findByUid(uid);
        if (!project.isPresent()) {
            log.error("Project/Subproject/Task not found with uid: " + uid);
            return null;
        }
        Optional<Project> parentProject = projectRepository.findByUid(project.get().getParentUid());
        projectRepository.delete(project.get());
        appData.getCurrentProjectMap().remove(uid);
        if (parentProject.isPresent()) {
            updateDates(parentProject.get());
        }
        return project.get();
    }

    @Override
    public float calculateCompletionProjectStatusByDate(String uid, Date checkDate) {
        Optional<Project> project = projectRepository.findByUid(uid);
        if (!project.isPresent()) {
            log.error("Project/Subproject/Task not found with uid: " + uid);
            return 0;
        }
        Date startDate = project.get().getStartDate();
        Date endDate = project.get().getEndDate();
        if (startDate == null || endDate == null) {
            log.error("Start date or end date is null for project with uid: " + uid);
            return 0;
        }
        long totalDays = ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant());
        long elapsedDays = ChronoUnit.DAYS.between(startDate.toInstant(), checkDate == null ? Instant.now() : checkDate.toInstant());
        return (float) elapsedDays / totalDays * 100;
    }

    @Override
    public Project updateEntityDates(String uid, Date newStartDate, Date newEndDate) {
        Optional<Project> existingProject = projectRepository.findByUid(uid);
        if (!existingProject.isPresent()) {
            log.error("Project/Subproject/Task not found with uid: " + uid);
            return null;
        }
        if (newStartDate != null) existingProject.get().setStartDate(newStartDate);
        if (newEndDate != null) existingProject.get().setEndDate(newEndDate);
        log.info("existingProject {}", existingProject.get());
        projectRepository.save(existingProject.get());
        updateDates(existingProject.get());
        return existingProject.get();
    }

    @Override
    public ProjectTree getProjectTree(String uid) {
        Optional<Project> project = projectRepository.findByUid(uid);
        if (!project.isPresent()) {
            log.error("Project/Subproject/Task not found with uid: " + uid);
            return null;
        }
        ProjectTree projectTree = new ProjectTree();
        projectTree.setUid(project.get().getUid());
        projectTree.setName(project.get().getName());
        projectTree.setType(project.get().getType());
        projectTree.setStartDate(project.get().getStartDate());
        projectTree.setEndDate(project.get().getEndDate());

        List<ProjectTree> children = new ArrayList<>();
        List<Project> subprojects = projectRepository.findAllByParentUid(project.get().getUid());
        for (Project subproject : subprojects) {
            children.add(getProjectTree(subproject.getUid()));
        }
        projectTree.setChildren(children);

        return projectTree;
    }
}
