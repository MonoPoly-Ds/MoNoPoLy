package client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PlayerPanel extends VBox {
    private VBox[] playerCards;
    private Label[] infoLabels;

    public PlayerPanel() {
        this.setPadding(new Insets(15));
        this.setSpacing(15);
        this.setPrefWidth(240);
        this.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("PLAYERS");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        title.setTextFill(Color.DARKSLATEGRAY);
        this.getChildren().add(title);

        playerCards = new VBox[4];
        infoLabels = new Label[4];
        Color[] pColors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

        for (int i = 0; i < 4; i++) {
            // کارت هر بازیکن
            playerCards[i] = new VBox(5);
            playerCards[i].setPadding(new Insets(10));
            playerCards[i].setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;");
            playerCards[i].setEffect(new DropShadow(5, Color.color(0,0,0,0.1)));

            // هدر کارت (دایره رنگی + نام)
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Circle token = new Circle(8, pColors[i]);
            Label nameLbl = new Label("Player " + (i + 1));
            nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            header.getChildren().addAll(token, nameLbl);

            // اطلاعات (پول و موقعیت)
            infoLabels[i] = new Label("Waiting to join...");
            infoLabels[i].setFont(Font.font("Consolas", 12));
            infoLabels[i].setTextFill(Color.DARKGRAY);

            playerCards[i].getChildren().addAll(header, new javafx.scene.control.Separator(), infoLabels[i]);
            this.getChildren().add(playerCards[i]);
        }
    }

    public void updatePlayer(int index, String name, int money, int position) {
        if (index >= 0 && index < 4) {
            infoLabels[index].setText(String.format("Balance: $%d\nLocation: Tile %d", money, position));
            infoLabels[index].setTextFill(Color.BLACK);
            // تغییر استایل برای نشان دادن فعال بودن
            playerCards[index].setStyle("-fx-background-color: #fff; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;");
        }
    }
}