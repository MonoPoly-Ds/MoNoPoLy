package server;

import ds.list.Node;
import model.GameState;
import model.Player;
import model.Property;
import model.Tile;
import model.TileType;
import utils.Constants;
import java.util.*;

public class GameEngine {
    private GameState gameState;
    private TurnManager turnManager;

    // صف کارت‌ها برای رعایت نوبت (Queue)
    private Queue<String> chanceDeck;
    private Queue<String> communityDeck;

    public GameEngine(TurnManager turnManager) {
        this.gameState = GameState.getInstance();
        this.turnManager = turnManager;
        initDecks();
    }

    private void initDecks() {
        List<String> chanceList = Arrays.asList(
                "Advance to GO (+200)",
                "Go to Jail",
                "Bank pays you dividend (+50)",
                "Speeding fine (-15)",
                "Take a trip to Reading RR"
        );
        List<String> communityList = Arrays.asList(
                "Doctor's fees (-50)",
                "Income Tax refund (+20)",
                "From sale of stock you get +50",
                "Pay hospital fees (-100)",
                "You inherit $100"
        );
        // بر زدن کارت‌ها و تبدیل به صف
        Collections.shuffle(chanceList);
        Collections.shuffle(communityList);
        chanceDeck = new LinkedList<>(chanceList);
        communityDeck = new LinkedList<>(communityList);
    }

    public synchronized String executeCommand(int playerId, String command) {
        if (!gameState.isGameStarted()) return "WAIT: Game has not started.";
        Player player = gameState.getPlayer(playerId);

        if (player.isBankrupt()) return "ERROR: You are bankrupt!";
        if (!turnManager.isTurn(playerId - 1)) return "ERROR: Not your turn.";

        if (command.startsWith("ROLL")) {
            if (turnManager.hasRolled()) return "ERROR: You have already rolled!";
            return rollDice(playerId);
        } else if (command.startsWith("BUY")) {
            if (!turnManager.hasRolled()) return "ERROR: You must roll dice first!";
            return buyProperty(playerId);
        } else if (command.startsWith("BUILD")) {
            return buildHouse(playerId);
        } else if (command.startsWith("MORTGAGE")) {
            return mortgageProperty(playerId);
        } else if (command.startsWith("UNMORTGAGE")) {
            return unmortgageProperty(playerId);
        } else if (command.startsWith("END")) {
            if (!turnManager.hasRolled()) return "ERROR: You must roll dice before ending turn!";
            turnManager.nextTurn();
            // رد کردن بازیکنان ورشکسته
            while (gameState.getPlayer(turnManager.getCurrentPlayerIndex() + 1).isBankrupt()) {
                turnManager.nextTurn();
            }
            ServerMain.broadcast("TURN:" + (turnManager.getCurrentPlayerIndex() + 1));
            return "SUCCESS: Turn ended.";
        }
        return "ERROR: Unknown command.";
    }

    // --- منطق رهن (Mortgage) ---
    private String mortgageProperty(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (!(tile instanceof Property)) return "ERROR: Not a property.";
        Property prop = (Property) tile;

        if (prop.getOwnerId() != playerId) return "ERROR: You don't own this.";
        if (prop.isMortgaged()) return "ERROR: Already mortgaged.";
        if (prop.getNumHouses() > 0 || prop.hasHotel()) return "ERROR: Sell houses first.";

        int loan = prop.getMortgageValue();
        player.setMoney(player.getMoney() + loan);
        prop.setMortgaged(true);

        ServerMain.broadcast("LOG:Player " + playerId + " mortgaged " + prop.getName());
        // ارسال پیام گرافیکی (1 = رهن شده)
        ServerMain.broadcast("MORTGAGE_STATE:" + prop.getId() + ":1");
        broadcastPlayerState(player);
        return "SUCCESS: Mortgaged " + prop.getName();
    }

    // --- منطق فک رهن (Unmortgage) ---
    private String unmortgageProperty(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (!(tile instanceof Property)) return "ERROR: Not a property.";
        Property prop = (Property) tile;

        if (prop.getOwnerId() != playerId) return "ERROR: You don't own this.";
        if (!prop.isMortgaged()) return "ERROR: Not mortgaged.";

        int cost = prop.getUnmortgageCost(); // اصل پول + سود
        if (player.getMoney() < cost) return "ERROR: Need $" + cost + " to unmortgage.";

        player.setMoney(player.getMoney() - cost);
        prop.setMortgaged(false);

        ServerMain.broadcast("LOG:Player " + playerId + " unmortgaged " + prop.getName());
        // ارسال پیام گرافیکی (0 = آزاد)
        ServerMain.broadcast("MORTGAGE_STATE:" + prop.getId() + ":0");
        broadcastPlayerState(player);
        return "SUCCESS: Property unmortgaged.";
    }

    // --- منطق ساخت خانه ---
    private String buildHouse(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (!(tile instanceof Property)) return "ERROR: Can only build on properties.";
        Property prop = (Property) tile;

        if (prop.getOwnerId() != playerId) return "ERROR: You don't own this.";
        if (prop.isMortgaged()) return "ERROR: Cannot build on mortgaged property.";
        if (prop.getColorGroup().equals("BLACK") || prop.getColorGroup().equals("WHITE") || prop.getColorGroup().equals("NONE"))
            return "ERROR: Cannot build here.";

        if (!ownsAllColorGroup(playerId, prop.getColorGroup()))
            return "ERROR: Need full color group (" + prop.getColorGroup() + ").";

        if (prop.hasHotel()) return "ERROR: Max build reached.";
        if (player.getMoney() < prop.getBuildCost()) return "ERROR: Not enough money.";

        player.setMoney(player.getMoney() - prop.getBuildCost());

        if (prop.getNumHouses() < 4) {
            prop.addHouse();
            ServerMain.broadcast("LOG:Player " + playerId + " built a HOUSE on " + prop.getName());
        } else {
            prop.setHotel(true);
            ServerMain.broadcast("LOG:Player " + playerId + " built a HOTEL on " + prop.getName());
        }

        // 5 یعنی هتل، 1-4 یعنی خانه
        int visualCount = prop.hasHotel() ? 5 : prop.getNumHouses();
        ServerMain.broadcast("HOUSE:" + prop.getId() + ":" + visualCount);
        broadcastPlayerState(player);
        return "SUCCESS: Build successful.";
    }

    private boolean ownsAllColorGroup(int playerId, String color) {
        Node current = gameState.getBoard().getHead();
        do {
            Tile t = (Tile) current.data;
            if (t instanceof Property) {
                Property p = (Property) t;
                if (p.getColorGroup().equals(color) && p.getOwnerId() != playerId) return false;
            }
            current = current.next;
        } while (current != gameState.getBoard().getHead());
        return true;
    }

    private String rollDice(int playerId) {
        Player player = gameState.getPlayer(playerId);
        turnManager.setRolled(true);

        int d1 = (int) (Math.random() * 6) + 1;
        int d2 = (int) (Math.random() * 6) + 1;
        int total = d1 + d2;
        boolean isDouble = (d1 == d2);

        // قوانین زندان
        if (player.isInJail()) {
            if (isDouble) {
                player.setInJail(false);
                ServerMain.broadcast("LOG:Player " + playerId + " rolled doubles -> Free!");
            } else {
                player.incrementJailTurn();
                if (player.getTurnsInJail() >= 3) {
                    player.setMoney(player.getMoney() - 50);
                    player.setInJail(false);
                    ServerMain.broadcast("LOG:Player " + playerId + " paid bail.");
                } else {
                    broadcastPlayerState(player);
                    return "In Jail. Rolled " + total + ". Stuck.";
                }
            }
        }

        // حرکت
        int oldPos = player.getPosition();
        Node newNode = gameState.getBoard().move(findNodeById(oldPos), total);
        Tile newTile = (Tile) newNode.data;
        int newPos = newTile.getId();

        // رفتن به زندان
        if (newTile.getType() == TileType.GO_TO_JAIL) {
            sendToJail(player);
            return "Rolled " + total + ". Go Directly to Jail!";
        }

        player.setPosition(newPos);
        ServerMain.broadcast("MOVED:" + playerId + ":" + total + ":" + newPos);

        String result = "Rolled " + total + ". Landed on " + newTile.getName();
        if (newPos < oldPos) {
            player.setMoney(player.getMoney() + Constants.GO_REWARD);
            result += " (Passed GO)";
        }

        handleTileInteraction(player, newTile);

        if (player.getMoney() < 0) {
            handleBankruptcy(player);
            result += " (BANKRUPT!)";
        }

        broadcastPlayerState(player);
        return result;
    }

    private void handleTileInteraction(Player player, Tile tile) {
        if (tile instanceof Property) {
            Property prop = (Property) tile;
            if (prop.getOwnerId() == -1) {
                // Available
            } else if (prop.getOwnerId() != player.getId()) {
                int rent = calculateRent(prop);
                if (rent > 0) payToPlayer(player, prop.getOwnerId(), rent);
            }
        } else if (tile.getType() == TileType.TAX) {
            int tax = (tile.getId() == 4) ? 200 : 100;
            player.setMoney(player.getMoney() - tax);
        } else if (tile.getType() == TileType.CHANCE || tile.getType() == TileType.COMMUNITY_CHEST) {
            drawCard(player);
        }
    }

    private int calculateRent(Property prop) {
        if (prop.isMortgaged()) return 0; // اگر رهن باشد اجاره ندارد

        int rent = prop.getBaseRent();
        // برای راه‌آهن و یوتیلیتی فعلاً ساده (اجاره پایه)
        if (prop.getColorGroup().equals("BLACK") || prop.getColorGroup().equals("WHITE") || prop.getColorGroup().equals("NONE")) {
            return rent;
        }

        if (prop.hasHotel()) return rent * 10;
        if (prop.getNumHouses() > 0) return (int) (rent * Math.pow(2.5, prop.getNumHouses()));
        if (ownsAllColorGroup(prop.getOwnerId(), prop.getColorGroup())) return rent * 2;

        return rent;
    }

    private void drawCard(Player player) {
        Queue<String> deck = (Math.random() > 0.5) ? chanceDeck : communityDeck;
        String type = (deck == chanceDeck) ? "Chance" : "Community Chest";

        String card = deck.poll(); // برداشتن از سر صف
        if (card == null) return;
        deck.offer(card); // گذاشتن ته صف

        ServerMain.broadcast("LOG:Player " + player.getId() + " drew " + type + ": " + card);

        if (card.contains("Advance to GO")) {
            player.setPosition(0);
            player.setMoney(player.getMoney() + 200);
            ServerMain.broadcast("MOVED:" + player.getId() + ":0:0");
        } else if (card.contains("Go to Jail")) {
            sendToJail(player);
        } else if (card.contains("+")) {
            int amount = Integer.parseInt(card.replaceAll("[^0-9]", ""));
            player.setMoney(player.getMoney() + amount);
        } else if (card.contains("-")) {
            int amount = Integer.parseInt(card.replaceAll("[^0-9]", ""));
            player.setMoney(player.getMoney() - amount);
        }
    }

    private void handleBankruptcy(Player player) {
        player.setBankrupt(true);
        player.setMoney(0);
        ServerMain.broadcast("LOG:PLAYER " + player.getId() + " BANKRUPT! Assets released.");

        Node current = gameState.getBoard().getHead();
        do {
            Tile t = (Tile) current.data;
            if (t instanceof Property) {
                Property prop = (Property) t;
                if (prop.getOwnerId() == player.getId()) {
                    prop.reset(); // پاک کردن مالکیت و خانه‌ها
                    ServerMain.broadcast("OWNER:" + prop.getId() + ":-1");
                    ServerMain.broadcast("HOUSE:" + prop.getId() + ":0");
                    ServerMain.broadcast("MORTGAGE_STATE:" + prop.getId() + ":0");
                }
            }
            current = current.next;
        } while (current != gameState.getBoard().getHead());
    }

    // --- متدهای کمکی استاندارد ---
    private void sendToJail(Player player) {
        player.setPosition(10);
        player.setInJail(true);
        ServerMain.broadcast("MOVED:" + player.getId() + ":0:10");
        broadcastPlayerState(player);
    }

    private void payToPlayer(Player payer, int receiverId, int amount) {
        payer.setMoney(payer.getMoney() - amount);
        Player receiver = gameState.getPlayer(receiverId);
        if (receiver != null && !receiver.isBankrupt()) {
            receiver.setMoney(receiver.getMoney() + amount);
            broadcastPlayerState(receiver);
        }
    }

    private String buyProperty(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (tile instanceof Property) {
            Property prop = (Property) tile;
            if (prop.getOwnerId() == -1) {
                if (player.getMoney() >= prop.getPrice()) {
                    player.setMoney(player.getMoney() - prop.getPrice());
                    prop.setOwnerId(playerId);
                    ServerMain.broadcast("LOG:Player " + playerId + " bought " + prop.getName());
                    ServerMain.broadcast("OWNER:" + tile.getId() + ":" + playerId);
                    broadcastPlayerState(player);
                    return "SUCCESS: You bought " + prop.getName();
                }
                return "ERROR: Not enough money.";
            }
            return "ERROR: Already owned.";
        }
        return "ERROR: Not for sale.";
    }

    private void broadcastPlayerState(Player p) {
        ServerMain.broadcast("STATS:" + p.getId() + ":" + p.getName() + ":" + p.getMoney() + ":" + p.getPosition());
    }

    private Node findNodeById(int tileId) {
        Node current = gameState.getBoard().getHead();
        do {
            Tile t = (Tile) current.data;
            if (t.getId() == tileId) return current;
            current = current.next;
        } while (current != gameState.getBoard().getHead());
        return gameState.getBoard().getHead();
    }
}