
package com.example.graphapi.model;

public class AuthRequest {
    private String accessToken;

    public AuthRequest() {}

    public AuthRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
