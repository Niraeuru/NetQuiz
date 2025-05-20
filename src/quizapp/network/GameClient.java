package quizapp.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;
import quizapp.model.Player;
import quizapp.model.Question;

public class GameClient {

    private static final int PORT = 8888;
    private static final int RECONNECT_ATTEMPTS = 3;
    private static final int RECONNECT_DELAY = 2000;
    private static final long KEEP_ALIVE_TIMEOUT = 15000;
    private static final long KEEP_ALIVE_INTERVAL = 3000;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String playerName;
    private final String roomCode;
    private boolean connected;
    private ClientListener listener;
    private KeepAliveSender keepAliveSender;
    private long lastKeepAliveReceived;

    @FunctionalInterface
    public interface QuestionCallback {
        void accept(Question question, int questionNumber, int totalQuestions);
    }

    private QuestionCallback questionCallback;
    private Consumer<Integer> timerCallback;
    private Runnable timeUpCallback;
    private Consumer<List<Player>> resultsCallback;
    private Runnable disconnectCallback;

    public GameClient(String playerName, String roomCode, String hostIP) {
        this.playerName = playerName;
        this.roomCode = roomCode;
        this.lastKeepAliveReceived = System.currentTimeMillis();
    }

    public boolean connect(String hostIP) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < RECONNECT_ATTEMPTS; attempt++) {
            try {
                socket = new Socket(hostIP, PORT);
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(15000); // 15 second read timeout
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                Message joinMessage = new Message(MessageType.JOIN);
                joinMessage.setPlayerName(playerName);
                joinMessage.setRoomCode(roomCode);
                out.writeObject(joinMessage);
                out.flush();

                Message response = (Message) in.readObject();

                if (response.getType() == MessageType.JOIN_SUCCESS) {
                    connected = true;
                    lastKeepAliveReceived = System.currentTimeMillis();

                    listener = new ClientListener();
                    keepAliveSender = new KeepAliveSender();
                    new Thread(listener).start();
                    new Thread(keepAliveSender).start();

                    return true;
                } else {
                    socket.close();
                    if (attempt < RECONNECT_ATTEMPTS - 1) {
                        Thread.sleep(RECONNECT_DELAY);
                    }
                }
            } catch (Exception e) {
                if (socket != null) {
                    socket.close();
                }
                if (attempt < RECONNECT_ATTEMPTS - 1) {
                    Thread.sleep(RECONNECT_DELAY);
                }
            }
        }
        return false;
    }

    private class KeepAliveSender implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            while (running && connected) {
                try {
                    Thread.sleep(KEEP_ALIVE_INTERVAL);
                    if (connected) {
                        sendKeepAlive();
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    System.out.println("Error sending keep-alive: " + e.getMessage());
                    handleDisconnect();
                    break;
                }
            }
        }

        public void stop() {
            running = false;
        }
    }

    private void sendKeepAlive() throws IOException {
        if (!connected) return;
        
        Message keepAlive = new Message(MessageType.KEEP_ALIVE);
        synchronized (out) {
            out.writeObject(keepAlive);
            out.flush();
        }
    }

    private class ClientListener implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            try {
                while (running && socket != null && !socket.isClosed()) {
                    try {
                        Message message = (Message) in.readObject();
                        lastKeepAliveReceived = System.currentTimeMillis();

                        switch (message.getType()) {
                            case QUESTION:
                                if (questionCallback != null) {
                                    questionCallback.accept(
                                            message.getQuestion(),
                                            message.getQuestionNumber(),
                                            message.getTotalQuestions()
                                    );
                                }
                                break;

                            case TIMER:
                                if (timerCallback != null) {
                                    timerCallback.accept(message.getTimeRemaining());
                                }
                                break;

                            case TIME_UP:
                                if (timeUpCallback != null) {
                                    timeUpCallback.run();
                                }
                                break;

                            case RESULTS:
                                if (resultsCallback != null) {
                                    resultsCallback.accept(message.getPlayerResults());
                                }
                                break;

                            case DISCONNECT:
                                handleDisconnect();
                                break;

                            case KEEP_ALIVE:
                                // Just update the last keep-alive timestamp
                                lastKeepAliveReceived = System.currentTimeMillis();
                                break;
                        }
                    } catch (IOException e) {
                        if (System.currentTimeMillis() - lastKeepAliveReceived > KEEP_ALIVE_TIMEOUT) {
                            System.out.println("Connection timeout - last keep-alive: " + 
                                (System.currentTimeMillis() - lastKeepAliveReceived) + "ms ago");
                            handleDisconnect();
                            break;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Error reading message: " + e.getMessage());
                        handleDisconnect();
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Client listener error: " + e.getMessage());
                handleDisconnect();
            }
        }

        public void stop() {
            running = false;
        }
    }

    private void handleDisconnect() {
        if (connected) {
            connected = false;
            if (disconnectCallback != null) {
                disconnectCallback.run();
            }
            disconnect();
        }
    }

    public void disconnect() {
        connected = false;
        if (listener != null) {
            listener.stop();
        }
        if (keepAliveSender != null) {
            keepAliveSender.stop();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAnswer(int answerIndex) {
        if (!connected) return;

        try {
            Message message = new Message(MessageType.ANSWER);
            message.setAnswerIndex(answerIndex);
            synchronized (out) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error sending answer: " + e.getMessage());
            handleDisconnect();
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setQuestionCallback(QuestionCallback callback) {
        this.questionCallback = callback;
    }

    public void setTimerCallback(Consumer<Integer> callback) {
        this.timerCallback = callback;
    }

    public void setTimeUpCallback(Runnable callback) {
        this.timeUpCallback = callback;
    }

    public void setResultsCallback(Consumer<List<Player>> callback) {
        this.resultsCallback = callback;
    }

    public void setDisconnectCallback(Runnable callback) {
        this.disconnectCallback = callback;
    }
}