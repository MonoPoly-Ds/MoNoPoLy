package model;

public class Property extends Tile {
    private int price;
    private int baseRent;
    private String colorGroup;
    private int ownerId;

    private int numHouses;
    private boolean hasHotel;
    private int buildCost;

    // وضعیت رهن
    private boolean isMortgaged;

    public Property(int id, String name, int price, int rent, String colorGroup) {
        super(id, name, TileType.PROPERTY);
        this.price = price;
        this.baseRent = rent;
        this.colorGroup = colorGroup;
        this.ownerId = -1;
        this.numHouses = 0;
        this.hasHotel = false;
        this.buildCost = price / 2;
        this.isMortgaged = false;
    }

    // --- متدهای مربوط به رهن ---

    public boolean isMortgaged() { return isMortgaged; }

    public void setMortgaged(boolean mortgaged) {
        this.isMortgaged = mortgaged;
    }

    // مبلغی که بانک به بازیکن می‌دهد (نصف قیمت)
    public int getMortgageValue() {
        return price / 2;
    }

    // مبلغی که بازیکن باید به بانک بدهد (وام + ۱۰ درصد سود)
    public int getUnmortgageCost() {
        return (int) (getMortgageValue() * 1.10);
    }

    // --- سایر گتر/سترها ---
    public int getPrice() { return price; }
    public int getBaseRent() { return baseRent; }
    public String getColorGroup() { return colorGroup; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public int getNumHouses() { return numHouses; }
    public void addHouse() { this.numHouses++; }
    public boolean hasHotel() { return hasHotel; }
    public void setHotel(boolean hasHotel) { this.hasHotel = hasHotel; }
    public int getBuildCost() { return buildCost; }

    @Override
    public void reset() {
        this.ownerId = -1;
        this.numHouses = 0;
        this.hasHotel = false;
        this.isMortgaged = false;
    }
}