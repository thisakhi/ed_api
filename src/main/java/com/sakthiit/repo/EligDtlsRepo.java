package com.sakthiit.repo;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sakthiit.entity.EligDtlsEntity;

public interface EligDtlsRepo extends JpaRepository<EligDtlsEntity, Serializable>{

}
