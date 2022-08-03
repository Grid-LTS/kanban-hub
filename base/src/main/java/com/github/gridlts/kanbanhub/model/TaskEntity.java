package com.github.gridlts.kanbanhub.model;

import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @Column(name = "task_id", nullable = false, columnDefinition = "char(36)")
    private String taskId;

    @Column(name = "resource_id", nullable = false, columnDefinition = "varchar(50)")
    private String resourceId;

    @Column(name = "insert_ts", nullable = false, columnDefinition = "text")
    @CreationTimestamp
    private Instant insertTime;

    @Column(name = "update_ts", nullable = false, columnDefinition = "text")
    @UpdateTimestamp
    private Instant updateTime;

    @Column(name = "title", nullable = false, columnDefinition = "varchar(255)")
    private String title;

    private String description;

    @Column(nullable = false, columnDefinition = "varchar(16)")
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "creation_date", nullable = false, columnDefinition = "text")
    private LocalDate creationDate;

    @Column(name = "completion_date", nullable = true, columnDefinition = "text")
    private LocalDate completionDate;

    @Column(nullable = false, columnDefinition = "varchar(32)")
    private String resource;

    @Column(name = "project_code", columnDefinition = "varchar(50)")
    private String projectCode;

    @Convert(converter = StringListConverter.class)
    private List<String> tags;

}
