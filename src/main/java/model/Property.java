package model;

public class Property extends Tile {
    private int price;
    private int rent;
    private int ownerId;
    private int houseCount;
    private boolean hasHotel;
    private boolean isMortgaged;
    private String colorGroup;

    public Property(int id, String name, int price, int rent, String colorGroup) {
        super(id, TileType.PROPERTY, name);
        this.price = price;
        this.rent = rent;
        this.colorGroup = colorGroup;
        this.ownerId = -1;
        this.houseCount = 0;
        this.hasHotel = false;
        this.isMortgaged = false;
    }

    public int getPrice() { return price; }
    public int getRent() { return rent; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public String getColorGroup() { return colorGroup; }
    public int getHouseCount() { return houseCount; }
    public void addHouse() { this.houseCount++; }
    public boolean isMortgaged() { return isMortgaged; }
    public void setMortgaged(boolean mortgaged) { isMortgaged = mortgaged; }
}