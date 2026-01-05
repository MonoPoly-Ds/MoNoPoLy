package server;

public class TurnManager {
    private int currentPlayerIndex;
    private int totalPlayers;
    private boolean hasRolled; // آیا بازیکن در این نوبت تاس ریخته؟

    public TurnManager(int totalPlayers) {
        this.totalPlayers = totalPlayers;
        this.currentPlayerIndex = 0;
        this.hasRolled = false;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    // بررسی نوبت و وضعیت تاس
    public boolean isTurn(int playerIndex) {
        return currentPlayerIndex == playerIndex;
    }

    public boolean hasRolled() {
        return hasRolled;
    }

    public void setRolled(boolean rolled) {
        this.hasRolled = rolled;
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % totalPlayers;
        hasRolled = false; // ریست کردن برای نفر بعدی
    }
}