package com.example.graphapi.repository;

import com.example.graphapi.entity.CategorayDetails;
import com.example.graphapi.entity.VideoFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryDetailsRepository extends JpaRepository<CategorayDetails, Long> {}
