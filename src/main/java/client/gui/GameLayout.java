package client.gui;

import utils.Constants; // ایمپورت
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class GameLayout extends BorderPane {
    private BoardPane boardPane;
    private ControlPanel controlPanel;
    private PlayerPanel playerPanel;
    private LogPanel logPanel;

    private StackPane centerContainer;
    private Group boardScaler;

    public GameLayout() {
        boardPane = new BoardPane();
        controlPanel = new ControlPanel();
        playerPanel = new PlayerPanel();
        logPanel = new LogPanel();

        // استفاده از Constants
        double size = Constants.BOARD_SIZE;
        boardPane.setMinSize(size, size);
        boardPane.setPrefSize(size, size);
        boardPane.setMaxSize(size, size);

        boardScaler = new Group(boardPane);

        centerContainer = new StackPane(boardScaler);
        centerContainer.setStyle("-fx-background-color: #FAF8EF;");
        centerContainer.setAlignment(Pos.CENTER);

        centerContainer.widthProperty().addListener((o, oldVal, newVal) -> resizeBoard());
        centerContainer.heightProperty().addListener((o, oldVal, newVal) -> resizeBoard());

        this.setCenter(centerContainer);
        this.setBottom(controlPanel);
        this.setRight(playerPanel);
        this.setLeft(logPanel);
    }

    private void resizeBoard() {
        double width = centerContainer.getWidth();
        double height = centerContainer.getHeight();
        if (width == 0 || height == 0) return;

        final double CONTENT_SIZE = Constants.BOARD_SIZE; // استفاده از Constants
        final double PADDING = 40;

        double scaleFactor = Math.min(
                (width - PADDING) / CONTENT_SIZE,
                (height - PADDING) / CONTENT_SIZE
        );
        scaleFactor = Math.max(scaleFactor, 0.1);

        boardScaler.setScaleX(scaleFactor);
        boardScaler.setScaleY(scaleFactor);
    }

    public ControlPanel getControlPanel() { return controlPanel; }
    public PlayerPanel getPlayerPanel() { return playerPanel; }
    public LogPanel getLogPanel() { return logPanel; }
    public BoardPane getBoardPane() { return boardPane; }

    public void addLog(String msg) {
        logPanel.addLog(msg);
    }
}