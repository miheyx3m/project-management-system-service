package com.foresightworks.projectmanagementsystemservice.api.controllers;

import com.foresightworks.projectmanagementsystemservice.api.models.Items;
import com.foresightworks.projectmanagementsystemservice.data.AppData;
import com.foresightworks.projectmanagementsystemservice.db.models.Project;
import com.foresightworks.projectmanagementsystemservice.data.models.ProjectTree;
import com.foresightworks.projectmanagementsystemservice.services.ProjectManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Api(tags = "Project Management System")
@RestController
@RequestMapping("/api/project")
@Slf4j
public class ProjectManagementController {

    @Autowired
    private ProjectManagementService projectManagementService;
    @Autowired
    private AppData appData;

    @PostMapping("/upload")
    public ResponseEntity<List<Project>> uploadAllProjectData(@RequestBody Items projectRequest) {
        List<Project> projectList = projectManagementService.uploadProjectData(projectRequest);
        return new ResponseEntity<>(projectList, HttpStatus.OK);
    }

    @PostMapping("/uploadFile")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Wrong Json file")
    })
    public ResponseEntity<List<Project>> uploadProjectFromJsonFile(@RequestPart("file") MultipartFile file) throws IOException {
        List<Project> projectList = projectManagementService.uploadProjectFromFile(file.getBytes());
        return projectList == null ? ResponseEntity.noContent().build() : new ResponseEntity<>(projectList, HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Not exist Project/Subproject/Task Uid")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<Project>> getAllProjectData() {
        return new ResponseEntity<>(projectManagementService.getProjectsFromDB(), HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Not exist Project/Subproject Parent Uid")
    })
    @GetMapping("/get")
    public ResponseEntity<Project> getProjectByUid(@RequestParam String uid) {
        Project existingProject = projectManagementService.getProjectFromDB(uid);
        return existingProject == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(existingProject);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Not exist Project/Subproject Parent Uid"),
            @ApiResponse(code = 200, message = "Project/Subproject/Task added")
    })
    @PostMapping("/add")
    public ResponseEntity<Project> addTaskOrSubproject(@RequestBody Project newProject) {
        Optional<Project> addedProject = projectManagementService.addTaskOrSubproject(newProject);
        if (!addedProject.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(addedProject.get());
    }

    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Not exist Project/Subproject/Task Uid"),
            @ApiResponse(code = 200, message = "Project/Subproject/Task deleted"),
            @ApiResponse(code = 400, message = "Can't delete last element in the project")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Project> deleteTaskOrSubproject(@RequestParam String uid) {
        if (appData.getCurrentProjectMap().size() <= 1) {
            return ResponseEntity.notFound().build();
        }
        Project deleteProject = projectManagementService.deleteTaskOrSubproject(uid);
        return deleteProject == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(deleteProject);
    }

    @GetMapping("/status")
    public ResponseEntity<Float> calculateCompletionUidStatus(@RequestParam String uid,
                                                              @RequestParam(required = false)
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "dd-MM-yyyy") Date checkDate) {
        float completionStatus = projectManagementService.calculateCompletionProjectStatusByDate(uid, checkDate);
        return ResponseEntity.ok(completionStatus);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Not exist Project/Subproject/Task Uid"),
            @ApiResponse(code = 200, message = "Project/Subproject/Task dates were updated"),
            @ApiResponse(code = 400, message = "Wrong request parameter/s")
    })
    @PutMapping("/updateDates")
    public ResponseEntity<Project> updateDates(@RequestParam String uid,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "dd-MM-yyyy") Date newStartDate,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "dd-MM-yyyy") Date newEndDate) {
        if ((newStartDate == null && newEndDate == null) || (newStartDate != null && newEndDate != null && newStartDate.after(newEndDate))) {
            ResponseEntity.badRequest().build();
        }
        Project existingProject = projectManagementService.updateEntityDates(uid, newStartDate, newEndDate);

        return existingProject == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(existingProject);
    }

    @GetMapping("/projectTree")
    public ResponseEntity<ProjectTree> getProjectTree(@RequestParam String uid) {
        ProjectTree projectTree = projectManagementService.getProjectTree(uid);
        return ResponseEntity.ok(projectTree);
    }

}
