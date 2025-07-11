package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.service.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private CheckBox tosCheckBox;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    public void initialize() {
        // Logic hiện/ẩn mật khẩu
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            passwordField.setVisible(!newVal);
            visiblePasswordField.setVisible(newVal);
        });

        // Logic kiểm tra độ mạnh mật khẩu
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
        });
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        // Lấy dữ liệu từ các trường
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate dữ liệu
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }
        if (!tosCheckBox.isSelected()) {
            showError("Bạn phải đồng ý với Điều khoản Dịch vụ.");
            return;
        }
        // ... các validate khác (email, độ dài mật khẩu)

        registerButton.setDisable(true);
        registerButton.setText("Đang đăng ký...");
        errorLabel.setVisible(false);

        // Tạo Task để gọi API đăng ký
        Task<String> registerTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return ApiClient.getInstance().register(username, email, password);
            }
        };

        registerTask.setOnSucceeded(e -> Platform.runLater(() -> {
            // TODO: Chuyển sang màn hình đăng nhập với thông báo thành công
            System.out.println("Đăng ký thành công!");
            registerButton.setDisable(false);
            registerButton.setText("Tạo tài khoản");
        }));

        registerTask.setOnFailed(e -> Platform.runLater(() -> {
            showError(registerTask.getException().getMessage());
            registerButton.setDisable(false);
            registerButton.setText("Tạo tài khoản");
        }));

        new Thread(registerTask).start();
    }

    private void updatePasswordStrength(String password) {
        double strength = 0;
        // Xóa các lớp CSS cũ
        passwordStrengthBar.getStyleClass().removeAll("weak", "medium", "strong");

        if (password.length() >= 8) strength += 0.3;
        if (password.matches(".*[A-Z].*")) strength += 0.2;
        if (password.matches(".*[a-z].*")) strength += 0.2;
        if (password.matches(".*[0-9].*")) strength += 0.2;
        if (password.matches(".*[!@#$%^&*()].*")) strength += 0.1;

        passwordStrengthBar.setProgress(strength);

        // Thêm lớp CSS mới dựa trên độ mạnh
        if (strength < 0.4) {
            passwordStrengthBar.getStyleClass().add("weak");
        } else if (strength < 0.8) {
            passwordStrengthBar.getStyleClass().add("medium");
        } else {
            passwordStrengthBar.getStyleClass().add("strong");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}