package quizapp.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import quizapp.model.Player;
import quizapp.model.Question;
import quizapp.model.Quiz;
import  quizapp.util.Logger;

public class GameServer {

    private static final int PORT = 8888;
    private static final long KEEP_ALIVE_INTERVAL = 3000; // 3 seconds
    private static final long CLIENT_TIMEOUT = 15000; // 15 seconds

    private ServerSocket serverSocket;
    private final Quiz quiz;
    public Logger logger;
    private final String roomCode;
    private final Map<String, ClientHandler> clients;
    private final List<Player> players;
    private boolean isRunning;
    private Consumer<List<Player>> playerUpdateCallback;
    private Timer keepAliveTimer;
    private Timer questionTimer;
    private int timeRemaining;

    public class ClientHandler implements Runnable {
        private final Socket socket;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;
        private final Player player;
        private final GameServer server;
        private volatile boolean running = true;
        private volatile long lastKeepAliveResponse;

        public ClientHandler(Socket socket, ObjectOutputStream out, ObjectInputStream in,
                             Player player, GameServer server) {
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.player = player;
            this.server = server;
            this.lastKeepAliveResponse = System.currentTimeMillis();
        }

        @Override
        public void run() {
            try {
                while (running && !socket.isClosed()) {
                    try {
                        Message message = (Message) in.readObject();
                        lastKeepAliveResponse = System.currentTimeMillis();

                        switch (message.getType()) {
                            case ANSWER:
                                handleAnswer(player, message.getAnswerIndex());
                                break;

                            case LEAVE:
                                running = false;
                                break;

                            case KEEP_ALIVE:
                                // Send keep-alive response
                                sendKeepAlive();
                                break;
                        }
                    } catch (IOException e) {
                        if (System.currentTimeMillis() - lastKeepAliveResponse > CLIENT_TIMEOUT) {
                            throw new IOException("Client timeout - no response for " + 
                                (System.currentTimeMillis() - lastKeepAliveResponse) + "ms");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Client disconnected: " + player.getName() + " - " + e.getMessage());
            } finally {
                close();
                server.removeClient(player.getName());
            }
        }

        public void sendMessage(Message message) throws IOException {
            synchronized (out) {
                out.writeObject(message);
                out.reset();
                out.flush();
            }
        }

        public void sendKeepAlive() throws IOException {
            Message keepAlive = new Message(MessageType.KEEP_ALIVE);
            sendMessage(keepAlive);
        }

        public void close() {
            running = false;
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public GameServer(Quiz quiz, String roomCode) {
        this.quiz = quiz;
        this.roomCode = roomCode;
        this.clients = Collections.synchronizedMap(new HashMap<>());
        this.players = Collections.synchronizedList(new ArrayList<>());
        this.isRunning = false;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;

        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Game server started on IP: " + hostAddress);
        System.out.println("Room code: " + roomCode);
        logger = new Logger(quiz.roomCode);

        startKeepAliveTimer();

        new Thread(() -> {
            try {
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setKeepAlive(true);
                    clientSocket.setTcpNoDelay(true);
                    handleNewClient(clientSocket);
                }
            } catch (IOException e) {
                if (isRunning) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startKeepAliveTimer() {
        keepAliveTimer = new Timer(true);
        keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (clients) {
                    List<String> disconnectedClients = new ArrayList<>();
                    for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                        try {
                            entry.getValue().sendKeepAlive();
                        } catch (IOException e) {
                            System.out.println("Client " + entry.getKey() + " failed keep-alive check: " + e.getMessage());
                            disconnectedClients.add(entry.getKey());
                        }
                    }
                    for (String clientName : disconnectedClients) {
                        removeClient(clientName);
                    }
                }
            }
        }, KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL);
    }

    private void handleNewClient(Socket clientSocket) {
        try {
            clientSocket.setKeepAlive(true);
            clientSocket.setTcpNoDelay(true);
            clientSocket.setSoTimeout(10000); // 10 second read timeout
            
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            Message joinMessage = (Message) in.readObject();

            if (joinMessage.getType() == MessageType.JOIN &&
                    joinMessage.getRoomCode().equals(roomCode)) {

                String playerName = joinMessage.getPlayerName();
                
                // Check if player with same name already exists
                synchronized (clients) {
                    if (clients.containsKey(playerName)) {
                        Message response = new Message(MessageType.JOIN_FAILED, "Player name already taken");
                        out.writeObject(response);
                        out.flush();
                        clientSocket.close();
                        return;
                    }
                }

                Player player = new Player(playerName);

                ClientHandler handler = new ClientHandler(clientSocket, out, in, player, this);
                synchronized (clients) {
                    clients.put(playerName, handler);
                }

                synchronized (players) {
                    players.add(player);
                }

                Thread handlerThread = new Thread(handler);
                handlerThread.setDaemon(true);
                handlerThread.start();

                Message response = new Message(MessageType.JOIN_SUCCESS);
                out.writeObject(response);
                out.flush();

                if (playerUpdateCallback != null) {
                    playerUpdateCallback.accept(getConnectedPlayers());
                }

            } else {
                Message response = new Message(MessageType.JOIN_FAILED, "Invalid room code");
                out.writeObject(response);
                out.flush();
                clientSocket.close();
            }

        } catch (Exception e) {
            System.out.println("Error handling new client: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void removeClient(String playerName) {
        synchronized (clients) {
            ClientHandler handler = clients.remove(playerName);
            if (handler != null) {
                handler.close();
            }
        }

        synchronized (players) {
            players.removeIf(p -> p.getName().equals(playerName));
        }

        if (playerUpdateCallback != null) {
            playerUpdateCallback.accept(getConnectedPlayers());
        }
        
        System.out.println("Player " + playerName + " disconnected");
    }

    public void stop() {
        isRunning = false;
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel();
        }

        synchronized (clients) {
            for (ClientHandler handler : clients.values()) {
                handler.close();
            }
            clients.clear();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastQuestion(Question question, int questionNumber) {
        Message message = new Message(MessageType.QUESTION);
        message.setQuestion(question);
        message.setQuestionNumber(questionNumber + 1);
        message.setTotalQuestions(quiz.getQuestionCount());

        synchronized (clients) {
            for (ClientHandler handler : clients.values()) {
                try {
                    handler.sendMessage(message);
                } catch (IOException e) {
                    // Client will be removed when its handler detects the error
                }
            }
        }
    }

    public void broadcastTimeUp() {
        Message message = new Message(MessageType.TIME_UP);

        synchronized (clients) {
            for (ClientHandler handler : clients.values()) {
                try {
                    handler.sendMessage(message);
                } catch (IOException e) {
                    // Client will be removed when its handler detects the error
                }
            }
        }
    }

    public void broadcastResults(List<Player> results) {
        Message message = new Message(MessageType.RESULTS);
        message.setPlayerResults(getConnectedPlayers());
        logger.close();

        synchronized (clients) {
            for (ClientHandler handler : clients.values()) {
                try {
                    handler.sendMessage(message);
                } catch (IOException e) {
                    // Client will be removed when its handler detects the error
                }
            }
        }
    }

    public void handleAnswer(Player player, int answerIndex) {
        synchronized (players) {
            Question currentQuestion = quiz.getQuestionAt(quiz.currentQuestionIndex);
            logger.logAnswer(player.getName(),quiz.currentQuestionIndex, String.valueOf(answerIndex),String.valueOf(currentQuestion.getCorrectAnswerIndex()),0);
            if (currentQuestion != null && currentQuestion.isCorrectAnswer(answerIndex)) {
                player.incrementCorrectAnswers();
                // Update all clients with the new scores immediately
                broadcastResults(getConnectedPlayers());
            }
        }
    }

    public List<Player> getConnectedPlayers() {
        synchronized (players) {
            List<Player> sortedPlayers = new ArrayList<>(players);
            Collections.sort(sortedPlayers, (p1, p2) -> {
                int scoreCompare = Integer.compare(p2.getCorrectAnswers(), p1.getCorrectAnswers());
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                return p1.getName().compareTo(p2.getName());
            });
            return sortedPlayers;
        }
    }

    public void setPlayerUpdateCallback(Consumer<List<Player>> callback) {
        this.playerUpdateCallback = callback;
    }

    public String getRoomCode() {
        return roomCode;
    }

    private void startQuestionTimer(int seconds) {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        questionTimer = new Timer();
        timeRemaining = seconds;

        updateTimerDisplay();
        broadcastTimer(timeRemaining);

        questionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeRemaining--;

                SwingUtilities.invokeLater(() -> {
                    updateTimerDisplay();
                    broadcastTimer(timeRemaining);

                    if (timeRemaining <= 0) {
                        questionTimer.cancel();
                        questionTimeUp();
                    }
                });
            }
        }, 1000, 1000);
    }

    public void broadcastTimer(int seconds) {
        Message message = new Message(MessageType.TIMER);
        message.setTimeRemaining(seconds);

        synchronized (clients) {
            for (ClientHandler handler : clients.values()) {
                try {
                    handler.sendMessage(message);
                } catch (IOException e) {
                    // Client will be removed when its handler detects the error
                }
            }
        }
    }

    private void updateTimerDisplay() {
        // Implementation of updateTimerDisplay method
    }

    private void questionTimeUp() {
        // Implementation of questionTimeUp method
    }
}