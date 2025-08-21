package com.example.graphapi.service;

import com.example.graphapi.entity.TrainingDetails;
import com.example.graphapi.entity.TrainingMaster;
import com.example.graphapi.entity.VideoFiles;
import com.example.graphapi.model.GraphFile;
import com.example.graphapi.repository.VideoFileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GraphApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private VideoFileRepository repository;

    @Value("${target-url}")
    private String targetUrl;


    public GraphApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getVideoDownloadUrl(String accessToken, String driveId, String itemId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                String.format("https://graph.microsoft.com/v1.0/drives/%s/items/%s", driveId, itemId),
                HttpMethod.GET,
                entity,
                String.class
            );

            JsonNode itemNode = objectMapper.readTree(response.getBody());
            String downloadUrl = itemNode.path("@microsoft.graph.downloadUrl").asText();

            return downloadUrl;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get video download URL: " + e.getMessage());
        }
    }

    public boolean validateToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://graph.microsoft.com/v1.0/me",
                HttpMethod.GET,
                entity,
                String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public List<GraphFile> getAllFiles(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Get files from OneDrive
            ResponseEntity<String> response = restTemplate.exchange(
                "https://graph.microsoft.com/v1.0/me/drive/root/children",
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseFilesFromResponse(response.getBody(),null,null,null);
            }

            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch files from Graph API: " + e.getMessage());
        }
    }

    public List<GraphFile> getRecentFiles(String accessToken, int limit) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Get recent files
            ResponseEntity<String> response = restTemplate.exchange(
                "https://graph.microsoft.com/v1.0/me/drive/recent?$top=" + limit,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseFilesFromResponse(response.getBody(),null,null,null);
            }

            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch recent files from Graph API: " + e.getMessage());
        }
    }

    public List<GraphFile> getDriveItemChildren(String accessToken, String driveId, String itemId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Get children of specific drive item
            String url = String.format("https://graph.microsoft.com/v1.0/drives/%s/items/%s/children", driveId, itemId);
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseFilesFromResponse(response.getBody(), accessToken, driveId, itemId);
            }

            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch drive item children from Graph API: " + e.getMessage());
        }
    }

    public JsonNode getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://graph.microsoft.com/v1.0/me",
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user info from Graph API: " + e.getMessage());
        }
    }

    private List<GraphFile> parseFilesFromResponse(String responseBody,String accessToken, String driveId, String itemId) {
        List<GraphFile> files = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode valueNode = root.get("value");

            if (valueNode != null && valueNode.isArray()) {
              for (JsonNode fileNode : valueNode) {

                if (driveId != null && fileNode.get("folder")!= null && fileNode.get("folder").get("childCount").asInt() != 0) {
                  List<GraphFile> oneDrivefiles =  getDriveItemChildren(accessToken, driveId, fileNode.get("id").asText());
                  for (GraphFile file : oneDrivefiles) {
                    VideoFiles videoFiles = new VideoFiles();
                    videoFiles.setFileName(file.getName());
                    videoFiles.setFilePath(file.getWebUrl());
                    videoFiles.setItemID(file.getId());
                    videoFiles.setDriveID(driveId);
                    videoFiles.setFolderName(fileNode.get("name").asText());
                    videoFiles.setFileCount(fileNode.get("folder").get("childCount").asInt());
                    videoFiles.setFolderid(fileNode.get("id").asText());
                    repository.save(videoFiles);
                  }
                }
                else {
                  GraphFile file = new GraphFile();
                  file.setId(fileNode.get("id").asText());
                  file.setName(fileNode.get("name").asText());


                  if (fileNode.has("webUrl")) {
                    file.setWebUrl(fileNode.get("webUrl").asText());
                  }

                  if (fileNode.has("size")) {
                    file.setSize(fileNode.get("size").asLong());
                  }

                  if (fileNode.has("createdDateTime")) {
                    file.setCreatedDateTime(fileNode.get("createdDateTime").asText());
                  }

                  if (fileNode.has("lastModifiedDateTime")) {
                    file.setLastModifiedDateTime(fileNode.get("lastModifiedDateTime").asText());
                  }

                  if (fileNode.has("@microsoft.graph.downloadUrl")) {
                    file.setDownloadUrl(fileNode.get("@microsoft.graph.downloadUrl").asText());
                  }
                  if(fileNode.has("parentReference"))
                  {
                    file.setFolderName(fileNode.get("parentReference").get("name").asText());
                    file.setFolderId(fileNode.get("parentReference").get("id").asText());
                  }

                  files.add(file);
                }
              }

            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse files response: " + e.getMessage());
        }

        return files;
    }


  public TrainingMaster getCategoryFromFileForTrainingMaster(String fileName, String driveID, String itemID, String accessToken) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      Map<String, String> body =  Map.of(
      "driveId", driveID,
      "itemId", itemID,
      "filename",fileName);

      HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

      ResponseEntity<TrainingMaster> response = restTemplate.postForEntity(targetUrl, requestEntity, TrainingMaster.class);

      return  response.getBody();
    } catch (Exception e) {
      throw new RuntimeException("Error forwarding file", e);
    }
  }

  public TrainingDetails getCategoryFromFileForTrainingDetails(String fileName, String driveID, String itemID, String accessToken) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      Map<String, String> body =  Map.of(
        "driveId", driveID,
        "itemId", itemID,
        "filename",fileName);

      HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

      ResponseEntity<TrainingDetails> response = restTemplate.postForEntity(targetUrl, requestEntity, TrainingDetails.class);

      return  response.getBody();
    } catch (Exception e) {
      throw new RuntimeException("Error forwarding file", e);
    }
  }
}
