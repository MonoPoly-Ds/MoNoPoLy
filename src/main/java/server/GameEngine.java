package server;

import ds.list.Node;
import model.GameState;
import model.Player;
import model.Property;
import model.Tile;
import model.TileType;
import utils.Constants; // ایمپورت

public class GameEngine {
    private GameState gameState;
    private TurnManager turnManager;

    public GameEngine(TurnManager turnManager) {
        this.gameState = GameState.getInstance();
        this.turnManager = turnManager;
    }

    public synchronized String executeCommand(int playerId, String command) {
        if (!gameState.isGameStarted()) return "WAIT: Game has not started.";
        if (!turnManager.isTurn(playerId - 1)) return "ERROR: Not your turn.";

        if (command.startsWith("ROLL")) {
            if (turnManager.hasRolled()) return "ERROR: You have already rolled!";
            return rollDice(playerId);
        } else if (command.startsWith("BUY")) {
            if (!turnManager.hasRolled()) return "ERROR: You must roll dice first!";
            return buyProperty(playerId);
        } else if (command.startsWith("END")) {
            if (!turnManager.hasRolled()) return "ERROR: You must roll dice before ending turn!";
            turnManager.nextTurn();
            ServerMain.broadcast("TURN:" + (turnManager.getCurrentPlayerIndex() + 1));
            return "SUCCESS: Turn ended.";
        }
        return "ERROR: Unknown command.";
    }

    private String rollDice(int playerId) {
        Player player = gameState.getPlayer(playerId);
        turnManager.setRolled(true);

        int d1 = (int) (Math.random() * 6) + 1;
        int d2 = (int) (Math.random() * 6) + 1;
        int total = d1 + d2;

        Node currentBoardNode = findNodeById(player.getPosition());
        Node newNode = gameState.getBoard().move(currentBoardNode, total);
        Tile newTile = (Tile) newNode.data;

        player.setPosition(newTile.getId());
        ServerMain.broadcast("MOVED:" + playerId + ":" + total + ":" + newTile.getId());

        String result = "Rolled " + d1 + " & " + d2 + " (" + total + "). Landed on " + newTile.getName();

        if (newTile.getType() == TileType.PROPERTY) {
            Property prop = (Property) newTile;
            if (prop.getOwnerId() == -1) {
                result += " (Price: $" + prop.getPrice() + ")";
            } else if (prop.getOwnerId() != playerId) {
                int rent = prop.getRent();
                player.setMoney(player.getMoney() - rent);
                Player owner = gameState.getPlayer(prop.getOwnerId());
                if (owner != null) {
                    owner.setMoney(owner.getMoney() + rent);
                    broadcastPlayerState(owner);
                }
                result += " (Paid Rent: $" + rent + " to Player " + prop.getOwnerId() + ")";
            }
        } else if (newTile.getType() == TileType.GO) {
            player.setMoney(player.getMoney() + Constants.GO_REWARD); // استفاده از Constants
        }

        broadcastPlayerState(player);
        return result;
    }

    private String buyProperty(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Node currentNode = findNodeById(player.getPosition());
        Tile tile = (Tile) currentNode.data;

        if (tile.getType() == TileType.PROPERTY) {
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
        String msg = "STATS:" + p.getId() + ":" + p.getName() + ":" + p.getMoney() + ":" + p.getPosition();
        ServerMain.broadcast(msg);
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