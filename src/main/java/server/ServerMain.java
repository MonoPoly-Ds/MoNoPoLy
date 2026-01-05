package server;

import model.GameState;
import utils.Constants; // ایمپورت جدید

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    // استفاده از Constants به جای اعداد هاردکد شده
    private static final int PORT = Constants.PORT;
    private static final int MAX_PLAYERS = Constants.MAX_PLAYERS;

    private static List<ClientHandler> connectedClients = new ArrayList<>();

    public static void main(String[] args) {
        GameState.getInstance();
        TurnManager turnManager = new TurnManager(MAX_PLAYERS);
        GameEngine gameEngine = new GameEngine(turnManager);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for " + MAX_PLAYERS + " players...");

            while (connectedClients.size() < MAX_PLAYERS) {
                Socket clientSocket = serverSocket.accept();

                int pId = connectedClients.size() + 1;
                ClientHandler handler = new ClientHandler(clientSocket, pId, gameEngine);
                connectedClients.add(handler);
                handler.start();

                GameState.getInstance().addPlayer(pId, "Player " + pId);
                System.out.println("Player " + pId + " connected.");

                if (connectedClients.size() == MAX_PLAYERS) {
                    System.out.println("Game Full! Starting...");
                    GameState.getInstance().startGame();

                    broadcast("GAME_STARTED");

                    // ارسال وضعیت اولیه (پول و مکان)
                    for (int i = 1; i <= MAX_PLAYERS; i++) {
                        model.Player p = GameState.getInstance().getPlayer(i);
                        broadcast("STATS:" + p.getId() + ":" + p.getName() + ":" + p.getMoney() + ":" + p.getPosition());
                    }

                    broadcast("TURN:1");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String msg) {
        for (ClientHandler client : connectedClients) {
            client.sendMessage(msg);
        }
    }
}