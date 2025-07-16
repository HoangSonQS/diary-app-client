package com.mydiary.diaryappclient.controller.components;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;

public class PinFieldController { // KHÔNG extends StackPane
    @FXML private HBox visualPinBox;
    @FXML private PasswordField pinField;

    private final Label[] visualPinChars = new Label[6];
    private Runnable onPinComplete;

    // CONSTRUCTOR ĐÃ BỊ XÓA

    @FXML
    public void initialize() {
        for (int i = 0; i < 6; i++) {
            visualPinChars[i] = new Label();
            visualPinChars[i].getStyleClass().add("visual-pin-char");
            visualPinChars[i].setAlignment(Pos.CENTER);
            visualPinChars[i].setPrefSize(40, 40);
            visualPinBox.getChildren().add(visualPinChars[i]);
        }
        pinField.textProperty().addListener((obs, oldVal, newVal) -> updateVisualState(newVal));
        updateVisualState("");
    }

    private void updateVisualState(String pin) {
        int pinLength = pin.length();
        for (int i = 0; i < 6; i++) {
            visualPinChars[i].setText(i < pinLength ? "●" : "");
            visualPinChars[i].getStyleClass().remove("visual-pin-char-focused");
        }
        if (pinLength < 6) {
            visualPinChars[pinLength].getStyleClass().add("visual-pin-char-focused");
        }
        if (pinLength == 6 && onPinComplete != null) {
            Platform.runLater(onPinComplete);
        }
    }

    public void setOnPinComplete(Runnable onPinComplete) {
        this.onPinComplete = onPinComplete;
    }

    public String getPin() {
        return pinField.getText();
    }

    public void clear() {
        pinField.clear();
    }
}