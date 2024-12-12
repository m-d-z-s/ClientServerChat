package me.mdzs.clientserverchat;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static final String HISTORY_FILE = "chat";
    private static final String messageHistory = readMessageHistory();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            System.out.println(messageHistory);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcast(String message) {
        System.out.println(message);
        saveMessageToHistory(message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private static void saveMessageToHistory(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readMessageHistory() {
        try {
            String history = Files.readString(Paths.get(HISTORY_FILE));

            return history;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String clientName;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                clientName = "Client-" + new Random().nextInt(1000);
                // broadcast(messageHistory);
                broadcast(sdf.format(new Date()) + " - Server - " + clientName + " joined the chat.");
                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(sdf.format(new Date()) + " - " + clientName + " - " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                broadcast(sdf.format(new Date()) + " - Server - " + clientName + " left the chat.");
            }
        }

        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
