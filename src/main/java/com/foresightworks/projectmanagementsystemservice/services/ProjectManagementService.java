package com.foresightworks.projectmanagementsystemservice.services;

import com.foresightworks.projectmanagementsystemservice.api.models.Items;
import com.foresightworks.projectmanagementsystemservice.data.models.ProjectTree;
import com.foresightworks.projectmanagementsystemservice.db.models.Project;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ProjectManagementService {
    List<Project> uploadProjectFromFile(byte[] fileDataBytes);

    List<Project> getProjectsFromDB();

    Project getProjectFromDB(String uid);

    List<Project> uploadProjectData(Items projects);

    Optional<Project> addTaskOrSubproject(Project newProject);

    Project deleteTaskOrSubproject(String uid);

    float calculateCompletionProjectStatusByDate(String uid, Date checkDate);

    Project updateDatesProjectorTaskOrSubproject(String uid, Date newStartDate, Date newEndDate);

    ProjectTree getProjectTree(String uid);
}
