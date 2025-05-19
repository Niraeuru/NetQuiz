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
    private static final int RECONNECT_DELAY = 2000; // 2 seconds

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String playerName;
    private final String roomCode;
    private boolean connected;
    private ClientListener listener;
    private long lastKeepAliveReceived;
    private static final long KEEP_ALIVE_TIMEOUT = 5000; // 5 seconds

    @FunctionalInterface
    public interface QuestionCallback {
        void accept(Question question, int questionNumber, int totalQuestions);
    }

    private QuestionCallback questionCallback;
    private Consumer<Integer> timerCallback;
    private Runnable timeUpCallback;
    private Consumer<Integer> scoreUpdateCallback;
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
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // Send join message
                Message joinMessage = new Message(MessageType.JOIN);
                joinMessage.setPlayerName(playerName);
                joinMessage.setRoomCode(roomCode);
                out.writeObject(joinMessage);

                // Get response
                Message response = (Message) in.readObject();

                if (response.getType() == MessageType.JOIN_SUCCESS) {
                    connected = true;

                    // Start listener thread
                    listener = new ClientListener();
                    new Thread(listener).start();

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

    private class ClientListener implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            try {
                while (running && socket != null && !socket.isClosed()) {
                    Message message = (Message) in.readObject();

                    // Update keep-alive timestamp
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

                        case SCORE_UPDATE:
                            if (scoreUpdateCallback != null) {
                                scoreUpdateCallback.accept(message.getScore());
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
                    }

                    // Check for keep-alive timeout
                    if (System.currentTimeMillis() - lastKeepAliveReceived > KEEP_ALIVE_TIMEOUT) {
                        handleDisconnect();
                        break;
                    }
                }
            } catch (Exception e) {
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
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
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

    public void setScoreUpdateCallback(Consumer<Integer> callback) {
        this.scoreUpdateCallback = callback;
    }

    public void setResultsCallback(Consumer<List<Player>> callback) {
        this.resultsCallback = callback;
    }

    public void setDisconnectCallback(Runnable callback) {
        this.disconnectCallback = callback;
    }
}