package com.example.graphapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "video_files")
public class VideoFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20000,name="File_Name")
    private String fileName;

    @Column(length = 20000,name="File_Path")
    private String filePath;

    @Column(name="File_ID")
    private String itemID;

    private String driveID;

    @Column(length = 20000,name="Folder_Name")
    private String folderName;

    @Column(name="Folder_ID")
    private String folderid;

    private int fileCount;


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


    public String getFolderName() {
      return folderName;
    }

    public void setFolderName(String folderName) {
      this.folderName = folderName;
    }

    public String getFolderid() {
      return folderid;
    }

    public void setFolderid(String folderid) {
      this.folderid = folderid;
    }

  public int getFileCount() {
      return fileCount;
    }

    public void setFileCount(int fileCount) {
      this.fileCount = fileCount;
    }
}
