package com.github.gridlts.khapi.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @Column(name = "task_id", nullable=false, columnDefinition = "char(36)")
    private String taskId;

    @Column(name = "resource_id", nullable=false, columnDefinition = "varchar(50)")
    private String resourceId;

    @Column(name="insert_ts", nullable=false, columnDefinition = "datetime(0")
    @CreationTimestamp
    private Instant insertTime;

    @Column(name="title", nullable = false, columnDefinition = "varchar(255)")
    private String title;

    private String description;

    @Column(name="completion_date", nullable=false, columnDefinition = "date")
    private Instant completionDate;

    @Column(nullable=false, columnDefinition = "varchar(32)")
    private String resource;

    @Column(name="project_code", columnDefinition = "varchar(50)")
    private String projectCode;

    @Convert(converter = StringListConverter.class)
    private List<String> tags;

}
