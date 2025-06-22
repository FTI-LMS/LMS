package com.example.graphapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication request containing Azure AD access token")
public class AuthRequest {

    @Schema(description = "Azure AD access token obtained from frontend authentication", 
            example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIs...", 
            required = true)
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}