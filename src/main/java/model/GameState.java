package model;

import ds.list.LinkedList;
import ds.map.MyHashMap;
import ds.queue.MyQueue;
import ds.stack.MyStack;
import ds.graph.MyGraph;
import ds.heap.MyMinMaxHeap;

public class GameState {
    private static GameState instance;

    private LinkedList board;
    private MyHashMap players;
    private MyQueue chanceCards;
    private MyQueue communityCards;
    private MyStack actionHistory;
    private MyGraph transactionGraph;
    private MyMinMaxHeap richList;

    private int[] playerIds;
    private int currentPlayerIndex;
    private int playerCount;
    private boolean isGameStarted;

    private GameState() {
        this.board = new LinkedList();
        this.players = new MyHashMap(20);
        this.chanceCards = new MyQueue();
        this.communityCards = new MyQueue();
        this.actionHistory = new MyStack();
        this.transactionGraph = new MyGraph(10);
        this.richList = new MyMinMaxHeap(10);

        this.playerIds = new int[4];
        this.playerCount = 0;
        this.currentPlayerIndex = 0;
        this.isGameStarted = false;

        initBoard();
        initCards();
    }

    public static synchronized GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }

    private void initBoard() {
        // ایجاد صفحه بازی طبق استاندارد مونوپولی (خلاصه شده)
        board.add(new Tile(0, TileType.GO, "GO"));
        board.add(new Property(1, "Mediterranean Ave", 60, 2, "BROWN"));
        board.add(new Tile(2, TileType.COMMUNITY_CHEST, "Community Chest"));
        board.add(new Property(3, "Baltic Ave", 60, 4, "BROWN"));
        board.add(new Tile(4, TileType.TAX, "Income Tax"));
        board.add(new Property(5, "Reading Railroad", 200, 25, "RAILROAD"));
        board.add(new Property(6, "Oriental Ave", 100, 6, "LIGHT_BLUE"));
        board.add(new Tile(7, TileType.CHANCE, "Chance"));
        board.add(new Property(8, "Vermont Ave", 100, 6, "LIGHT_BLUE"));
        board.add(new Property(9, "Connecticut Ave", 120, 8, "LIGHT_BLUE"));
        board.add(new Tile(10, TileType.JAIL, "Jail / Just Visiting"));
        // ... (می‌توانید خانه‌های بیشتر اضافه کنید)
    }

    private void initCards() {
        chanceCards.enqueue(new Card("Advance to GO", Card.CardType.CHANCE, Card.EffectType.MOVE, 0));
        chanceCards.enqueue(new Card("Bank pays you dividend of $50", Card.CardType.CHANCE, Card.EffectType.MONEY, 50));
        chanceCards.enqueue(new Card("Go to Jail", Card.CardType.CHANCE, Card.EffectType.JAIL, 10));
    }

    public void addPlayer(int id, String name) {
        if (playerCount < 4) {
            Player newPlayer = new Player(id, name, 1500);
            players.put(id, newPlayer);
            playerIds[playerCount] = id;
            transactionGraph.addNode(id, name);
            richList.insert(1500, newPlayer);
            playerCount++;
        }
    }

    // Getters
    public LinkedList getBoard() { return board; }
    public MyHashMap getPlayers() { return players; }
    public Player getPlayer(int id) { return (Player) players.get(id); }
    public MyQueue getChanceCards() { return chanceCards; }
    public MyGraph getTransactionGraph() { return transactionGraph; }
    public MyStack getActionHistory() { return actionHistory; }

    // Turn Management
    public int getCurrentPlayerId() { return playerIds[currentPlayerIndex]; }
    public void nextTurn() { currentPlayerIndex = (currentPlayerIndex + 1) % playerCount; }

    // Game Status
    public boolean isGameStarted() { return isGameStarted; }
    public void startGame() { isGameStarted = true; }
    public int getPlayerCount() { return playerCount; }
}