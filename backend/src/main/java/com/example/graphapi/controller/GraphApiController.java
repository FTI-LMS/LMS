
package com.example.graphapi.controller;

import com.example.graphapi.entity.CategorayDetails;
import com.example.graphapi.entity.VideoFile;
import com.example.graphapi.model.AuthRequest;
import com.example.graphapi.model.GraphFile;
import com.example.graphapi.repository.CategoryDetailsRepository;
import com.example.graphapi.repository.VideoFileRepository;
import com.example.graphapi.service.GraphApiService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.nd4j.shade.j2objc.annotations.AutoreleasePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GraphApiController {

    private final GraphApiService graphApiService;

    @Autowired
    private  CategoryDetailsRepository categoryDetailsRepository;
    @Autowired
    private  VideoFileRepository repository;

    public GraphApiController(GraphApiService graphApiService) {
        this.graphApiService = graphApiService;
    }

    @Operation(
        summary = "Health Check",
        description = "Simple health check endpoint to verify the API is running"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("service", "Microsoft Graph API Backend");
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get User Info",
        description = "Retrieves user information from Microsoft Graph using a test token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User info retrieved successfully",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Invalid token",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Map<String, Object> response = new HashMap<>();

        try {
            // This is a test endpoint - in production you'd get the token from the request
            String testToken = "test_token"; // This will fail but shows the endpoint structure
            JsonNode userInfo = graphApiService.getUserInfo(testToken);

            if (userInfo != null) {
                response.put("userInfo", userInfo);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to retrieve user info");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("error", "Test endpoint - requires valid Microsoft Graph token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
        summary = "Validate Azure AD Token",
        description = "Validates an Azure AD access token and returns user information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Invalid token",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(
            @Parameter(description = "Request containing Azure AD access token", required = true)
            @RequestBody AuthRequest authRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isValid = graphApiService.validateToken(authRequest.getAccessToken());
            response.put("valid", isValid);

            if (isValid) {
                // If token is valid, also get user info
                JsonNode userInfo = graphApiService.getUserInfo(authRequest.getAccessToken());
                response.put("userInfo", userInfo);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("error", "Token validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Get OneDrive Files",
        description = "Retrieves all files from the user's OneDrive root directory"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files retrieved successfully",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Invalid token",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/files")
    public ResponseEntity<Map<String, Object>> getAllFiles(
            @Parameter(description = "Request containing Azure AD access token", required = true)
            @RequestBody AuthRequest authRequest) {
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
            response.put("error", "Failed to retrieve files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Get Recent OneDrive Files",
        description = "Retrieves recent files from the user's OneDrive"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent files retrieved successfully",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Invalid token",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/files/recent")
    public ResponseEntity<Map<String, Object>> getRecentFiles(
            @Parameter(description = "Request containing Azure AD access token", required = true)
            @RequestBody AuthRequest authRequest,
            @Parameter(description = "Number of recent files to retrieve (default: 10)")
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
            response.put("limit", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to retrieve recent files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
            summary = "Get Drive Item Children",
            description = "Retrieves children of a specific drive item using drive ID and item ID"
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Drive item children retrieved successfully",
                        content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Invalid token",
                        content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                        content = @Content(mediaType = "application/json"))
        })
        @PostMapping("/drives/{driveId}/items/{itemId}/children")
        public ResponseEntity<Map<String, Object>> getDriveItemChildren(
                @Parameter(description = "Drive ID", required = true, example = "b!h-u0gl1vu0mtETS610zX9hufYTIu9hZJjUnn3YdGkBYfslJiWknLTrSquiV92Sgm")
                @PathVariable String driveId,
                @Parameter(description = "Item ID", required = true, example = "015ZUXCKD3HEDKB5ZQHFF2CAMHKFSS7WBU")
                @PathVariable String itemId,
                @Parameter(description = "Request containing Azure AD access token", required = true)
                @RequestBody AuthRequest authRequest) {
            Map<String, Object> response = new HashMap<>();

            try {
                // First validate the token
                if (!graphApiService.validateToken(authRequest.getAccessToken())) {
                    response.put("error", "Invalid access token");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                List<GraphFile> files = graphApiService.getDriveItemChildren(authRequest.getAccessToken(), driveId, "015ZUXCKD3HEDKB5ZQHFF2CAMHKFSS7WBU");
              for (GraphFile file : files) {
                VideoFile videoFile = new VideoFile();
                  videoFile.setFileName(file.getName());
                  videoFile.setFilePath(file.getWebUrl());
                  videoFile.setItemID(file.getId());
                  videoFile.setDriveID(driveId);
                  repository.save(videoFile);
                }

                List<VideoFile> fileList =  fileList = repository.findAll();
                fileList.stream().forEach(vi->{
                CategorayDetails categorayDetails = new CategorayDetails();
                String details= graphApiService.getCategoryFromFile(vi.getFileName(),vi.getDriveID(),vi.getItemID(),authRequest.getAccessToken());
                //Check the return type and add set the values in category Details.
                categoryDetailsRepository.save(categorayDetails);
              });

                response.put("files", files);
                response.put("count", files.size());
                response.put("driveId", driveId);
                response.put("itemId", itemId);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("error", "Failed to retrieve drive item children: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

 /* @GetMapping("/getCategory")
  public ResponseEntity<String> getCategrories() {


    String response = "";
    List<VideoFile> fileList =  fileList = repository.findAll();
    fileList.stream().forEach(vi->{
      CategorayDetails categorayDetails = new CategorayDetails();
      graphApiService.getCategoryFromFile(vi.getFileName(),vi.getDriveID(),vi.getItemID());
      categoryDetailsRepository.save(categorayDetails);
     });

       return ResponseEntity.ok(response);
  }*/

 }
