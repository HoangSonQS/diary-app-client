package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class LoginController {

    // Liên kết các thành phần từ file FXML
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signUpLink;

    // Phương thức này được tự động gọi sau khi FXML được tải
    public void initialize() {
        // Đồng bộ hóa nội dung giữa ô mật khẩu và ô văn bản
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Xử lý logic cho checkbox "Hiện mật khẩu"
        showPasswordCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            passwordField.setVisible(!isNowSelected);
            visiblePasswordField.setVisible(isNowSelected);
        });
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Tên đăng nhập và mật khẩu không được để trống.");
            return;
        }

        // Tắt nút đăng nhập và hiển thị trạng thái chờ
        loginButton.setDisable(true);
        loginButton.setText("Đang xử lý...");
        errorLabel.setVisible(false);

        // Tạo một Task để thực hiện cuộc gọi API trên một luồng nền
        // Tránh làm đóng băng giao diện người dùng
        Task<String> loginTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Gọi API
                ApiClient.getInstance().login(username, password);
                // Có thể lưu token ở đây nếu cần
                return "Đăng nhập thành công!";
            }
        };

        loginTask.setOnSucceeded(e -> {
            // Khi thành công, chuyển sang màn hình chính
            System.out.println(loginTask.getValue());
            // TODO: Chuyển sang màn hình chính (main-view.fxml)
        });

        loginTask.setOnFailed(e -> {
            // Khi thất bại, hiển thị lỗi
            Throwable exception = loginTask.getException();
            showError(exception.getMessage());
        });

        loginTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    // Chuyển sang màn hình chính sau khi đăng nhập thành công
                    SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    showError("Không thể tải màn hình chính.");
                    // Bật lại nút bấm ngay cả khi chuyển scene lỗi
                    loginButton.setDisable(false);
                    loginButton.setText("Đăng nhập");
                }
                // Không cần bật lại nút ở đây vì đã chuyển màn hình
            });
        });

        loginTask.setOnFailed(e -> {
            Throwable exception = loginTask.getException();
            Platform.runLater(() -> {
                showError(exception.getMessage());
                // Bật lại nút bấm khi có lỗi
                loginButton.setDisable(false);
                loginButton.setText("Đăng nhập");
            });
        });

        new Thread(loginTask).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleSignUpLinkAction(ActionEvent event) {
        try {
            SceneManager.switchScene("register-view.fxml", "Tạo tài khoản mới");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Chuyển sang màn hình đăng ký");
    }

}