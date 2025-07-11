package com.mydiary.diaryappclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Cập nhật đường dẫn để trỏ vào thư mục /views/
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/views/register-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Ultimate Diary - Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}