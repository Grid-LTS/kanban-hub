package com.github.gridlts.khapi.model;

import com.github.gridlts.khapi.resources.TaskResourceType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @Column(name = "task_id", nullable=false, columnDefinition = "char(36)")
    private String taskId;

    @Column(name="title", nullable = false, columnDefinition = "varchar(255)")
    private String title;

    private String description;

    @Column(name="completion_date", nullable=false, columnDefinition = "datetime")
    private LocalDate completionDate;

    @Column(nullable=false, columnDefinition = "varchar(32)")
    private TaskResourceType resource;

    @Column(name="project_code", columnDefinition = "varchar(50)")
    private String projectCode;

    @Convert(converter = StringListConverter.class)
    private List<String> tags;

}
