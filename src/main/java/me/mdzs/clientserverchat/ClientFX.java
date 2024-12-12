package me.mdzs.clientserverchat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientFX extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private PrintWriter out;
    private BufferedReader in;

    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        TextField inputField = new TextField();
        inputField.setPromptText("Enter your message...");
        Button sendButton = new Button("Send");

        root.getChildren().addAll(chatArea, inputField, sendButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Подключение к серверу в отдельном потоке
        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out = new PrintWriter(socket.getOutputStream(), true);

                // Получение сообщений от сервера
                String message;
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    // Обновляем UI в JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> chatArea.appendText("Disconnected from server.\n"));
            }
        }).start();

        // Отправка сообщений
        sendButton.setOnAction(event -> sendMessage(inputField, chatArea));
        inputField.setOnAction(event -> sendMessage(inputField, chatArea));
    }

    private void sendMessage(TextField inputField, TextArea chatArea) {
        String message = inputField.getText();
        if (message.isEmpty()) {
            return;
        }
        out.println(message);
        inputField.clear();
        // chatArea.appendText("You: " + message + "\n");
    }

    private void appendMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    private void showError(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(error);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        if (out != null) {
            out.close();
        }
        if (in != null) {
            in.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
