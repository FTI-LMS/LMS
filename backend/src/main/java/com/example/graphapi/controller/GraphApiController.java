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
@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Map<String, Object> response = new HashMap<>();

        try {
            // First validate the token
            if (!graphApiService.validateToken("eyJ0eXAiOiJKV1QiLCJub25jZSI6InkzcHR2WWhBTkIyZTlMeURaRF9OamhpVmJUbTdTbTA4ei1LdEdIak9EYVEiLCJhbGciOiJSUzI1NiIsIng1dCI6IkNOdjBPSTNSd3FsSEZFVm5hb01Bc2hDSDJYRSIsImtpZCI6IkNOdjBPSTNSd3FsSEZFVm5hb01Bc2hDSDJYRSJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zZDI1NTVkOS1mNTZiLTQ2NmUtYmViYy1mMzU0YzBiZGQ5YjQvIiwiaWF0IjoxNzUwNjA5MjQ4LCJuYmYiOjE3NTA2MDkyNDgsImV4cCI6MTc1MDYxNDQ4NCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFVUUF1LzhaQUFBQXBYb1g0Mk0xbVE0OHViRnNGSVowQmpPSHhaODdCRWZzVDBsVnlKVEFJR3lROGdTWFdEWkxKZXFJY3N1cXVLelJRTUg4d1Y0TDg1eVlMQlRHVjhlbmlBPT0iLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6IlNoYXJlcG9pbnQgR3ByYXBoIEFQSSIsImFwcGlkIjoiNjBiY2VjMDctZWM2Ni00MmVkLTg2MmEtY2U2MzQyOGZkMzg2IiwiYXBwaWRhY3IiOiIwIiwiaWR0eXAiOiJ1c2VyIiwiaXBhZGRyIjoiNDkuNDcuNzEuMTg4IiwibmFtZSI6IlJhamliIEIiLCJvaWQiOiJhZmU0N2RlOC1jZTgxLTQ5MTgtOTk3OC1hODA3NjMxYjA0OTUiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzIwMDRDNTk0M0JEQyIsInJoIjoiMS5BY1lBMlZVbFBXdjFia2EtdlBOVXdMM1p0QU1BQUFBQUFBQUF3QUFBQUFBQUFBRHBBTm5HQUEuIiwic2NwIjoiQWNjZXNzUmV2aWV3LlJlYWQuQWxsIEFjY2Vzc1Jldmlldy5SZWFkV3JpdGUuQWxsIERpcmVjdG9yeS5BY2Nlc3NBc1VzZXIuQWxsIERpcmVjdG9yeS5SZWFkLkFsbCBEaXJlY3RvcnkuUmVhZFdyaXRlLkFsbCBVc2VyLlJlYWQgcHJvZmlsZSBvcGVuaWQgZW1haWwiLCJzaWQiOiIwMDVlZjBjOS00NGJmLTA3MjgtZTAwNC01NWVkNTYwMzkxMTgiLCJzdWIiOiJkanVrcnlHejZYU2RickZaaU1LTXBqTnMtMXBCaFJVeEdSSkJMVllNSnlnIiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkFTIiwidGlkIjoiM2QyNTU1ZDktZjU2Yi00NjZlLWJlYmMtZjM1NGMwYmRkOWI0IiwidW5pcXVlX25hbWUiOiJSYWppYmlyaXNAYW5raXQ1MTlnbWFpbC5vbm1pY3Jvc29mdC5jb20iLCJ1cG4iOiJSYWppYmlyaXNAYW5raXQ1MTlnbWFpbC5vbm1pY3Jvc29mdC5jb20iLCJ1dGkiOiI2YXUwbFlWTkUwcVFUNFdDZFY1OUFBIiwidmVyIjoiMS4wIiwid2lkcyI6WyJiNzlmYmY0ZC0zZWY5LTQ2ODktODE0My03NmIxOTRlODU1MDkiXSwieG1zX2Z0ZCI6Ikt3cGxFZTBaQWUtMDBxaVBuMjZuSHRYRUJQUjhUTGRDdXdoOGJRODBJcXNCYTI5eVpXRnpiM1YwYUMxa2MyMXoiLCJ4bXNfaWRyZWwiOiIyNiAxIiwieG1zX3N0Ijp7InN1YiI6IkczdW1iSWJ4OUVDbm9nZGRjaGF5WFR6NFEyTW5Fak1wQjdKMFBOQTNoLW8ifSwieG1zX3RjZHQiOjE3NTA1NjIxNzd9.Xkevru8Ly_O43NOBCfOrba22UtUqLOVrkmoLjevaxfEvOk0fnH9UkkG3eEKTqOiKuWeakZBVczH2j75EOEcBeKgQ-asJH9kjaPJCTqq2G4Ho8iswgx0tJaVOKc4A1ZqC3KvW-J-8FRpdh7hMTvnIDh0X98CiXVFj8kxnyW_NcH7cACUMjtwLjfYKD3Jt2Vnu7w3sYmAVQMJkqJEsR1rDSG_QN12uZwTKFmx_9foI-aheIuIoePXElnMAEzl20M0STQXWi1coHqdUd3UDiRAh9kDoBt6ZuobCZNVCpKBLW6fzGsrE3mqKui9QQO2oqh8NTFrDONFmAD0ChepEqKjWMA")) {
                response.put("error", "Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            JsonNode userInfo = graphApiService.getUserInfo("eyJ0eXAiOiJKV1QiLCJub25jZSI6InkzcHR2WWhBTkIyZTlMeURaRF9OamhpVmJUbTdTbTA4ei1LdEdIak9EYVEiLCJhbGciOiJSUzI1NiIsIng1dCI6IkNOdjBPSTNSd3FsSEZFVm5hb01Bc2hDSDJYRSIsImtpZCI6IkNOdjBPSTNSd3FsSEZFVm5hb01Bc2hDSDJYRSJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zZDI1NTVkOS1mNTZiLTQ2NmUtYmViYy1mMzU0YzBiZGQ5YjQvIiwiaWF0IjoxNzUwNjA5MjQ4LCJuYmYiOjE3NTA2MDkyNDgsImV4cCI6MTc1MDYxNDQ4NCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFVUUF1LzhaQUFBQXBYb1g0Mk0xbVE0OHViRnNGSVowQmpPSHhaODdCRWZzVDBsVnlKVEFJR3lROGdTWFdEWkxKZXFJY3N1cXVLelJRTUg4d1Y0TDg1eVlMQlRHVjhlbmlBPT0iLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6IlNoYXJlcG9pbnQgR3ByYXBoIEFQSSIsImFwcGlkIjoiNjBiY2VjMDctZWM2Ni00MmVkLTg2MmEtY2U2MzQyOGZkMzg2IiwiYXBwaWRhY3IiOiIwIiwiaWR0eXAiOiJ1c2VyIiwiaXBhZGRyIjoiNDkuNDcuNzEuMTg4IiwibmFtZSI6IlJhamliIEIiLCJvaWQiOiJhZmU0N2RlOC1jZTgxLTQ5MTgtOTk3OC1hODA3NjMxYjA0OTUiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzIwMDRDNTk0M0JEQyIsInJoIjoiMS5BY1lBMlZVbFBXdjFia2EtdlBOVXdMM1p0QU1BQUFBQUFBQUF3QUFBQUFBQUFBRHBBTm5HQUEuIiwic2NwIjoiQWNjZXNzUmV2aWV3LlJlYWQuQWxsIEFjY2Vzc1Jldmlldy5SZWFkV3JpdGUuQWxsIERpcmVjdG9yeS5BY2Nlc3NBc1VzZXIuQWxsIERpcmVjdG9yeS5SZWFkLkFsbCBEaXJlY3RvcnkuUmVhZFdyaXRlLkFsbCBVc2VyLlJlYWQgcHJvZmlsZSBvcGVuaWQgZW1haWwiLCJzaWQiOiIwMDVlZjBjOS00NGJmLTA3MjgtZTAwNC01NWVkNTYwMzkxMTgiLCJzdWIiOiJkanVrcnlHejZYU2RickZaaU1LTXBqTnMtMXBCaFJVeEdSSkJMVllNSnlnIiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkFTIiwidGlkIjoiM2QyNTU1ZDktZjU2Yi00NjZlLWJlYmMtZjM1NGMwYmRkOWI0IiwidW5pcXVlX25hbWUiOiJSYWppYmlyaXNAYW5raXQ1MTlnbWFpbC5vbm1pY3Jvc29mdC5jb20iLCJ1cG4iOiJSYWppYmlyaXNAYW5raXQ1MTlnbWFpbC5vbm1pY3Jvc29mdC5jb20iLCJ1dGkiOiI2YXUwbFlWTkUwcVFUNFdDZFY1OUFBIiwidmVyIjoiMS4wIiwid2lkcyI6WyJiNzlmYmY0ZC0zZWY5LTQ2ODktODE0My03NmIxOTRlODU1MDkiXSwieG1zX2Z0ZCI6Ikt3cGxFZTBaQWUtMDBxaVBuMjZuSHRYRUJQUjhUTGRDdXdoOGJRODBJcXNCYTI5eVpXRnpiM1YwYUMxa2MyMXoiLCJ4bXNfaWRyZWwiOiIyNiAxIiwieG1zX3N0Ijp7InN1YiI6IkczdW1iSWJ4OUVDbm9nZGRjaGF5WFR6NFEyTW5Fak1wQjdKMFBOQTNoLW8ifSwieG1zX3RjZHQiOjE3NTA1NjIxNzd9.Xkevru8Ly_O43NOBCfOrba22UtUqLOVrkmoLjevaxfEvOk0fnH9UkkG3eEKTqOiKuWeakZBVczH2j75EOEcBeKgQ-asJH9kjaPJCTqq2G4Ho8iswgx0tJaVOKc4A1ZqC3KvW-J-8FRpdh7hMTvnIDh0X98CiXVFj8kxnyW_NcH7cACUMjtwLjfYKD3Jt2Vnu7w3sYmAVQMJkqJEsR1rDSG_QN12uZwTKFmx_9foI-aheIuIoePXElnMAEzl20M0STQXWi1coHqdUd3UDiRAh9kDoBt6ZuobCZNVCpKBLW6fzGsrE3mqKui9QQO2oqh8NTFrDONFmAD0ChepEqKjWMA");
            response.put("userInfo", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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

   
}