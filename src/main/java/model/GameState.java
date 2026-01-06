package model;

import ds.list.LinkedList;
import utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private static GameState instance;
    private LinkedList board;
    private List<Player> players;
    private boolean isGameStarted;

    private GameState() {
        players = new ArrayList<>();
        board = new LinkedList();
        isGameStarted = false;
        initBoard();
    }

    public static synchronized GameState getInstance() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    private void initBoard() {
        for (int i = 0; i < Constants.TOTAL_TILES; i++) {
            TileType type = determineTileType(i);
            String name = determineTileName(i);

            // --- تغییرات برای قابل خرید شدن راه‌آهن و خدمات ---
            if (type == TileType.PROPERTY) {
                // املاک معمولی
                int price = (i + 1) * 10 + 50;
                int rent = (i + 1) * 2;
                String color = determineColor(i);
                board.add(new Property(i, name, price, rent, color));
            }
            else if (type == TileType.RAILROAD) {
                // راه‌آهن‌ها (قیمت ثابت ۲۰۰، اجاره ۲۵)
                board.add(new Property(i, name, 200, 25, "BLACK"));
            }
            else if (type == TileType.UTILITY) {
                // اداره برق و آب (قیمت ثابت ۱۵۰، اجاره ۲۰)
                board.add(new Property(i, name, 150, 20, "WHITE"));
            }
            else {
                // سایر خانه‌ها (Tax, Go, Jail, ...) به صورت Tile معمولی می‌مانند
                board.add(new Tile(i, name, type));
            }
        }
    }

    // تشخیص رنگ ملک‌ها
    private String determineColor(int i) {
        if (i == 1 || i == 3) return "BROWN";
        if (i == 6 || i == 8 || i == 9) return "LIGHT_BLUE";
        if (i == 11 || i == 13 || i == 14) return "PINK";
        if (i == 16 || i == 18 || i == 19) return "ORANGE";
        if (i == 21 || i == 23 || i == 24) return "RED";
        if (i == 26 || i == 27 || i == 29) return "YELLOW";
        if (i == 31 || i == 32 || i == 34) return "GREEN";
        if (i == 37 || i == 39) return "DARK_BLUE";
        return "NONE";
    }

    private TileType determineTileType(int i) {
        if (i == 0) return TileType.GO;
        // تعریف خانه‌های مالیات
        if (i == 4) return TileType.TAX; // Income Tax
        if (i == 38) return TileType.TAX; // Luxury Tax

        if (i == 5 || i == 15 || i == 25 || i == 35) return TileType.RAILROAD;
        if (i == 12 || i == 28) return TileType.UTILITY;
        if (i == 10) return TileType.JAIL;
        if (i == 20) return TileType.PARKING;
        if (i == 30) return TileType.GO_TO_JAIL;
        if (i == 2 || i == 17 || i == 33 || i == 7 || i == 22 || i == 36) return TileType.CHANCE;
        return TileType.PROPERTY;
    }

    private String determineTileName(int index) {
        String[] names = {
                "GO", "Mediterranean Ave", "Community Chest", "Baltic Ave", "Income Tax", "Reading RR", "Oriental Ave", "Chance", "Vermont Ave", "Connecticut Ave",
                "Jail", "St. Charles Place", "Electric Company", "States Ave", "Virginia Ave", "Penn. RR", "St. James Place", "Community Chest", "Tennessee Ave", "New York Ave",
                "Free Parking", "Kentucky Ave", "Chance", "Indiana Ave", "Illinois Ave", "B. & O. RR", "Atlantic Ave", "Ventnor Ave", "Water Works", "Marvin Gardens",
                "Go To Jail", "Pacific Ave", "North Carolina Ave", "Community Chest", "Pennsylvania Ave", "Short Line", "Chance", "Park Place", "Luxury Tax", "Boardwalk"
        };
        return (index >= 0 && index < names.length) ? names[index] : "Tile " + index;
    }

    public LinkedList getBoard() { return board; }
    public void addPlayer(int id, String name) { players.add(new Player(id, name, Constants.STARTING_MONEY)); }
    public Player getPlayer(int id) { for (Player p : players) if (p.getId() == id) return p; return null; }
    public boolean isGameStarted() { return isGameStarted; }
    public void startGame() { isGameStarted = true; }
}