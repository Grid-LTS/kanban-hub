package com.github.gridlts.khapi.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "last_updated")
public class LastUpdatedEntity {

    @Id
    @Column(nullable=false, columnDefinition = "varchar(32)")
    private String resource;

    @Column(name = "last_updated_time", nullable=false, columnDefinition = "timestamp")
    LocalDateTime lastUpdated;
}
