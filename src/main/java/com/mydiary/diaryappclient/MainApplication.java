package com.mydiary.diaryappclient;

import com.mydiary.diaryappclient.util.SceneManager; // 1. Import lớp SceneManager
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 2. Gán cửa sổ chính (Stage) cho SceneManager để nó có thể điều khiển
        SceneManager.setPrimaryStage(stage);

        // 3. Sử dụng SceneManager để tải và hiển thị màn hình đầu tiên
        //    Thao tác này thay thế hoàn toàn cho việc tạo FXMLLoader và Scene thủ công
        SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
    }

    public static void main(String[] args) {
        launch();
    }
}