package com.example.graphapi.controller;

import com.example.graphapi.model.AuthRequest;
import com.example.graphapi.model.GraphFile;
import com.example.graphapi.service.GraphApiService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(originPatterns = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Get Recent Files",
        description = "Retrieves recently accessed files from the user's OneDrive"
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
            @Parameter(description = "Maximum number of files to return", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Request containing Azure AD access token", required = true)
            @RequestBody AuthRequest authRequest) {
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