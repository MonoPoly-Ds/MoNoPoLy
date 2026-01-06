package model;

// کلمه abstract را از اینجا حذف کردیم
public class Tile {
    private int id;
    private String name;
    private TileType type;

    public Tile(int id, String name, TileType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public TileType getType() { return type; }

    // اگر متدی مثل reset دارید که در Property پیاده‌سازی شده،
    // در اینجا باید یک نسخه خالی از آن داشته باشید تا ارور ندهد.
    public void reset() {
        // بدنه خالی: برای خانه‌های معمولی (مثل GO یا Parking) کاری انجام نمی‌دهد
    }
}