package com.example.graphapi.repository;

import com.example.graphapi.entity.VideoFile;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VideoFileRepository extends JpaRepository<VideoFile, Long> {}
