package com.example.graphapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Training_Details")
public class TrainingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Training_ID")
    private String trainingId;

    @Column(name=" Training_Detail_ID")
    private String  trainingDetailId;

    @Column(name=" Module_Name")
    private String  moduleName;

    @Column(name=" Module_Topic")
    private String  moduleTopic;

    @Column(name=" Module_Duration")
    private Double duration;

    @Column(name=" Module_Path")
    private String modulePath;

    @Column(name=" Trainer_Name")
    private String instructorName;

    public String getTrainingId() {
      return trainingId;
    }

    public void setTrainingId(String trainingId) {
      this.trainingId = trainingId;
    }

    public String getTrainingDetailId() {
      return trainingDetailId;
    }

    public void setTrainingDetailId(String trainingDetailId) {
      this.trainingDetailId = trainingDetailId;
    }

    public String getModuleName() {
      return moduleName;
    }

    public void setModuleName(String moduleName) {
      this.moduleName = moduleName;
    }

    public String getModuleTopic() {
      return moduleTopic;
    }

    public void setModuleTopic(String moduleTopic) {
      this.moduleTopic = moduleTopic;
    }

    public Double getDuration() {
      return duration;
    }

    public void setDuration(Double duration) {
      this.duration = duration;
    }

    public String getModulePath() {
      return modulePath;
    }

    public void setModulePath(String modulePath) {
      this.modulePath = modulePath;
    }

    public String getInstructorName() {
      return instructorName;
    }

    public void setInstructorName(String instructorName) {
      this.instructorName = instructorName;
    }
}
