package com.mydiary.diaryappclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydiary.diaryappclient.model.AuthResponse;
import com.mydiary.diaryappclient.model.LoginRequest;
import com.mydiary.diaryappclient.model.RegisterRequest;
import okhttp3.*;

import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api/auth";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static ApiClient instance;

    // Singleton Pattern
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    /**
     * Gọi API đăng nhập
     * @return AuthResponse chứa token
     * @throws IOException nếu có lỗi mạng hoặc lỗi server
     */
    public AuthResponse login(String username, String password) throws IOException {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String jsonBody = objectMapper.writeValueAsString(loginRequest);
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Ném lỗi với thông điệp từ server nếu có
                throw new IOException("Lỗi đăng nhập: " + response.body().string());
            }
            // Chuyển đổi JSON response thành đối tượng AuthResponse
            return objectMapper.readValue(response.body().string(), AuthResponse.class);
        }
    }

    /**
     * Gọi API đăng ký
     * @return String thông báo thành công từ server
     * @throws IOException nếu có lỗi mạng hoặc lỗi server
     */
    public String register(String username, String email, String password) throws IOException {
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        String jsonBody = objectMapper.writeValueAsString(registerRequest);
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/register")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Lỗi đăng ký: " + response.body().string());
            }
            return response.body().string();
        }
    }
}