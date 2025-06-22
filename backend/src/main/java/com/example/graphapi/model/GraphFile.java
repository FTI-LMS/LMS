package com.example.graphapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a file from Microsoft OneDrive")
public class GraphFile {

    @Schema(description = "Unique identifier of the file", example = "01BYE5RZ6QN3ZWBTUFOFD3GSPGOHDJD36K")
    private String id;

    @Schema(description = "Name of the file", example = "document.pdf")
    private String name;

    @Schema(description = "Web URL to access the file", example = "https://onedrive.live.com/...")
    private String webUrl;

    @Schema(description = "Size of the file in bytes", example = "1048576")
    private Long size;

    @Schema(description = "File creation date and time", example = "2023-01-01T12:00:00Z")
    private String createdDateTime;

    @Schema(description = "Last modified date and time", example = "2023-01-02T14:30:00Z")
    private String lastModifiedDateTime;

    @Schema(description = "Direct download URL for the file")
    private String downloadUrl;

    // Constructors
    public GraphFile() {}

    public GraphFile(String id, String name, String webUrl, long size, 
                    String createdDateTime, String lastModifiedDateTime, String downloadUrl) {
        this.id = id;
        this.name = name;
        this.webUrl = webUrl;
        this.size = size;
        this.createdDateTime = createdDateTime;
        this.lastModifiedDateTime = lastModifiedDateTime;
        this.downloadUrl = downloadUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(String lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}