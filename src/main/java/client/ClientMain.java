package client;

import client.gui.GameLayout;
import client.gui.TradeDialog;
import utils.Constants; // ایمپورت جدید
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ClientMain extends Application {
    private NetworkManager networkManager;
    private GameLayout root;
    private int myPlayerId;

    private int currentTileId = 0;
    private boolean isMyTurn = false;

    @Override
    public void start(Stage primaryStage) {
        root = new GameLayout();

        // استفاده از Constants برای اتصال
        networkManager = new NetworkManager(
                Constants.HOST,
                Constants.PORT,
                message -> Platform.runLater(() -> handleServerMessage(message))
        );

        setupButtons();

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle(Constants.APP_TITLE + " - Connecting..."); // استفاده از تایتل ثابت
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void setupButtons() {
        setButtonsState(false, false, false);

        root.getControlPanel().getRollButton().setOnAction(e -> networkManager.sendMessage("ROLL"));
        root.getControlPanel().getBuyButton().setOnAction(e -> networkManager.sendMessage("BUY"));
        root.getControlPanel().getEndTurnButton().setOnAction(e -> networkManager.sendMessage("END"));

        Button tradeBtn = new Button("Trade");
        tradeBtn.setOnAction(e -> new TradeDialog(command -> networkManager.sendMessage(command)).show());
        root.getControlPanel().getChildren().add(tradeBtn);
    }

    private void handleServerMessage(String message) {
        System.out.println("Server: " + message);

        if (message.startsWith("WELCOME Player")) {
            myPlayerId = Integer.parseInt(message.split(" ")[2]);
            ((Stage) root.getScene().getWindow()).setTitle(Constants.APP_TITLE + " - Player " + myPlayerId);
            root.addLog("Connected as Player " + myPlayerId);
        }
        else if (message.equals("GAME_STARTED")) {
            root.addLog("Game Started!");
            for(int i=0; i<4; i++) root.getBoardPane().updateTokenPosition(i, 0);
        }
        else if (message.startsWith("TURN:")) {
            int turnId = Integer.parseInt(message.split(":")[1]);
            isMyTurn = (myPlayerId == turnId);
            root.addLog("It is Player " + turnId + "'s turn.");

            if (isMyTurn) {
                setButtonsState(true, false, false);
            } else {
                setButtonsState(false, false, false);
            }
        }
        else if (message.startsWith("MOVED:")) {
            String[] parts = message.split(":");
            int pId = Integer.parseInt(parts[1]);
            int tileId = Integer.parseInt(parts[3]);
            root.getBoardPane().updateTokenPosition(pId - 1, tileId);

            if (pId == myPlayerId) {
                currentTileId = tileId;
                setButtonsState(false, true, true);
            }
        }
        else if (message.startsWith("OWNER:")) {
            String[] parts = message.split(":");
            int tileId = Integer.parseInt(parts[1]);
            int ownerId = Integer.parseInt(parts[2]);
            root.getBoardPane().setTileOwner(tileId, ownerId - 1);

            if (ownerId == myPlayerId && isMyTurn) {
                root.getControlPanel().getBuyButton().setDisable(true);
            }
        }
        else if (message.startsWith("STATS:")) {
            String[] parts = message.split(":");
            root.getPlayerPanel().updatePlayer(Integer.parseInt(parts[1]) - 1, parts[2], Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
        }
        else if (message.startsWith("SUCCESS: You bought")) {
            root.getControlPanel().getBuyButton().setDisable(true);
            root.addLog(message);
        }
        else {
            root.addLog(message);
        }
    }

    private void setButtonsState(boolean roll, boolean buy, boolean end) {
        root.getControlPanel().getRollButton().setDisable(!roll);
        root.getControlPanel().getBuyButton().setDisable(!buy);
        root.getControlPanel().getEndTurnButton().setDisable(!end);
    }

    @Override
    public void stop() { if (networkManager != null) networkManager.close(); }
    public static void main(String[] args) { launch(args); }
}