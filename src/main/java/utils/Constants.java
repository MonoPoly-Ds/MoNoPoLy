package utils;

public class Constants {
    // تنظیمات شبکه
    public static final String HOST = "localhost";
    public static final int PORT = 8080;

    // تنظیمات بازی
    public static final int MAX_PLAYERS = 4;
    public static final int STARTING_MONEY = 1500;
    public static final int GO_REWARD = 200;
    public static final int JAIL_FINE = 50;

    // *** اضافه شده: تعداد کل خانه‌های بازی ***
    public static final int TOTAL_TILES = 40;

    // تنظیمات گرافیکی
    public static final int TILE_SIZE = 60;
    public static final int BOARD_SIZE = 11 * TILE_SIZE;

    public static final String APP_TITLE = "Monopoly Client";

    private Constants() {}
}