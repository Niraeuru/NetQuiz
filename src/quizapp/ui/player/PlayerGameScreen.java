package quizapp.ui.player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
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
    private JButton[] answerButtons;
    private JButton exitButton;

    private GameClient client;
    private Question currentQuestion;
    private int selectedAnswer = -1;

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
                showWaitingScreen("Quiz completed! Waiting for host...");
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

        // Show waiting screen immediately after time up
        showWaitingScreen("Waiting for next question...");
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