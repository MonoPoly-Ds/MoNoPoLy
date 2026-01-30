package server;

import ds.list.Node;
import ds.queue.MyQueue;
import model.GameState;
import model.Player;
import model.Property;
import model.Tile;
import model.TileType;
import utils.Constants;

public class GameEngine {
    private GameState gameState;
    private TurnManager turnManager;

    private MyQueue chanceDeck;
    private MyQueue communityDeck;

    public GameEngine(TurnManager turnManager) {
        this.gameState = GameState.getInstance();
        this.turnManager = turnManager;
        this.chanceDeck = new MyQueue();
        this.communityDeck = new MyQueue();
        initDecks();
    }

    private void initDecks() {
        String[] chanceCards = {
                "Advance to GO (+200)",
                "Go to Jail",
                "Bank pays you dividend (+50)",
                "Speeding fine (-15)",
                "Take a trip to Reading RR"
        };
        String[] communityCards = {
                "Doctor's fees (-50)",
                "Income Tax refund (+20)",
                "From sale of stock you get +50",
                "Pay hospital fees (-100)",
                "You inherit $100"
        };

        shuffleAndFill(chanceCards, chanceDeck);
        shuffleAndFill(communityCards, communityDeck);
    }

    private void shuffleAndFill(String[] array, MyQueue queue) {
        // الگوریتم Fisher-Yates برای بر زدن آرایه
        for (int i = array.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            String temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        // افزودن به صف دست‌نویس
        for (String card : array) {
            queue.enqueue(card);
        }
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
        } else if (command.startsWith("TRADE")) {
            // منطق معامله با استفاده از گراف
            return executeTrade(playerId, command);
        } else if (command.startsWith("END")) {
            if (!turnManager.hasRolled()) return "ERROR: You must roll dice before ending turn!";
            turnManager.nextTurn();

            while (gameState.getPlayer(turnManager.getCurrentPlayerIndex() + 1).isBankrupt()) {
                turnManager.nextTurn();
            }
            ServerMain.broadcast("TURN:" + (turnManager.getCurrentPlayerIndex() + 1));
            return "SUCCESS: Turn ended.";
        }
        return "ERROR: Unknown command.";
    }

    // --- متد اجرای معامله با استفاده از گراف ---
    private String executeTrade(int senderId, String command) {
        // فرمت دستور: TRADE:targetId:offerAmount:requestAmount
        try {
            String[] parts = command.split(":");
            if (parts.length < 4) return "ERROR: Invalid trade format.";

            int targetId = Integer.parseInt(parts[1]);
            int offer = Integer.parseInt(parts[2]);   // پولی که من می‌دهم
            int request = Integer.parseInt(parts[3]); // پولی که می‌خواهم

            if (senderId == targetId) return "ERROR: Cannot trade with yourself.";

            Player sender = gameState.getPlayer(senderId);
            Player target = gameState.getPlayer(targetId);

            if (target == null) return "ERROR: Player not found.";
            if (target.isBankrupt()) return "ERROR: Player is bankrupt.";

            // بررسی موجودی
            if (sender.getMoney() < offer) return "ERROR: You don't have enough money to offer.";
            if (target.getMoney() < request) return "ERROR: Target doesn't have enough money.";

            // انجام تراکنش مالی
            sender.setMoney(sender.getMoney() - offer + request);
            target.setMoney(target.getMoney() + offer - request);

            // --- استفاده از MyGraph برای ثبت تراکنش ---
            int senderIndex = senderId - 1;
            int targetIndex = targetId - 1;

            // اگر Sender مبلغی پیشنهاد داده، در گراف ثبت می‌شود (یال از Sender به Target)
            if (offer > 0) {
                gameState.getTransactionGraph().addTransaction(senderIndex, targetIndex, offer);
            }
            // اگر Sender مبلغی درخواست کرده (یعنی Target پول می‌دهد)، یال از Target به Sender ثبت می‌شود
            if (request > 0) {
                gameState.getTransactionGraph().addTransaction(targetIndex, senderIndex, request);
            }

            // اطلاع‌رسانی به همه
            ServerMain.broadcast("LOG:Trade! P" + senderId + " gave $" + offer + " <-> P" + targetId + " gave $" + request);
            broadcastPlayerState(sender);
            broadcastPlayerState(target);

            return "SUCCESS: Trade completed.";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Invalid trade command.";
        }
    }

    // --- متد خرید ملک با به‌روزرسانی درخت دارایی ---
    private String buyProperty(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (tile instanceof Property) {
            Property prop = (Property) tile;
            if (prop.getOwnerId() == -1 && player.getMoney() >= prop.getPrice()) {
                player.setMoney(player.getMoney() - prop.getPrice());
                prop.setOwnerId(playerId);

                // --- تغییر جدید: اضافه کردن به درخت دارایی (AssetTree) ---
                // ساختار: بازیکن -> رنگ -> اسم ملک
                player.getAssetTree().addProperty(prop.getColorGroup(), prop.getName(), prop.getId());

                ServerMain.broadcast("OWNER:" + tile.getId() + ":" + playerId);
                broadcastPlayerState(player);

                // چاپ ساختار درخت در کنسول سرور برای دیباگ و مشاهده ساختار سلسله‌مراتبی
                System.out.println(player.getAssetTree().printTree());

                return "SUCCESS: You bought " + prop.getName();
            }
        }
        return "ERROR: Transaction failed.";
    }

    // --- متد ساخت خانه با به‌روزرسانی درخت دارایی ---
    private String buildHouse(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (!(tile instanceof Property)) return "ERROR: Can only build on properties.";
        Property prop = (Property) tile;

        if (prop.getOwnerId() != playerId) return "ERROR: You don't own this.";
        if (prop.isMortgaged()) return "ERROR: Cannot build on mortgaged property.";

        if (!ownsAllColorGroup(playerId, prop.getColorGroup()))
            return "ERROR: Need full color group (" + prop.getColorGroup() + ").";

        if (prop.hasHotel()) return "ERROR: Max build reached.";
        if (player.getMoney() < prop.getBuildCost()) return "ERROR: Not enough money.";

        player.setMoney(player.getMoney() - prop.getBuildCost());

        if (prop.getNumHouses() < 4) {
            prop.addHouse();
            ServerMain.broadcast("LOG:Player " + playerId + " built a HOUSE on " + prop.getName());

            // --- تغییر جدید: اضافه کردن خانه به درخت دارایی ---
            player.getAssetTree().addBuilding(prop.getName(), "House");

        } else {
            prop.setHotel(true);
            ServerMain.broadcast("LOG:Player " + playerId + " built a HOTEL on " + prop.getName());

            // --- تغییر جدید: اضافه کردن هتل به درخت دارایی ---
            player.getAssetTree().addBuilding(prop.getName(), "Hotel");
        }

        // چاپ ساختار درخت برای مشاهده تغییرات
        System.out.println(player.getAssetTree().printTree());

        int visualCount = prop.hasHotel() ? 5 : prop.getNumHouses();
        ServerMain.broadcast("HOUSE:" + prop.getId() + ":" + visualCount);
        broadcastPlayerState(player);
        return "SUCCESS: Build successful.";
    }

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
        ServerMain.broadcast("MORTGAGE_STATE:" + prop.getId() + ":1");
        broadcastPlayerState(player);
        return "SUCCESS: Mortgaged " + prop.getName();
    }

    private String unmortgageProperty(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (!(tile instanceof Property)) return "ERROR: Not a property.";
        Property prop = (Property) tile;

        if (prop.getOwnerId() != playerId) return "ERROR: You don't own this.";
        if (!prop.isMortgaged()) return "ERROR: Not mortgaged.";

        int cost = prop.getUnmortgageCost();
        if (player.getMoney() < cost) return "ERROR: Need $" + cost + " to unmortgage.";

        player.setMoney(player.getMoney() - cost);
        prop.setMortgaged(false);

        ServerMain.broadcast("LOG:Player " + playerId + " unmortgaged " + prop.getName());
        ServerMain.broadcast("MORTGAGE_STATE:" + prop.getId() + ":0");
        broadcastPlayerState(player);
        return "SUCCESS: Property unmortgaged.";
    }

    private boolean ownsAllColorGroup(int playerId, String color) {
        Node current = gameState.getBoard().getHead();
        if (current == null) return false;
        Node head = current;
        do {
            Tile t = (Tile) current.data;
            if (t instanceof Property) {
                Property p = (Property) t;
                if (p.getColorGroup().equals(color) && p.getOwnerId() != playerId) return false;
            }
            current = current.next;
        } while (current != head);
        return true;
    }

    private String rollDice(int playerId) {
        Player player = gameState.getPlayer(playerId);
        turnManager.setRolled(true);

        int d1 = (int) (Math.random() * 6) + 1;
        int d2 = (int) (Math.random() * 6) + 1;
        int total = d1 + d2;
        boolean isDouble = (d1 == d2);

        if (player.isInJail()) {
            if (isDouble) {
                player.setInJail(false);
            } else {
                player.incrementJailTurn();
                if (player.getTurnsInJail() >= 3) {
                    player.setMoney(player.getMoney() - 50);
                    player.setInJail(false);
                } else {
                    broadcastPlayerState(player);
                    return "In Jail. Rolled " + total + ". Stuck.";
                }
            }
        }

        int oldPos = player.getPosition();
        // استفاده از متد move لیست پیوندی دست‌نویس
        Node newNode = gameState.getBoard().move(findNodeById(oldPos), total);
        Tile newTile = (Tile) newNode.data;
        int newPos = newTile.getId();

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

    private void drawCard(Player player) {
        MyQueue deck = (Math.random() > 0.5) ? chanceDeck : communityDeck;
        String type = (deck == chanceDeck) ? "Chance" : "Community Chest";

        String card = (String) deck.dequeue();
        if (card == null) return;
        deck.enqueue(card);

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

    private void handleTileInteraction(Player player, Tile tile) {
        if (tile instanceof Property) {
            Property prop = (Property) tile;
            if (prop.getOwnerId() != -1 && prop.getOwnerId() != player.getId()) {
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
        if (prop.isMortgaged()) return 0;
        int rent = prop.getBaseRent();
        if (prop.getColorGroup().equals("BLACK") || prop.getColorGroup().equals("WHITE") || prop.getColorGroup().equals("NONE")) return rent;
        if (prop.hasHotel()) return rent * 10;
        if (prop.getNumHouses() > 0) return (int) (rent * Math.pow(2.5, prop.getNumHouses()));
        if (ownsAllColorGroup(prop.getOwnerId(), prop.getColorGroup())) return rent * 2;
        return rent;
    }

    private void handleBankruptcy(Player player) {
        player.setBankrupt(true);
        player.setMoney(0);

        Node current = gameState.getBoard().getHead();
        if (current == null) return;
        Node head = current;
        do {
            Tile t = (Tile) current.data;
            if (t instanceof Property) {
                Property prop = (Property) t;
                if (prop.getOwnerId() == player.getId()) {
                    prop.reset();
                    ServerMain.broadcast("OWNER:" + prop.getId() + ":-1");
                    ServerMain.broadcast("HOUSE:" + prop.getId() + ":0");
                }
            }
            current = current.next;
        } while (current != head);
    }

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

    private void broadcastPlayerState(Player p) {
        ServerMain.broadcast("STATS:" + p.getId() + ":" + p.getName() + ":" + p.getMoney() + ":" + p.getPosition());
    }

    private Node findNodeById(int tileId) {
        Node current = gameState.getBoard().getHead();
        if (current == null) return null;
        Node head = current;
        do {
            Tile t = (Tile) current.data;
            if (t.getId() == tileId) return current;
            current = current.next;
        } while (current != head);
        return head;
    }
}