package com.mydiary.diaryappclient.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    private String token;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}