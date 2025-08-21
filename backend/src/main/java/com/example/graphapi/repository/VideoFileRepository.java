package com.example.graphapi.repository;

import com.example.graphapi.entity.VideoFiles;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VideoFileRepository extends JpaRepository<VideoFiles, Long> {}
