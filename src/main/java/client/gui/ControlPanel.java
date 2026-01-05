package client.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ControlPanel extends HBox {
    private Button rollButton;
    private Button buyButton;
    private Button endTurnButton;

    public ControlPanel() {
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new javafx.geometry.Insets(20));
        this.setStyle("-fx-background-color: #2c3e50;"); // پس‌زمینه تیره

        rollButton = createButton("ROLL DICE", "#3498db"); // آبی
        buyButton = createButton("BUY PROP", "#27ae60");   // سبز
        endTurnButton = createButton("END TURN", "#e74c3c"); // قرمز

        this.getChildren().addAll(rollButton, buyButton, endTurnButton);
    }

    private Button createButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setPrefSize(140, 50);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 5; -fx-cursor: hand;", colorHex));
        btn.setEffect(new DropShadow(3, Color.BLACK));

        // افکت هاور موس
        btn.setOnMouseEntered(e -> btn.setStyle(String.format("-fx-background-color:derive(%s, 20%%); -fx-background-radius: 5;", colorHex)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 5;", colorHex)));

        return btn;
    }

    public Button getRollButton() { return rollButton; }
    public Button getBuyButton() { return buyButton; }
    public Button getEndTurnButton() { return endTurnButton; }
}