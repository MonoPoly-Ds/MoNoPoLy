package client.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LogPanel extends VBox {
    private TextArea logArea;

    public LogPanel() {
        this.setPadding(new Insets(10));
        this.setSpacing(10);
        this.setPrefWidth(250);
        this.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 1 0 0;");

        Label title = new Label("GAME LOG");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        title.setTextFill(Color.DARKSLATEGRAY);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(600);
        logArea.setFont(Font.font("Consolas", 12)); // فونت مونواسپیس برای لاگ‌ها
        logArea.setStyle("-fx-control-inner-background: #ffffff; -fx-highlight-fill: #ecf0f1; -fx-highlight-text-fill: black; -fx-text-fill: #333;");

        this.getChildren().addAll(title, logArea);
    }

    public void addLog(String message) {
        logArea.appendText("• " + message + "\n\n"); // بولت پوینت برای هر پیام
    }
}