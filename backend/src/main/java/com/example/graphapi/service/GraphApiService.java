
package com.example.graphapi.service;

import com.example.graphapi.model.GraphFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
