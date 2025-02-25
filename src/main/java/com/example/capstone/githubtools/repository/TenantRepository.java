package com.example.capstone.githubtools.repository;

import com.example.capstone.githubtools.model.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {
}
