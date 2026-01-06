package model;

import java.io.Serializable;

public class Player implements Serializable {
    private int id;
    private String name;
    private int money;
    private int position;
    private boolean inJail;
    private int turnsInJail;
    private boolean isBankrupt;

    public Player(int id, String name, int startingMoney) {
        this.id = id;
        this.name = name;
        this.money = startingMoney;
        this.position = 0;
        this.inJail = false;
        this.turnsInJail = 0;
        this.isBankrupt = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public boolean isInJail() { return inJail; }
    public void setInJail(boolean inJail) {
        this.inJail = inJail;
        if(!inJail) turnsInJail = 0;
    }

    public int getTurnsInJail() { return turnsInJail; }
    public void incrementJailTurn() { this.turnsInJail++; }

    public boolean isBankrupt() { return isBankrupt; }
    public void setBankrupt(boolean bankrupt) { isBankrupt = bankrupt; }
}