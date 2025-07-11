package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class RegisterController {

    // --- FXML Components ---
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
    @FXML private Hyperlink signInLink; // Liên kết để quay lại màn hình Đăng nhập

    /**
     * Phương thức này được gọi tự động khi FXML được tải.
     * Dùng để thiết lập các listener và binding ban đầu.
     */
    public void initialize() {
        // Đồng bộ hóa nội dung giữa ô mật khẩu ẩn và hiện
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Thêm listener cho checkbox "Hiện mật khẩu"
        showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            passwordField.setVisible(!newVal);
            visiblePasswordField.setVisible(newVal);
        });

        // Thêm listener để cập nhật thanh báo độ mạnh mật khẩu khi người dùng gõ
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
        });
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Tạo tài khoản".
     */
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        // Lấy dữ liệu từ các ô nhập liệu
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // --- Client-side Validation ---
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Vui lòng điền đầy đủ các trường bắt buộc.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }
        if (!tosCheckBox.isSelected()) {
            showError("Bạn phải đồng ý với Điều khoản Dịch vụ.");
            return;
        }

        // Vô hiệu hóa nút bấm để tránh double-click và hiển thị trạng thái chờ
        registerButton.setDisable(true);
        registerButton.setText("Đang đăng ký...");
        errorLabel.setVisible(false);

        // Tạo một Task để gọi API trên luồng nền, tránh làm treo giao diện
        Task<String> registerTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Gọi API đăng ký và trả về thông báo thành công
                return ApiClient.getInstance().register(username, email, password);
            }
        };

        // Xử lý khi Task thành công
        registerTask.setOnSucceeded(e -> Platform.runLater(() -> {
            // Hiển thị một cửa sổ thông báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Đăng ký thành công");
            alert.setHeaderText(null);
            alert.setContentText("Tài khoản của bạn đã được tạo. Hãy đăng nhập để bắt đầu!");
            alert.showAndWait(); // Chờ người dùng nhấn OK

            // Sau khi người dùng nhấn OK, chuyển về màn hình đăng nhập
            try {
                SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
            } catch (IOException ioException) {
                ioException.printStackTrace();
                showError("Lỗi chuyển màn hình.");
            }
        }));

        // Xử lý khi Task thất bại (ví dụ: email đã tồn tại)
        registerTask.setOnFailed(e -> Platform.runLater(() -> {
            showError(registerTask.getException().getMessage());
            // Kích hoạt lại nút bấm để người dùng thử lại
            registerButton.setDisable(false);
            registerButton.setText("Tạo tài khoản");
        }));

        // Bắt đầu chạy Task trên một luồng mới
        new Thread(registerTask).start();
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn vào liên kết "Đã có tài khoản? Đăng nhập".
     */
    @FXML
    private void handleSignInLinkAction(ActionEvent event) {
        try {
            SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi không thể tải màn hình đăng nhập.");
        }
    }

    /**
     * Cập nhật giao diện của ProgressBar dựa trên độ phức tạp của mật khẩu.
     */
    private void updatePasswordStrength(String password) {
        double strength = 0;
        // Xóa các lớp CSS cũ để tránh chồng chéo
        passwordStrengthBar.getStyleClass().removeAll("weak", "medium", "strong");

        if (password.length() >= 8) strength += 0.3;
        if (password.matches(".*[A-Z].*")) strength += 0.2; // Chứa chữ hoa
        if (password.matches(".*[a-z].*")) strength += 0.2; // Chứa chữ thường
        if (password.matches(".*[0-9].*")) strength += 0.2; // Chứa số
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength += 0.1; // Chứa ký tự đặc biệt

        passwordStrengthBar.setProgress(strength);

        // Thêm lớp CSS mới tương ứng với độ mạnh
        if (strength < 0.4) {
            passwordStrengthBar.getStyleClass().add("weak");
        } else if (strength < 0.8) {
            passwordStrengthBar.getStyleClass().add("medium");
        } else {
            passwordStrengthBar.getStyleClass().add("strong");
        }
    }

    /**
     * Hiển thị một thông báo lỗi trên giao diện.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}