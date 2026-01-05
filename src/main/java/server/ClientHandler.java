package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private int playerId;
    private GameEngine gameEngine;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, int playerId, GameEngine gameEngine) {
        this.socket = socket;
        this.playerId = playerId;
        this.gameEngine = gameEngine;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("WELCOME Player " + playerId);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Player " + playerId + " sent: " + inputLine);
                String response = gameEngine.executeCommand(playerId, inputLine);
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Player " + playerId + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    // این متد را به انتهای کلاس ClientHandler اضافه کنید
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}