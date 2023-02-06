package com.foresightworks.projectmanagementsystemservice.api.models;

import com.foresightworks.projectmanagementsystemservice.db.models.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Items {
    private List<Project> items;
}
