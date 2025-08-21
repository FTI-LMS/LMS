package com.example.graphapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Training_Master")
public class TrainingMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Training_ID")
    private String trainingID;

    @Column(name=" Training_Name")
    private String  trainingName;

    @Column(name=" Training_Category")
    private String  category;

    @Column(name=" Training_Topic")
    private String  trainingTopic;

    @Column(name=" Training_Duration")
    private Double  duration;

    @Column(name=" Training_Content_Path")
    private String  trainingContentPath;

    public String getTrainingID() {
      return trainingID;
    }

    public void setTrainingID(String trainingID) {
      this.trainingID = trainingID;
    }

    public String getTrainingName() {
      return trainingName;
    }

    public void setTrainingName(String trainingName) {
      this.trainingName = trainingName;
    }

    public String getCategory() {
      return category;
    }

    public void setCategory(String category) {
      this.category = category;
    }

    public String getTrainingTopic() {
      return trainingTopic;
    }

    public void setTrainingTopic(String trainingTopic) {
      this.trainingTopic = trainingTopic;
    }

    public Double getDuration() {
      return duration;
    }

    public void setDuration(Double duration) {
      this.duration = duration;
    }

    public String getTrainingContentPath() {
      return trainingContentPath;
    }

    public void setTrainingContentPath(String trainingContentPath) {
      this.trainingContentPath = trainingContentPath;
  }
}
