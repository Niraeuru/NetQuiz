package quizapp.ui.host;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import quizapp.model.Player;
import quizapp.model.Question;
import quizapp.model.Quiz;
import quizapp.network.GameServer;
import quizapp.util.ColorScheme;

public class HostGameScreen extends JFrame {

    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel playerListPanel;
    private JPanel questionPanel;
    private JLabel roomCodeLabel;
    private JLabel timerLabel;
    private JLabel questionLabel;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton endQuizButton;

    private Quiz quiz;
    private GameServer server;
    private int currentQuestionIndex = -1;
    private Question currentQuestion;
    private Timer questionTimer;
    private int timeRemaining;
    private boolean quizStarted = false;

    public HostGameScreen(Quiz quiz, GameServer server) {
        this.quiz = quiz;
        this.server = server;

        initComponents();
        setupLayout();
        addListeners();

        setTitle("Host Game Control");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showLobby();
        server.setPlayerUpdateCallback(this::updatePlayerList);
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(ColorScheme.BACKGROUND);

        leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(ColorScheme.BACKGROUND);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ColorScheme.PRIMARY, 2),
                "Quiz Progress",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                ColorScheme.PRIMARY
        ));

        rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(ColorScheme.BACKGROUND);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ColorScheme.SECONDARY, 2),
                "Players",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                ColorScheme.SECONDARY
        ));

        roomCodeLabel = new JLabel("Room Code: " + server.getRoomCode());
        roomCodeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roomCodeLabel.setForeground(ColorScheme.PRIMARY);

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timerLabel.setForeground(ColorScheme.PRIMARY);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        questionPanel = new JPanel(new BorderLayout(10, 20));
        questionPanel.setBackground(ColorScheme.CARD_BACKGROUND);
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        questionLabel = new JLabel("Waiting for players to join...");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setForeground(ColorScheme.PRIMARY_TEXT);

        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setBackground(ColorScheme.BACKGROUND);

        statusLabel = new JLabel("Lobby - Waiting for players");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        statusLabel.setForeground(ColorScheme.SECONDARY_TEXT);

        startButton = new JButton("Start Quiz");
        startButton.setBackground(ColorScheme.PRIMARY);
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setFocusPainted(false);

        endQuizButton = new JButton("End Quiz");
        endQuizButton.setBackground(ColorScheme.ERROR);
        endQuizButton.setForeground(Color.WHITE);
        endQuizButton.setFont(new Font("Arial", Font.BOLD, 16));
        endQuizButton.setFocusPainted(false);
        endQuizButton.setEnabled(false);
    }

    private void setupLayout() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(ColorScheme.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Quiz Host - " + quiz.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(roomCodeLabel, BorderLayout.EAST);

        questionPanel.add(timerLabel, BorderLayout.NORTH);
        questionPanel.add(questionLabel, BorderLayout.CENTER);

        JPanel questionControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        questionControlPanel.setBackground(ColorScheme.BACKGROUND);
        questionControlPanel.add(startButton);
        questionControlPanel.add(endQuizButton);

        leftPanel.add(questionPanel, BorderLayout.CENTER);
        leftPanel.add(questionControlPanel, BorderLayout.SOUTH);

        JScrollPane playerScrollPane = new JScrollPane(playerListPanel);
        playerScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        rightPanel.add(playerScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(800);
        splitPane.setBorder(null);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private void addListeners() {
        startButton.addActionListener(e -> {
            if (!quizStarted) {
                startQuiz();
            }
        });

        endQuizButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    HostGameScreen.this,
                    "Are you sure you want to end the quiz?",
                    "Confirm End Quiz",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (questionTimer != null) {
                    questionTimer.cancel();
                }
                showResults();
            }
        });
    }

    private void showLobby() {
        questionLabel.setText("Waiting for players to join...");
        statusLabel.setText("Lobby - Waiting for players");
        startButton.setText("Start Quiz");
        timerLabel.setText("");
        updatePlayerList(server.getConnectedPlayers());
    }

    private void startQuiz() {
        quizStarted = true;
        startButton.setEnabled(false);
        endQuizButton.setEnabled(true);
        nextQuestion();
    }

    private void nextQuestion() {
        currentQuestionIndex++;

        if (currentQuestionIndex < quiz.getQuestions().size()) {
            currentQuestion = quiz.getQuestions().get(currentQuestionIndex);

            questionLabel.setText("<html><div style='text-align: center;'>" +
                    "Question " + (currentQuestionIndex + 1) + "/" + quiz.getQuestions().size() +
                    "<br><br>" + currentQuestion.getText() + "</div></html>");

            statusLabel.setText("Question in progress...");
            startQuestionTimer(currentQuestion.getTimeLimit());
            server.broadcastQuestion(currentQuestion, currentQuestionIndex);
        } else {
            showResults();
        }
    }

    private void startQuestionTimer(int seconds) {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        questionTimer = new Timer();
        timeRemaining = seconds;

        updateTimerDisplay();

        questionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeRemaining--;

                SwingUtilities.invokeLater(() -> {
                    updateTimerDisplay();

                    if (timeRemaining <= 0) {
                        questionTimer.cancel();
                        questionTimeUp();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void updateTimerDisplay() {
        timerLabel.setText(String.format("%02d:%02d", timeRemaining / 60, timeRemaining % 60));

        if (timeRemaining <= 5) {
            timerLabel.setForeground(ColorScheme.ERROR);
        } else if (timeRemaining <= 10) {
            timerLabel.setForeground(ColorScheme.WARNING);
        } else {
            timerLabel.setForeground(ColorScheme.PRIMARY);
        }
    }

    private void questionTimeUp() {
        server.broadcastTimeUp();

        String[] options = currentQuestion.getOptions();
        int correctIndex = currentQuestion.getCorrectAnswerIndex();
        String correctAnswer = options[correctIndex];

        questionLabel.setText("<html><div style='text-align: center;'>" +
                currentQuestion.getText() +
                "<br><br>Correct Answer: <span style='color: green; font-weight: bold;'>" +
                "Option " + (char)('A' + correctIndex) + ": " + correctAnswer +
                "</span></div></html>");

        Timer transitionTimer = new Timer();
        transitionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (currentQuestionIndex < quiz.getQuestions().size() - 1) {
                        nextQuestion();
                    } else {
                        showResults();
                    }
                });
            }
        }, 3000);
    }

    private void showResults() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        statusLabel.setText("Quiz completed!");
        timerLabel.setText("");
        startButton.setEnabled(false);
        endQuizButton.setEnabled(false);

        List<Player> players = new ArrayList<>(server.getConnectedPlayers());
        Collections.sort(players, (p1, p2) -> p2.getCorrectAnswers() - p1.getCorrectAnswers());

        if (players.isEmpty()) {
            questionLabel.setText("<html><div style='text-align: center;'>" +
                    "<h1>Quiz Completed</h1>" +
                    "<p>No players participated</p>" +
                    "</div></html>");
        } else {
            StringBuilder resultsHtml = new StringBuilder();
            resultsHtml.append("<html><div style='text-align: center;'>");
            resultsHtml.append("<h1>Quiz Results</h1>");

            if (!players.isEmpty()) {
                Player winner = players.get(0);
                resultsHtml.append("<h2>Winner: ").append(winner.getName())
                        .append(" (").append(winner.getCorrectAnswers()).append(" correct answers)</h2>");
            }

            resultsHtml.append("<table align='center' style='margin-top: 20px;'>");
            resultsHtml.append("<tr><th>Rank</th><th>Player</th><th>Correct Answers</th></tr>");

            int displayCount = Math.min(players.size(), 10);
            for (int i = 0; i < displayCount; i++) {
                Player p = players.get(i);
                String style = (i == 0) ? "style='color: gold; font-weight: bold;'" :
                        (i == 1) ? "style='color: silver; font-weight: bold;'" :
                                (i == 2) ? "style='color: #cd7f32; font-weight: bold;'" : "";

                resultsHtml.append("<tr ").append(style).append(">");
                resultsHtml.append("<td>").append(i + 1).append("</td>");
                resultsHtml.append("<td>").append(p.getName()).append("</td>");
                resultsHtml.append("<td>").append(p.getCorrectAnswers()).append("</td>");
                resultsHtml.append("</tr>");
            }

            resultsHtml.append("</table></div></html>");
            questionLabel.setText(resultsHtml.toString());
        }

        server.broadcastResults(players);
    }

    private void updatePlayerList(List<Player> players) {
        SwingUtilities.invokeLater(() -> {
            playerListPanel.removeAll();

            if (players.isEmpty()) {
                JLabel noPlayersLabel = new JLabel("No players yet");
                noPlayersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                playerListPanel.add(noPlayersLabel);
            } else {
                List<Player> sortedPlayers = new ArrayList<>(players);
                Collections.sort(sortedPlayers, (p1, p2) -> p2.getCorrectAnswers() - p1.getCorrectAnswers());

                for (Player player : sortedPlayers) {
                    JPanel playerPanel = createPlayerPanel(player);
                    playerListPanel.add(playerPanel);
                    playerListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }

            playerListPanel.revalidate();
            playerListPanel.repaint();
        });
    }

    private JPanel createPlayerPanel(Player player) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(ColorScheme.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.SECONDARY, 1));
        panel.setMaximumSize(new Dimension(230, 40));
        panel.setPreferredSize(new Dimension(230, 40));

        JLabel nameLabel = new JLabel(player.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel correctAnswersLabel = new JLabel(String.valueOf(player.getCorrectAnswers()));
        correctAnswersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        correctAnswersLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        correctAnswersLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(correctAnswersLabel, BorderLayout.EAST);

        return panel;
    }
}