package client;

import client.gui.GameLayout;
import client.gui.TradeDialog;
import utils.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox; // برای چیدمان دکمه‌ها
import javafx.stage.Stage;

public class ClientMain extends Application {
    private NetworkManager networkManager;
    private GameLayout root;
    private int myPlayerId;
    private boolean isMyTurn = false;

    // دکمه‌های جدید که نیاز به مدیریت فعال/غیرفعال شدن دارند
    private Button buildBtn;
    private Button mortgageBtn;
    private Button unmortgageBtn;
    private Button tradeBtn;

    @Override
    public void start(Stage primaryStage) {
        root = new GameLayout();

        // اتصال به سرور با استفاده از Constants
        networkManager = new NetworkManager(
                Constants.HOST,
                Constants.PORT,
                message -> Platform.runLater(() -> handleServerMessage(message))
        );

        setupButtons();

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle(Constants.APP_TITLE + " - Connecting...");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void setupButtons() {
        // 1. تنظیم اکشن دکمه‌های اصلی (که در ControlPanel تعریف شده‌اند)
        root.getControlPanel().getRollButton().setOnAction(e -> networkManager.sendMessage("ROLL"));
        root.getControlPanel().getBuyButton().setOnAction(e -> networkManager.sendMessage("BUY"));
        root.getControlPanel().getEndTurnButton().setOnAction(e -> networkManager.sendMessage("END"));

        // 2. ساخت دکمه‌های جدید

        // دکمه معامله (Trade)
        tradeBtn = new Button("Trade");
        tradeBtn.setOnAction(e -> new TradeDialog(command -> networkManager.sendMessage(command)).show());

        // دکمه ساخت خانه (Build) - بنفش
        buildBtn = new Button("Build");
        buildBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
        buildBtn.setOnAction(e -> networkManager.sendMessage("BUILD"));

        // دکمه رهن (Mortgage) - قرمز
        mortgageBtn = new Button("Mortgage");
        mortgageBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        mortgageBtn.setOnAction(e -> networkManager.sendMessage("MORTGAGE"));

        // دکمه فک رهن (Unmortgage) - سبز
        unmortgageBtn = new Button("Unmortgage");
        unmortgageBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        unmortgageBtn.setOnAction(e -> networkManager.sendMessage("UNMORTGAGE"));

        // 3. اضافه کردن دکمه‌ها به پنل پایین
        // همه دکمه‌های جدید را به لیست فرزندان ControlPanel اضافه می‌کنیم
        root.getControlPanel().getChildren().addAll(tradeBtn, buildBtn, mortgageBtn, unmortgageBtn);

        // تنظیم وضعیت اولیه (همه غیرفعال)
        updateButtonStates(false, false, false);
    }

    private void handleServerMessage(String message) {
        // چاپ در کنسول برای دیباگ
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
                // شروع نوبت: فقط Roll فعال است، بقیه غیرفعال
                updateButtonStates(true, false, false);
            } else {
                // نوبت من نیست: همه غیرفعال
                updateButtonStates(false, false, false);
            }
        }
        else if (message.startsWith("MOVED:")) {
            try {
                String[] parts = message.split(":");
                int pId = Integer.parseInt(parts[1]);
                int tileId = Integer.parseInt(parts[3]);

                // حرکت گرافیکی مهره
                root.getBoardPane().updateTokenPosition(pId - 1, tileId);

                // اگر من حرکت کردم، وضعیت دکمه‌ها تغییر می‌کند
                if (pId == myPlayerId) {
                    // تاس ریخته شده، حالا می‌توان خرید کرد، ساخت، رهن داد یا نوبت را تمام کرد
                    updateButtonStates(false, true, true);
                }
            } catch (Exception e) {
                System.err.println("Error parsing MOVE: " + message);
            }
        }
        else if (message.startsWith("OWNER:")) {
            // تغییر رنگ دور خانه (خرید یا رهن شدن)
            String[] parts = message.split(":");
            int tileId = Integer.parseInt(parts[1]);
            int ownerId = Integer.parseInt(parts[2]);
            root.getBoardPane().setTileOwner(tileId, ownerId - 1);

            // اگر من خریدم، دکمه خرید غیرفعال شود
            if (ownerId == myPlayerId && isMyTurn) {
                root.getControlPanel().getBuyButton().setDisable(true);
            }
        }
        else if (message.startsWith("HOUSE:")) {
            // آپدیت تعداد خانه‌های روی ملک
            try {
                String[] parts = message.split(":");
                int tileId = Integer.parseInt(parts[1]);
                int count = Integer.parseInt(parts[2]);
                root.getBoardPane().updateHouseVisuals(tileId, count);
            } catch (Exception e) {
                System.err.println("Error parsing HOUSE: " + message);
            }
        }
        else if (message.startsWith("STATS:")) {
            // آپدیت پنل اطلاعات بازیکن
            try {
                String[] parts = message.split(":");
                int pId = Integer.parseInt(parts[1]);
                root.getPlayerPanel().updatePlayer(
                        pId - 1,
                        parts[2],
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4])
                );
            } catch (Exception e) {
                System.err.println("Error parsing STATS: " + message);
            }
        }
        else if (message.startsWith("SUCCESS: You bought")) {
            root.getControlPanel().getBuyButton().setDisable(true);
            root.addLog(message);
        }
        else {
            // سایر پیام‌ها (لاگ، ارور و ...)
            root.addLog(message);
        }
    }

    /**
     * مدیریت مرکزی وضعیت دکمه‌ها
     * @param roll آیا دکمه تاس فعال باشد؟
     * @param buy آیا دکمه خرید فعال باشد؟
     * @param actions آیا دکمه‌های عملیاتی (پایان نوبت، ساخت، رهن و ...) فعال باشند؟
     */
    private void updateButtonStates(boolean roll, boolean buy, boolean actions) {
        // دکمه‌های اصلی
        root.getControlPanel().getRollButton().setDisable(!roll);
        root.getControlPanel().getBuyButton().setDisable(!buy);
        root.getControlPanel().getEndTurnButton().setDisable(!actions);

        // دکمه‌های عملیاتی جدید (فقط زمانی فعال می‌شوند که نوبت ماست و تاس ریخته‌ایم)
        // یا در برخی قوانین، قبل از تاس هم می‌شود ساخت و ساز کرد.
        // در اینجا فرض می‌کنیم دکمه‌های مدیریتی (ساخت، رهن، ترید) همراه با End Turn فعال می‌شوند.

        boolean managementEnabled = actions || (isMyTurn && !roll); // اگر نوبت من است و تاس نریختم یا ریختم

        // برای سادگی مطابق منطق شما: بعد از حرکت فعال شوند
        buildBtn.setDisable(!actions);
        mortgageBtn.setDisable(!actions);
        unmortgageBtn.setDisable(!actions);
        tradeBtn.setDisable(!actions && !roll); // ترید همیشه وقتی نوبت ماست فعال باشد بهتر است
    }

    @Override
    public void stop() {
        if (networkManager != null) networkManager.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}