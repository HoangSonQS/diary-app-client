package com.mydiary.diaryappclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydiary.diaryappclient.model.domain.Entry;
import com.mydiary.diaryappclient.model.dto.AuthResponse;
import com.mydiary.diaryappclient.model.dto.HasPinResponse;
import com.mydiary.diaryappclient.model.dto.LoginRequest;
import com.mydiary.diaryappclient.model.dto.RegisterRequest;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.List;

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

    public static class SetPinRequest {
        public String pin;
        public SetPinRequest(String pin) {
            this.pin = pin;
        }
    }

    // Constructor được sửa đổi để tự động thêm token vào mỗi request
    private ApiClient() {
        // --- BẮT ĐẦU THAY ĐỔI ---
        // 1. Tạo một logger
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log cả body và header

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // 2. Thêm Interceptor thêm token của chúng ta
        builder.addInterceptor(chain -> {
            Request originalRequest = chain.request();
            Request.Builder newRequestBuilder = originalRequest.newBuilder();
            String token = AuthService.getInstance().getAuthToken();
            if (token != null && !token.isEmpty()) {
                System.out.println("DEBUG: Tìm thấy token, đang thêm vào header...");
                newRequestBuilder.header("Authorization", "Bearer " + token);
            } else {
                System.out.println("DEBUG: Không tìm thấy token trong AuthService.");
            }
            return chain.proceed(newRequestBuilder.build());
        });

        // 3. Thêm logger vào cuối cùng để nó in ra request cuối cùng
        builder.addInterceptor(loggingInterceptor);

        this.client = builder.build();
        // --- KẾT THÚC THAY ĐỔI ---
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

    public static class PinLoginRequest {
        public String usernameOrEmail;
        public String pin;
        public PinLoginRequest(String usernameOrEmail, String pin) {
            this.usernameOrEmail = usernameOrEmail;
            this.pin = pin;
        }
    }

    // Phương thức mới
    public AuthResponse pinLogin(String username, String pin) throws IOException {
        PinLoginRequest pinLoginRequest = new PinLoginRequest(username, pin);
        String jsonBody = objectMapper.writeValueAsString(pinLoginRequest);
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/login-pin") // Endpoint mới
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Đăng nhập bằng PIN thất bại: " + response.body().string());
            }
            return objectMapper.readValue(response.body().string(), AuthResponse.class);
        }
    }

    public void setPin(String pin) throws IOException {
        SetPinRequest setPinRequest = new SetPinRequest(pin);
        String jsonBody = objectMapper.writeValueAsString(setPinRequest);
        RequestBody body = RequestBody.create(jsonBody, JSON);

        // Endpoint này yêu cầu phải xác thực, Interceptor sẽ tự thêm token
        Request request = new Request.Builder()
                .url(BASE_URL + "/user/set-pin")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final String responseBody = response.body().string();
            System.out.println("DEBUG setPin API: Status Code = " + response.code());
            System.out.println("DEBUG setPin API: Response Body = " + responseBody);
            if (!response.isSuccessful()) {
                throw new IOException("Không thể thiết lập PIN: " + response.body().string());
            }
            // Không cần trả về gì cả, 200 OK là thành công
        }
    }
    public boolean hasPin(String username) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/users/" + username + "/has-pin")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Ném lỗi nếu không thể kiểm tra (ví dụ: user không tồn tại)
                throw new IOException("Không thể kiểm tra PIN cho người dùng: " + username);
            }
            HasPinResponse hasPinResponse = objectMapper.readValue(response.body().string(), HasPinResponse.class);
            return hasPinResponse.getHasPin();
        }
    }

    public List<Entry> getEntries() throws IOException {
        // Endpoint này yêu-cầu xác-thực, Interceptor sẽ tự-động thêm token
        Request request = new Request.Builder()
                .url(BASE_URL + "/entries")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tải danh-sách nhật-ký: " + response.body().string());
            }

            // Dùng TypeReference để Jackson hiểu cần chuyển đổi JSON array thành List<Entry>
            return objectMapper.readValue(response.body().string(), new TypeReference<List<Entry>>() {});
        }
    }
}