package com.github.gridlts.kanbanhub.repository;

import com.github.gridlts.kanbanhub.model.LastUpdatedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LastUpdatedRepository extends JpaRepository<LastUpdatedEntity, String>  {

    Optional<LastUpdatedEntity> findOneByResource(String resource);
}
