package quizapp.ui.player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import quizapp.model.Player;
import quizapp.model.Question;
import quizapp.network.GameClient;
import quizapp.ui.WelcomeScreen;
import quizapp.util.ColorScheme;

/**
 * Screen for players to participate in the quiz game.
 */
public class PlayerGameScreen extends JFrame {
    
    private JPanel mainPanel;
    private JPanel questionPanel;
    private JPanel answerPanel;
    private JPanel waitingPanel;
    private JPanel resultsPanel;
    private JLabel timerLabel;
    private JLabel questionLabel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
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
        
        // Start in waiting mode
        showWaitingScreen("Waiting for the host to start the quiz...");
        
        // Set up client callbacks
        setupClientCallbacks();
    }
    
    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setBackground(ColorScheme.BACKGROUND);
        
        // Question panel
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
        
        // Answer panel
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
            answerButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectAnswer(index);
                }
            });
            answerPanel.add(answerButtons[i]);
        }
        
        // Waiting panel
        waitingPanel = new JPanel(new BorderLayout());
        waitingPanel.setBackground(ColorScheme.BACKGROUND);
        
        JLabel waitingLabel = new JLabel("Waiting for the host...");
        waitingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        waitingLabel.setForeground(ColorScheme.PRIMARY_TEXT);
        waitingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        waitingPanel.add(waitingLabel, BorderLayout.CENTER);
        
        // Results panel
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(ColorScheme.BACKGROUND);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Status and score labels
        statusLabel = new JLabel("Connected to quiz");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(ColorScheme.SECONDARY_TEXT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setForeground(ColorScheme.PRIMARY);
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Exit button
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
        headerPanel.add(scoreLabel, BorderLayout.EAST);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ColorScheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // We'll swap between different panels in the center
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
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitQuiz();
            }
        });
    }
    
    private void setupClientCallbacks() {
        client.setQuestionCallback((question, questionNumber, totalQuestions) -> {
            SwingUtilities.invokeLater(() -> {
                currentQuestion = question;
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
        
        client.setScoreUpdateCallback((score) -> {
            SwingUtilities.invokeLater(() -> {
                updateScore(score);
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
        // Reset answer selection
        selectedAnswer = -1;
        
        // Update question text
        questionLabel.setText("<html><div style='text-align: center;'>" + 
                             "Question " + questionNumber + "/" + totalQuestions +
                             "<br><br>" + question.getText() + "</div></html>");
        
        // Update answer buttons
        String[] options = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            answerButtons[i].setText("<html><div style='text-align: center;'>" + 
                                    (char)('A' + i) + "<br>" + options[i] + "</div></html>");
            answerButtons[i].setEnabled(true);
            answerButtons[i].setBorder(null);
        }
        
        // Show question and answer panels
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
            return; // Already answered
        }
        
        selectedAnswer = index;
        
        // Visual feedback
        for (int i = 0; i < answerButtons.length; i++) {
            if (i == index) {
                answerButtons[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
            } else {
                answerButtons[i].setEnabled(false);
            }
        }
        
        // Send answer to server
        client.sendAnswer(index);
        
        statusLabel.setText("Answer submitted! Waiting for next question...");
    }
    
    private void updateTimer(int seconds) {
        timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        
        // Change color based on time remaining
        if (seconds <= 5) {
            timerLabel.setForeground(ColorScheme.ERROR);
        } else if (seconds <= 10) {
            timerLabel.setForeground(ColorScheme.WARNING);
        } else {
            timerLabel.setForeground(ColorScheme.PRIMARY);
        }
    }
    
    private void timeUp() {
        // Disable answer buttons
        for (JButton button : answerButtons) {
            button.setEnabled(false);
        }
        
        // Show correct answer
        int correctIndex = currentQuestion.getCorrectAnswerIndex();
        answerButtons[correctIndex].setBorder(BorderFactory.createLineBorder(ColorScheme.SUCCESS, 4));
        
        // If player didn't answer
        if (selectedAnswer == -1) {
            statusLabel.setText("Time's up! You didn't answer. The correct answer was " + (char)('A' + correctIndex));
        } 
        // If player answered correctly
        else if (selectedAnswer == correctIndex) {
            statusLabel.setText("Correct! Well done!");
        } 
        // If player answered incorrectly
        else {
            statusLabel.setText("Incorrect. The correct answer was " + (char)('A' + correctIndex));
        }
        
        showWaitingScreen("Waiting for next question...");
    }
    
    private void updateScore(int score) {
        scoreLabel.setText("Correct Answers: " + score);
    }
    
    private void showResults(List<Player> players) {
        // Create results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(ColorScheme.CARD_BACKGROUND);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Quiz Results");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Find current player and their rank
        Player currentPlayer = null;
        int playerRank = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(client.getPlayerName())) {
                currentPlayer = players.get(i);
                playerRank = i + 1;
                break;
            }
        }
        
        JLabel playerResultLabel = new JLabel();
        if (currentPlayer != null) {
            playerResultLabel.setText(String.format("You finished in position %d with %d correct answers",
                    playerRank, currentPlayer.getCorrectAnswers()));
        } else {
            playerResultLabel.setText("You did not finish the quiz");
        }
        playerResultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        playerResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        resultsPanel.add(titleLabel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resultsPanel.add(playerResultLabel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Show top players
        JLabel topPlayersLabel = new JLabel("Leaderboard:");
        topPlayersLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPlayersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(topPlayersLabel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Create table header
        JPanel headerPanel = new JPanel(new GridLayout(1, 3));
        headerPanel.setBackground(ColorScheme.CARD_BACKGROUND);
        headerPanel.setMaximumSize(new Dimension(400, 30));
        
        JLabel rankHeader = new JLabel("Rank", SwingConstants.CENTER);
        JLabel nameHeader = new JLabel("Player", SwingConstants.CENTER);
        JLabel scoreHeader = new JLabel("Correct Answers", SwingConstants.CENTER);
        
        headerPanel.add(rankHeader);
        headerPanel.add(nameHeader);
        headerPanel.add(scoreHeader);
        resultsPanel.add(headerPanel);
        
        // Add player rows
        int displayCount = Math.min(players.size(), 10);
        for (int i = 0; i < displayCount; i++) {
            Player p = players.get(i);
            
            JPanel playerPanel = new JPanel(new GridLayout(1, 3));
            playerPanel.setBackground(ColorScheme.CARD_BACKGROUND);
            playerPanel.setMaximumSize(new Dimension(400, 30));
            
            // Add medal icon for top 3
            String rankText = (i == 0) ? "🥇" : (i == 1) ? "🥈" : (i == 2) ? "🥉" : String.valueOf(i + 1);
            
            JLabel rankLabel = new JLabel(rankText, SwingConstants.CENTER);
            JLabel nameLabel = new JLabel(p.getName(), SwingConstants.CENTER);
            JLabel scoreLabel = new JLabel(String.valueOf(p.getCorrectAnswers()), SwingConstants.CENTER);
            
            // Highlight current player
            if (p.getName().equals(client.getPlayerName())) {
                rankLabel.setForeground(ColorScheme.PRIMARY);
                nameLabel.setForeground(ColorScheme.PRIMARY);
                scoreLabel.setForeground(ColorScheme.PRIMARY);
                rankLabel.setFont(rankLabel.getFont().deriveFont(Font.BOLD));
                nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
                scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD));
            }
            
            playerPanel.add(rankLabel);
            playerPanel.add(nameLabel);
            playerPanel.add(scoreLabel);
            
            resultsPanel.add(playerPanel);
        }
        
        // Add a "Return to Main Menu" button
        JButton playAgainButton = new JButton("Return to Main Menu");
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.setBackground(ColorScheme.PRIMARY);
        playAgainButton.setForeground(Color.WHITE);
        playAgainButton.setFocusPainted(false);
        playAgainButton.addActionListener(e -> exitQuiz());
        
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resultsPanel.add(playAgainButton);
        
        // Show the results panel
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
            "Connection to the host was lost. The quiz may have ended or there was a quizapp.network error.",
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