package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.controller.components.PinFieldController;
import com.mydiary.diaryappclient.model.AuthResponse;
import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import com.mydiary.diaryappclient.util.SecurityUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class EnterPinController {

    @FXML private Label errorLabel;
    @FXML private Button unlockButton;

    private String username;

    // Inject controller của component con, không inject PasswordField cũ nữa
    @FXML private PinFieldController pinFieldComponentController;

    @FXML
    public void initialize() {
        // Đăng ký hành động tự động gửi khi nhập đủ 6 số
        if (pinFieldComponentController != null) {
            pinFieldComponentController.setOnPinComplete(() -> unlockButton.fire());
        }
    }

    public void initData(String username) {
        this.username = username;
        // Có thể thêm một Label trên giao diện để hiển thị "Đăng nhập cho: [username]" nếu muốn
    }

    @FXML
    void handleUnlockAction(ActionEvent event) {
        String pin = pinFieldComponentController.getPin();

        if (pin.isEmpty() || this.username == null || this.username.isEmpty()) {
            showError("Lỗi: Không có thông tin người dùng hoặc PIN.");
            return;
        }

        unlockButton.setDisable(true);
        unlockButton.setText("Đang xử lý...");
        errorLabel.setVisible(false);

        // Tạo Task để gọi API đăng nhập bằng PIN
        Task<AuthResponse> pinLoginTask = new Task<>() {
            @Override
            protected AuthResponse call() throws Exception {
                // Sử dụng username đã được lưu và PIN người dùng nhập
                return ApiClient.getInstance().pinLogin(username, pin);
            }
        };

        pinLoginTask.setOnSucceeded(e -> {
            AuthResponse authResponse = pinLoginTask.getValue();
            AuthService.getInstance().setAuthToken(authResponse.getToken());
            AuthService.getInstance().setUsername(this.username); // Lưu lại username vào session

            Platform.runLater(() -> {
                try {
                    SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Lỗi tải màn hình chính.");
                }
            });
        });

        pinLoginTask.setOnFailed(e -> {
            Throwable exception = pinLoginTask.getException();
            Platform.runLater(() -> {
                showError(exception.getMessage());
                pinFieldComponentController.clear(); // Xóa PIN sai để người dùng nhập lại
                unlockButton.setDisable(false);
                unlockButton.setText("Mở khóa");
            });
        });

        new Thread(pinLoginTask).start();
    }

    @FXML
    void handleForgotPasswordLink(ActionEvent event) {
        try {
            // Sửa tên phương thức ở đây
            CredentialManager.deleteLastUser();
            SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi khi chuyển màn hình.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}