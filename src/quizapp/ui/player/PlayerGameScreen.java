package quizapp.ui.player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import quizapp.model.Player;
import quizapp.model.Question;
import quizapp.network.GameClient;
import quizapp.ui.WelcomeScreen;
import quizapp.util.ColorScheme;

public class PlayerGameScreen extends JFrame {

    private JPanel mainPanel;
    private JPanel questionPanel;
    private JPanel answerPanel;
    private JPanel waitingPanel;
    private JPanel resultsPanel;
    private JLabel timerLabel;
    private JLabel questionLabel;
    private JLabel statusLabel;
    private JLabel correctAnswersLabel;
    private JButton[] answerButtons;
    private JButton exitButton;

    private GameClient client;
    private Question currentQuestion;
    private int selectedAnswer = -1;
    private int correctAnswers = 0;

    public PlayerGameScreen(GameClient client) {
        this.client = client;

        initComponents();
        setupLayout();
        addListeners();

        setTitle("Quiz Game - Player");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showWaitingScreen("Waiting for the host to start the quiz...");
        setupClientCallbacks();
    }

    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setBackground(ColorScheme.BACKGROUND);

        questionPanel = new JPanel();
        questionPanel.setLayout(new BorderLayout(10, 20));
        questionPanel.setBackground(ColorScheme.CARD_BACKGROUND);
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        timerLabel.setForeground(ColorScheme.PRIMARY);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        questionLabel = new JLabel("Waiting for question...");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setForeground(ColorScheme.PRIMARY_TEXT);
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        questionPanel.add(timerLabel, BorderLayout.NORTH);
        questionPanel.add(questionLabel, BorderLayout.CENTER);

        answerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        answerPanel.setBackground(ColorScheme.BACKGROUND);
        answerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        answerButtons = new JButton[4];
        Color[] optionColors = {
                ColorScheme.OPTION_A,
                ColorScheme.OPTION_B,
                ColorScheme.OPTION_C,
                ColorScheme.OPTION_D
        };

        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JButton("Option " + (char)('A' + i));
            answerButtons[i].setBackground(optionColors[i]);
            answerButtons[i].setForeground(Color.WHITE);
            answerButtons[i].setFont(new Font("Arial", Font.BOLD, 18));
            answerButtons[i].setFocusPainted(false);
            answerButtons[i].setPreferredSize(new Dimension(150, 80));
            final int index = i;
            answerButtons[i].addActionListener(e -> selectAnswer(index));
            answerPanel.add(answerButtons[i]);
        }

        waitingPanel = new JPanel(new BorderLayout());
        waitingPanel.setBackground(ColorScheme.BACKGROUND);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(ColorScheme.BACKGROUND);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        statusLabel = new JLabel("Connected to quiz");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(ColorScheme.SECONDARY_TEXT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        correctAnswersLabel = new JLabel("Correct Answers: 0");
        correctAnswersLabel.setFont(new Font("Arial", Font.BOLD, 18));
        correctAnswersLabel.setForeground(ColorScheme.PRIMARY);
        correctAnswersLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        exitButton = new JButton("Exit Quiz");
        exitButton.setBackground(Color.LIGHT_GRAY);
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setFocusPainted(false);
    }

    private void setupLayout() {
        mainPanel.setLayout(new BorderLayout(20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel nameLabel = new JLabel("Player: " + client.getPlayerName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);

        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(correctAnswersLabel, BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ColorScheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(waitingPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ColorScheme.BACKGROUND);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBackground(ColorScheme.BACKGROUND);
        statusPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(ColorScheme.BACKGROUND);
        buttonPanel.add(exitButton);

        bottomPanel.add(statusPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private void addListeners() {
        exitButton.addActionListener(e -> exitQuiz());
    }

    private void setupClientCallbacks() {
        client.setQuestionCallback((question, questionNumber, totalQuestions) -> {
            SwingUtilities.invokeLater(() -> {
                currentQuestion = question;
                selectedAnswer = -1;  // Reset selected answer for new question
                showQuestion(question, questionNumber, totalQuestions);
            });
        });

        client.setTimerCallback((seconds) -> {
            SwingUtilities.invokeLater(() -> {
                updateTimer(seconds);
            });
        });

        client.setTimeUpCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                timeUp();
            });
        });

        client.setResultsCallback((players) -> {
            SwingUtilities.invokeLater(() -> {
                showResults(players);
            });
        });

        client.setDisconnectCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                handleDisconnect();
            });
        });
    }

    private void showQuestion(Question question, int questionNumber, int totalQuestions) {
        selectedAnswer = -1;

        questionLabel.setText("<html><div style='text-align: center;'>" +
                "Question " + questionNumber + "/" + totalQuestions +
                "<br><br>" + question.getText() + "</div></html>");

        String[] options = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            answerButtons[i].setText("<html><div style='text-align: center;'>" +
                    (char)('A' + i) + "<br>" + options[i] + "</div></html>");
            answerButtons[i].setEnabled(true);
            answerButtons[i].setBorder(null);  // Reset any previous borders
        }

        Container contentPane = mainPanel.getParent();
        if (contentPane instanceof JPanel) {
            JPanel centerPanel = (JPanel) ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            centerPanel.removeAll();
            centerPanel.setLayout(new BorderLayout());
            centerPanel.add(questionPanel, BorderLayout.NORTH);
            centerPanel.add(answerPanel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
        }

        statusLabel.setText("Question in progress. Select an answer.");
    }

    private void showWaitingScreen(String message) {
        JPanel centerPanel = (JPanel) ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        centerPanel.removeAll();

        JLabel waitingLabel = new JLabel(message);
        waitingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        waitingLabel.setForeground(ColorScheme.PRIMARY_TEXT);
        waitingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel spinnerPanel = new JPanel(new BorderLayout());
        spinnerPanel.setBackground(ColorScheme.BACKGROUND);
        spinnerPanel.add(waitingLabel, BorderLayout.CENTER);

        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(spinnerPanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();

        statusLabel.setText("Waiting for host...");
    }

    private void selectAnswer(int index) {
        if (selectedAnswer != -1) {
            return;
        }

        selectedAnswer = index;

        for (int i = 0; i < answerButtons.length; i++) {
            if (i == index) {
                answerButtons[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
            } else {
                answerButtons[i].setEnabled(false);
            }
        }

        client.sendAnswer(index);
        statusLabel.setText("Answer submitted! Waiting for next question...");
    }

    private void updateTimer(int seconds) {
        if (seconds < 0) return;  // Prevent negative timer values
        
        timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));

        if (seconds <= 5) {
            timerLabel.setForeground(ColorScheme.ERROR);
        } else if (seconds <= 10) {
            timerLabel.setForeground(ColorScheme.WARNING);
        } else {
            timerLabel.setForeground(ColorScheme.PRIMARY);
        }
    }

    private void timeUp() {
        for (JButton button : answerButtons) {
            button.setEnabled(false);
        }

        if (selectedAnswer == -1) {
            statusLabel.setText("Time's up! You didn't answer.");
        } else if (selectedAnswer == currentQuestion.getCorrectAnswerIndex()) {
            statusLabel.setText("Correct! Well done!");
        } else {
            statusLabel.setText("Incorrect.");
        }

        showWaitingScreen("Waiting for next question...");
    }

    private void showResults(List<Player> players) {
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(ColorScheme.CARD_BACKGROUND);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Quiz Results");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Player currentPlayer = null;
        for (Player p : players) {
            if (p.getName().equals(client.getPlayerName())) {
                currentPlayer = p;
                break;
            }
        }

        JLabel playerResultLabel;
        if (currentPlayer != null) {
            playerResultLabel = new JLabel(String.format("You got %d correct answers",
                    currentPlayer.getCorrectAnswers()));
        } else {
            playerResultLabel = new JLabel("You did not finish the quiz");
        }
        playerResultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        playerResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultsPanel.add(titleLabel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resultsPanel.add(playerResultLabel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel topPlayersLabel = new JLabel("Leaderboard");
        topPlayersLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPlayersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(topPlayersLabel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Create a table-like panel for the leaderboard
        JPanel leaderboardPanel = new JPanel();
        leaderboardPanel.setLayout(new BoxLayout(leaderboardPanel, BoxLayout.Y_AXIS));
        leaderboardPanel.setBackground(ColorScheme.CARD_BACKGROUND);
        leaderboardPanel.setMaximumSize(new Dimension(400, 300));

        // Add header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        headerPanel.setBackground(ColorScheme.CARD_BACKGROUND);
        headerPanel.add(new JLabel("Rank"));
        headerPanel.add(new JLabel("Player"));
        headerPanel.add(new JLabel("Correct Answers"));
        resultsPanel.add(headerPanel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        int displayCount = Math.min(players.size(), 10);
        for (int i = 0; i < displayCount; i++) {
            Player p = players.get(i);
            boolean isCurrentPlayer = p.getName().equals(client.getPlayerName());

            JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            playerPanel.setBackground(isCurrentPlayer ? ColorScheme.PRIMARY : ColorScheme.CARD_BACKGROUND);
            playerPanel.setMaximumSize(new Dimension(400, 30));

            String rankText = (i == 0) ? "🥇" : (i == 1) ? "🥈" : (i == 2) ? "🥉" : String.valueOf(i + 1);
            JLabel rankLabel = new JLabel(rankText);
            rankLabel.setFont(new Font("Arial", Font.BOLD, 16));
            rankLabel.setForeground(isCurrentPlayer ? Color.WHITE : ColorScheme.PRIMARY_TEXT);

            JLabel nameLabel = new JLabel(p.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            nameLabel.setForeground(isCurrentPlayer ? Color.WHITE : ColorScheme.PRIMARY_TEXT);

            JLabel scoreLabel = new JLabel(p.getCorrectAnswers() + " correct");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
            scoreLabel.setForeground(isCurrentPlayer ? Color.WHITE : ColorScheme.PRIMARY_TEXT);

            playerPanel.add(rankLabel);
            playerPanel.add(nameLabel);
            playerPanel.add(scoreLabel);

            resultsPanel.add(playerPanel);
            resultsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JButton playAgainButton = new JButton("Return to Main Menu");
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.setBackground(ColorScheme.PRIMARY);
        playAgainButton.setForeground(Color.WHITE);
        playAgainButton.setFocusPainted(false);
        playAgainButton.addActionListener(e -> exitQuiz());

        resultsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resultsPanel.add(playAgainButton);

        JPanel centerPanel = (JPanel) ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();

        statusLabel.setText("Quiz completed!");
    }

    private void handleDisconnect() {
        JOptionPane.showMessageDialog(this,
                "Connection to the host was lost. The quiz may have ended or there was a network error.",
                "Disconnected", JOptionPane.WARNING_MESSAGE);

        exitQuiz();
    }

    private void exitQuiz() {
        client.disconnect();
        WelcomeScreen welcomeScreen = new WelcomeScreen();
        welcomeScreen.setVisible(true);
        dispose();
    }
}