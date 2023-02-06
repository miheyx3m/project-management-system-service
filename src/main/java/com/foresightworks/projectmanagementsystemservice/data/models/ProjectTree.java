package com.foresightworks.projectmanagementsystemservice.data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTree {
    private String uid;
    private String name;
    private String type;
    private Date startDate;
    private Date endDate;
    private List<ProjectTree> children;
}
