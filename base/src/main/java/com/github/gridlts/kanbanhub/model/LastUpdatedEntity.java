package com.github.gridlts.kanbanhub.model;

import lombok.Data;

import javax.persistence.*;
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
