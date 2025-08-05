package com.example.graphapi.service;

import com.example.graphapi.model.AuthRequest;
import com.example.graphapi.model.GraphFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GraphApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

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
                return parseFilesFromResponse(response.getBody());
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
                return parseFilesFromResponse(response.getBody());
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
                return parseFilesFromResponse(response.getBody());
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

    private List<GraphFile> parseFilesFromResponse(String responseBody) {
        List<GraphFile> files = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode valueNode = root.get("value");

            if (valueNode != null && valueNode.isArray()) {
                for (JsonNode fileNode : valueNode) {
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

                    files.add(file);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse files response: " + e.getMessage());
        }

        return files;
    }
}
