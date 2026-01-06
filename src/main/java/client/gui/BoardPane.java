package client.gui;

import utils.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class BoardPane extends Pane {
    private Circle[] playerTokens;
    private StackPane[] tilePanes;
    private HBox[] houseContainers; // کانتینر برای خانه‌ها و هتل‌ها

    private final int TILE_SIZE = Constants.TILE_SIZE;
    private final int BOARD_SIZE = Constants.BOARD_SIZE;

    public BoardPane() {
        this.setPrefSize(BOARD_SIZE, BOARD_SIZE);
        this.setStyle("-fx-background-color: #FDF5E6; -fx-border-color: #444; -fx-border-width: 2;");

        tilePanes = new StackPane[40];
        houseContainers = new HBox[40];

        drawCenterInfo();
        drawBoard();
        initPlayerTokens();
    }

    // تغییر رنگ دور خانه (مالکیت)
    public void setTileOwner(int tileIndex, int playerIndex) {
        if (tileIndex < 0 || tileIndex >= 40) return;
        Color[] pColors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        // اگر -1 باشد یعنی بی‌مالک (رنگ مشکی یا شفاف)
        Color ownerColor = (playerIndex >= 0 && playerIndex < 4) ? pColors[playerIndex] : Color.BLACK;

        StackPane tile = tilePanes[tileIndex];
        if (tile.getChildren().get(0) instanceof Rectangle) {
            Rectangle bg = (Rectangle) tile.getChildren().get(0);
            bg.setStroke(ownerColor);
            bg.setStrokeWidth(playerIndex == -1 ? 0.5 : 4);
            bg.setStrokeType(StrokeType.INSIDE);
        }
    }

    // آپدیت گرافیکی خانه‌ها و هتل
    public void updateHouseVisuals(int tileId, int count) {
        if (houseContainers[tileId] == null) return;
        houseContainers[tileId].getChildren().clear();

        if (count == 5) { // هتل
            Rectangle hotel = new Rectangle(12, 12, Color.RED);
            hotel.setStroke(Color.BLACK);
            houseContainers[tileId].getChildren().add(hotel);
        } else if (count > 0) { // خانه‌ها
            for (int i = 0; i < count; i++) {
                Circle house = new Circle(4, Color.GREEN);
                house.setStroke(Color.BLACK);
                houseContainers[tileId].getChildren().add(house);
            }
        }
    }

    // تغییر ظاهر برای رهن (خاکستری شدن)
    public void updateMortgageVisuals(int tileId, boolean isMortgaged) {
        if (tileId < 0 || tileId >= 40) return;
        StackPane tile = tilePanes[tileId];

        javafx.scene.Node lastNode = tile.getChildren().get(tile.getChildren().size() - 1);
        String MORTGAGE_TAG = "MORTGAGE_OVERLAY";

        if (isMortgaged) {
            // اگر هنوز لایه رهن ندارد، اضافه کن
            boolean hasOverlay = lastNode instanceof StackPane && MORTGAGE_TAG.equals(lastNode.getUserData());
            if (!hasOverlay) {
                Rectangle overlay = new Rectangle(TILE_SIZE, TILE_SIZE, Color.rgb(50, 50, 50, 0.7));
                Label mLabel = new Label("MORTGAGED");
                mLabel.setTextFill(Color.WHITE);
                mLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 9px; -fx-rotate: -45;");

                StackPane overlayGroup = new StackPane(overlay, mLabel);
                overlayGroup.setUserData(MORTGAGE_TAG);
                tile.getChildren().add(overlayGroup);
            }
        } else {
            // اگر باید فک رهن شود، لایه را حذف کن
            if (lastNode instanceof StackPane && MORTGAGE_TAG.equals(lastNode.getUserData())) {
                tile.getChildren().remove(lastNode);
            }
        }
    }

    private void drawBoard() {
        for (int i = 0; i < 40; i++) {
            double[] coords = getTileCoordinates(i);
            StackPane tileContainer = new StackPane();
            tileContainer.setLayoutX(coords[0]);
            tileContainer.setLayoutY(coords[1]);
            tileContainer.setPrefSize(TILE_SIZE, TILE_SIZE);
            tileContainer.setStyle("-fx-border-color: black; -fx-border-width: 0.5;");

            tilePanes[i] = tileContainer;

            // پس‌زمینه
            Rectangle bg = new Rectangle(TILE_SIZE, TILE_SIZE);
            bg.setFill(getTileBackgroundColor(i));
            bg.setStroke(Color.BLACK);
            bg.setStrokeWidth(0.5);
            tileContainer.getChildren().add(bg);

            // نوار رنگی و کانتینر خانه
            if (isProperty(i)) {
                Rectangle colorBar = new Rectangle(TILE_SIZE, TILE_SIZE / 4);
                colorBar.setFill(getTileColor(i));
                colorBar.setStroke(Color.BLACK);
                colorBar.setStrokeWidth(0.5);
                tileContainer.getChildren().add(colorBar);
                StackPane.setAlignment(colorBar, Pos.TOP_CENTER);

                HBox houseBox = new HBox(2);
                houseBox.setAlignment(Pos.CENTER);
                houseBox.setMaxSize(TILE_SIZE, TILE_SIZE / 4);
                houseContainers[i] = houseBox;
                tileContainer.getChildren().add(houseBox);
                StackPane.setAlignment(houseBox, Pos.TOP_CENTER);
                StackPane.setMargin(houseBox, new Insets(2,0,0,0));
            }

            // محتوا (نام، آیکون، قیمت)
            VBox content = new VBox(2);
            content.setAlignment(Pos.CENTER);
            content.setPrefSize(TILE_SIZE, TILE_SIZE);
            if (isProperty(i)) content.setPadding(new Insets(15, 2, 2, 2));
            else content.setPadding(new Insets(2));

            if (!getTileIcon(i).isEmpty()) {
                Label iconLbl = new Label(getTileIcon(i));
                iconLbl.setStyle("-fx-font-size: 18px;");
                // رنگ‌بندی آیکون
                if (i == 7 || i == 22 || i == 36) iconLbl.setTextFill(Color.ORANGERED); // Chance
                else if (i == 2 || i == 17 || i == 33) iconLbl.setTextFill(Color.DEEPSKYBLUE); // Chest
                else if (isRailroad(i)) iconLbl.setTextFill(Color.BLACK);
                else if (i == 12) iconLbl.setTextFill(Color.GOLDENROD);
                else if (i == 28) iconLbl.setTextFill(Color.DARKBLUE);
                content.getChildren().add(iconLbl);
            }

            Label nameLbl = new Label(getTileName(i));
            nameLbl.setFont(Font.font("Arial Narrow", FontWeight.BOLD, 10));
            nameLbl.setWrapText(true);
            nameLbl.setTextAlignment(TextAlignment.CENTER);
            nameLbl.setMaxWidth(TILE_SIZE - 4);
            content.getChildren().add(nameLbl);

            int price = getTilePrice(i);
            if (price > 0) {
                Label priceLbl = new Label("$" + price);
                priceLbl.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
                priceLbl.setTextFill(Color.DARKSLATEGRAY);
                content.getChildren().add(priceLbl);
            }

            tileContainer.getChildren().add(content);
            this.getChildren().add(tileContainer);
        }
    }

    private void drawCenterInfo() {
        Rectangle center = new Rectangle(TILE_SIZE, TILE_SIZE, BOARD_SIZE - 2 * TILE_SIZE, BOARD_SIZE - 2 * TILE_SIZE);
        center.setFill(Color.web("#FAF0E6"));
        center.setStroke(Color.LIGHTGRAY);
        this.getChildren().add(center);
        VBox centerBox = new VBox(5);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPrefSize(300, 100);
        centerBox.setLayoutX((BOARD_SIZE - 300) / 2.0);
        centerBox.setLayoutY((BOARD_SIZE - 100) / 2.0);
        Text title = new Text("MONOPOLY");
        title.setFont(Font.font("Impact", 50));
        title.setFill(Color.web("#C21807"));
        Text subTitle = new Text("Student Project Edition");
        subTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        subTitle.setFill(Color.GRAY);
        centerBox.getChildren().addAll(title, subTitle);
        this.getChildren().add(centerBox);
    }

    private void initPlayerTokens() {
        playerTokens = new Circle[4];
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        for (int i = 0; i < 4; i++) {
            playerTokens[i] = new Circle(8, colors[i]);
            playerTokens[i].setStroke(Color.WHITE);
            playerTokens[i].setStrokeWidth(2);
            playerTokens[i].setVisible(false);
            this.getChildren().add(playerTokens[i]);
        }
    }

    public void updateTokenPosition(int playerIndex, int tileIndex) {
        if (playerIndex < 0 || playerIndex >= 4) return;
        playerTokens[playerIndex].setVisible(true);
        double[] coords = getTileCoordinates(tileIndex);
        double offsetX = (playerIndex % 2 == 0) ? TILE_SIZE * 0.25 : TILE_SIZE * 0.75;
        double offsetY = (playerIndex < 2) ? TILE_SIZE * 0.25 : TILE_SIZE * 0.75;
        playerTokens[playerIndex].setCenterX(coords[0] + offsetX);
        playerTokens[playerIndex].setCenterY(coords[1] + offsetY);
        playerTokens[playerIndex].toFront();
    }

    // --- متدهای کمکی و ثابت‌ها ---
    private int getTilePrice(int i) {
        if (isRailroad(i)) return 200;
        if (isUtility(i)) return 150;
        if (isProperty(i)) return (i + 1) * 10 + 50;
        return 0;
    }

    private String getTileIcon(int i) {
        if (i == 2 || i == 17 || i == 33) return "📦";
        if (i == 7 || i == 22 || i == 36) return "?";
        if (i == 5 || i == 15 || i == 25 || i == 35) return "🚆";
        if (i == 12) return "💡";
        if (i == 28) return "💧";
        if (i == 38 || i == 4) return "💎";
        if (i == 10) return "⛓️";
        if (i == 20) return "🚗";
        if (i == 30) return "👮";
        if (i == 0) return "⬅";
        return "";
    }

    private boolean isProperty(int i) { return !isCorner(i) && !isSpecial(i) && !isRailroad(i) && !isUtility(i); }
    private boolean isCorner(int i) { return i % 10 == 0; }
    private boolean isRailroad(int i) { return i == 5 || i == 15 || i == 25 || i == 35; }
    private boolean isUtility(int i) { return i == 12 || i == 28; }
    private boolean isSpecial(int i) { return i == 2 || i == 7 || i == 17 || i == 22 || i == 33 || i == 36 || i == 4 || i == 38; }

    private Color getTileBackgroundColor(int i) {
        if (isCorner(i)) return Color.web("#D8E6F3");
        if (isRailroad(i)) return Color.web("#F0F0F0");
        return Color.WHITE;
    }

    private Color getTileColor(int index) {
        if (index == 1 || index == 3) return Color.web("#8B4513");
        if (index == 6 || index == 8 || index == 9) return Color.web("#87CEEB");
        if (index == 11 || index == 13 || index == 14) return Color.web("#FF69B4");
        if (index == 16 || index == 18 || index == 19) return Color.web("#FFA500");
        if (index == 21 || index == 23 || index == 24) return Color.web("#FF0000");
        if (index == 26 || index == 27 || index == 29) return Color.web("#FFFF00");
        if (index == 31 || index == 32 || index == 34) return Color.web("#008000");
        if (index == 37 || index == 39) return Color.web("#0000FF");
        return Color.TRANSPARENT;
    }

    private double[] getTileCoordinates(int index) {
        double endCoord = BOARD_SIZE - TILE_SIZE;
        double x = 0, y = 0;
        if (index < 10) { x = endCoord - (index * TILE_SIZE); y = endCoord; }
        else if (index < 20) { x = 0; y = endCoord - ((index - 10) * TILE_SIZE); }
        else if (index < 30) { x = (index - 20) * TILE_SIZE; y = 0; }
        else { x = endCoord; y = (index - 30) * TILE_SIZE; }
        return new double[]{x, y};
    }

    private String getTileName(int index) {
        String[] names = {
                "GO", "Medit.\nAve", "Comm.\nChest", "Baltic\nAve", "Income\nTax", "Reading\nRR", "Oriental\nAve", "Chance", "Vermont\nAve", "Conn.\nAve",
                "JAIL", "St. C\nPlace", "Electric\nCo.", "States\nAve", "Virginia\nAve", "Penn.\nRR", "St. J\nPlace", "Comm.\nChest", "Tenn.\nAve", "NY\nAve",
                "Free\nPark", "KY\nAve", "Chance", "Ind.\nAve", "Ill.\nAve", "B. & O.\nRR", "Atl.\nAve", "Ventnor\nAve", "Water\nWorks", "Marvin\nGdn",
                "Go To\nJail", "Pacific\nAve", "NC\nAve", "Comm.\nChest", "Penn.\nAve", "Short\nLine", "Chance", "Park\nPlace", "Luxury\nTax", "Board\nWalk"
        };
        return (index >= 0 && index < names.length) ? names[index] : "";
    }
}