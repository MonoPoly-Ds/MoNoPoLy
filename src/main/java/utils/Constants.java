package utils;

public class Constants {
    // تنظیمات شبکه
    public static final String HOST = "localhost";
    public static final int PORT = 8080;

    // تنظیمات بازی
    public static final int MAX_PLAYERS = 4;
    public static final int STARTING_MONEY = 1500;
    public static final int GO_REWARD = 200;      // جایزه عبور از شروع
    public static final int JAIL_FINE = 50;       // جریمه زندان (اختیاری)

    // تنظیمات گرافیکی
    public static final int TILE_SIZE = 60;       // اندازه هر خانه
    // اندازه کل صفحه = (تعداد خانه‌های یک ضلع) * (اندازه هر خانه)
    // 11 خانه در هر ضلع وجود دارد (شامل گوشه‌ها)
    public static final int BOARD_SIZE = 11 * TILE_SIZE;

    public static final String APP_TITLE = "Monopoly Client";

    // سازنده خصوصی تا کسی نتواند از این کلاس نمونه (new) بسازد
    private Constants() {}
}