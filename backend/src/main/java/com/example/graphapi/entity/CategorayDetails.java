package com.example.graphapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "category_details")
public class CategorayDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String instructorName;
    private String category;
    private Double duration;

      public Long getId() {
        return id;
      }

      public void setId(Long id) {
        this.id = id;
      }

      public String getFileName() {
        return fileName;
      }

      public void setFileName(String fileName) {
        this.fileName = fileName;
      }

      public String getInstructorName() {
        return instructorName;
      }

      public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
      }

      public String getCategory() {
        return category;
      }

      public void setCategory(String category) {
        this.category = category;
      }

      public Double getDuration() {
        return duration;
      }

      public void setDuration(Double duration) {
        this.duration = duration;
      }
}
