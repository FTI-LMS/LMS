
package com.example.graphapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphFile {
    private String id;
    private String name;
    private String webUrl;
    private long size;
    private String createdDateTime;
    private String lastModifiedDateTime;
    
    @JsonProperty("@microsoft.graph.downloadUrl")
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
