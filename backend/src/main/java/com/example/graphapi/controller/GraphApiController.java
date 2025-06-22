
package com.example.graphapi.controller;

import com.example.graphapi.model.AuthRequest;
import com.example.graphapi.model.GraphFile;
import com.example.graphapi.service.GraphApiService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = {"http://localhost:4200", "https://*.replit.dev", "https://*.replit.co"})
public class GraphApiController {
    
    private final GraphApiService graphApiService;
    
    public GraphApiController(GraphApiService graphApiService) {
        this.graphApiService = graphApiService;
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody AuthRequest authRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = graphApiService.validateToken(authRequest.getAccessToken());
            response.put("valid", isValid);
            
            if (isValid) {
                JsonNode userInfo = graphApiService.getUserInfo(authRequest.getAccessToken());
                response.put("userInfo", userInfo);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/files")
    public ResponseEntity<Map<String, Object>> getAllFiles(@RequestBody AuthRequest authRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // First validate the token
            if (!graphApiService.validateToken(authRequest.getAccessToken())) {
                response.put("error", "Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            List<GraphFile> files = graphApiService.getAllFiles(authRequest.getAccessToken());
            response.put("files", files);
            response.put("count", files.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/files/recent")
    public ResponseEntity<Map<String, Object>> getRecentFiles(
            @RequestBody AuthRequest authRequest,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // First validate the token
            if (!graphApiService.validateToken(authRequest.getAccessToken())) {
                response.put("error", "Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            List<GraphFile> files = graphApiService.getRecentFiles(authRequest.getAccessToken(), limit);
            response.put("files", files);
            response.put("count", files.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestBody AuthRequest authRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // First validate the token
            if (!graphApiService.validateToken(authRequest.getAccessToken())) {
                response.put("error", "Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            JsonNode userInfo = graphApiService.getUserInfo(authRequest.getAccessToken());
            response.put("userInfo", userInfo);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
