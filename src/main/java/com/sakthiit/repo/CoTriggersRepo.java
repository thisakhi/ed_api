package com.sakthiit.repo;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sakthiit.entity.CoTriggersEntity;

public interface CoTriggersRepo extends JpaRepository<CoTriggersEntity, Serializable> {

}
