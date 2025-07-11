package com.mydiary.diaryappclient.service;

public class AuthService {
    private static AuthService instance;
    private String authToken;

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

    public boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    public void clearAuthToken() {
        this.authToken = null;
    }
}