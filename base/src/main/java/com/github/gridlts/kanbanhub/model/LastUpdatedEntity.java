package com.github.gridlts.kanbanhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "last_updated")
public class LastUpdatedEntity {

    @Id
    @Column(nullable=false, columnDefinition = "varchar(32)")
    private String resource;

    @Column(name = "last_updated_time", nullable=false, columnDefinition = "text")
    LocalDateTime lastUpdated;
}
