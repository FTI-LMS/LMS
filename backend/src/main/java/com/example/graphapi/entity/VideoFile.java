package com.example.graphapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "video_files")
public class VideoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;

    private String itemID;

    private String driveID;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

      public String getItemID() {
        return itemID;
      }

      public void setItemID(String itemID) {
        this.itemID = itemID;
      }

      public String getDriveID() {
        return driveID;
      }

      public void setDriveID(String driveID) {
        this.driveID = driveID;
      }
}
