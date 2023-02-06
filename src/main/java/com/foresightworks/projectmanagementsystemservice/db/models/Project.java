package com.foresightworks.projectmanagementsystemservice.db.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    private String uid;
    private String name;
    private String type;
    @Schema(type = "string", pattern = "dd-MM-yyyy", example = "22-01-2023")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date startDate;
    @Schema(type = "string", pattern = "dd-MM-yyyy", example = "22-01-2023")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date endDate;
    private String parentUid;
}
