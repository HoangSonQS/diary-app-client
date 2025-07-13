package com.mydiary.diaryappclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydiary.diaryappclient.model.AuthResponse;
import com.mydiary.diaryappclient.model.LoginRequest;
import com.mydiary.diaryappclient.model.RegisterRequest;
import okhttp3.*;

import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static ApiClient instance;

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // Constructor được sửa đổi để tự động thêm token vào mỗi request
    private ApiClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> {
            Request originalRequest = chain.request();
            Request.Builder newRequestBuilder = originalRequest.newBuilder();

            String token = AuthService.getInstance().getAuthToken();
            if (token != null && !token.isEmpty()) {
                newRequestBuilder.header("Authorization", "Bearer " + token);
            }

            return chain.proceed(newRequestBuilder.build());
        });
        this.client = builder.build();
    }

    public AuthResponse login(String username, String password) throws IOException {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String jsonBody = objectMapper.writeValueAsString(loginRequest);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/login")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            // --- BẮT ĐẦU DEBUG ---
            // Đọc nội dung response vào một biến để có thể sử dụng lại,
            // vì response.body().string() chỉ có thể được gọi MỘT LẦN.
            final String responseBody = response.body().string();

            System.out.println("DEBUG API: Status Code = " + response.code());
            System.out.println("DEBUG API: Response Body = " + responseBody);
            // --- KẾT THÚC DEBUG ---

            if (!response.isSuccessful()) {
                // Sử dụng lại responseBody đã đọc
                throw new IOException("Lỗi đăng nhập: " + responseBody);
            }
            // Sử dụng lại responseBody để chuyển đổi
            return objectMapper.readValue(responseBody, AuthResponse.class);
        }
    }

    public String register(String username, String email, String password) throws IOException {
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        String jsonBody = objectMapper.writeValueAsString(registerRequest);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/register")
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