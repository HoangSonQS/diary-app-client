package com.mydiary.diaryappclient.service;

public class AuthService {
    private static AuthService instance;
    private String authToken;
    private String username;

    // Singleton Pattern
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    public void clearAuthToken() {
        this.authToken = null;
        this.username = null;
    }
}