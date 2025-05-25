package quizapp.ui.host;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import quizapp.model.Question;
import quizapp.model.Quiz;
import quizapp.network.GameServer;
import quizapp.util.ColorScheme;

public class HostSetupScreen extends JFrame {

    private JPanel mainPanel;
    private JPanel questionListPanel;
    private JTextField quizNameField;
    private JButton addQuestionButton;
    private JButton removeSelectedButton;
    private JButton startQuizButton;
    private JLabel statusLabel;
    private JLabel roomCodeLabel;
    private JSpinner timerSpinner;
    private DefaultListModel<String> questionListModel;
    private JList<String> questionList;

    private Quiz quiz;
    private ArrayList<Question> questions;
    private GameServer gameServer;

    public HostSetupScreen() {
        questions = new ArrayList<>();
        quiz = new Quiz();

        initComponents();
        addListeners();

        setTitle("Quiz Setup - Host");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(ColorScheme.BACKGROUND);

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        headerPanel.setBackground(ColorScheme.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        roomCodeLabel = new JLabel("Room Code: " + quiz.roomCode);
        roomCodeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        roomCodeLabel.setForeground(Color.WHITE);

        JLabel timerLabel = new JLabel("Question Timer (sec):");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setForeground(Color.WHITE);

        timerSpinner = new JSpinner(new SpinnerNumberModel(10, 5, 60, 5));
        timerSpinner.setPreferredSize(new Dimension(60, 25));

        headerPanel.add(roomCodeLabel);
        headerPanel.add(Box.createHorizontalStrut(50));
        headerPanel.add(timerLabel);
        headerPanel.add(timerSpinner);

        // Questions Panel
        JPanel questionsPanel = new JPanel(new BorderLayout(10, 10));
        questionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ColorScheme.PRIMARY, 2),
                "Questions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                ColorScheme.PRIMARY
        ));

        questionListModel = new DefaultListModel<>();
        questionList = new JList<>(questionListModel);
        questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionList.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane questionScrollPane = new JScrollPane(questionList);
        questionScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel questionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addQuestionButton = new JButton("Add Question");
        removeSelectedButton = new JButton("Remove Selected");
        styleButton(addQuestionButton, ColorScheme.PRIMARY);
        styleButton(removeSelectedButton, ColorScheme.ERROR);

        questionButtonPanel.add(addQuestionButton);
        questionButtonPanel.add(removeSelectedButton);

        questionsPanel.add(questionScrollPane, BorderLayout.CENTER);
        questionsPanel.add(questionButtonPanel, BorderLayout.SOUTH);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(mainPanel.getBackground());

        startQuizButton = new JButton("Start Quiz");
        styleButton(startQuizButton, ColorScheme.PRIMARY);
        startQuizButton.setEnabled(false);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(mainPanel.getBackground());
        JLabel infoLabel1 = new JLabel("Questions will automatically advance after the timer expires.");
        JLabel infoLabel2 = new JLabel("Players receive 100 points for correct answers.");
        infoLabel1.setForeground(ColorScheme.SECONDARY_TEXT);
        infoLabel2.setForeground(ColorScheme.SECONDARY_TEXT);
        infoPanel.add(infoLabel1);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(infoLabel2);

        bottomPanel.add(infoPanel, BorderLayout.CENTER);
        bottomPanel.add(startQuizButton, BorderLayout.SOUTH);

        // Add all components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(questionsPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 35));
    }

    private void addListeners() {
        addQuestionButton.addActionListener(e -> showAddQuestionDialog());

        removeSelectedButton.addActionListener(e -> {
            int selectedIndex = questionList.getSelectedIndex();
            if (selectedIndex != -1) {
                questionListModel.remove(selectedIndex);
                questions.remove(selectedIndex);
                updateStartButton();
            }
        });

        startQuizButton.addActionListener(e -> startQuiz());
    }

    private void showAddQuestionDialog() {
        AddQuestionDialog dialog = new AddQuestionDialog(this,(Integer) timerSpinner.getValue());
        Question question = dialog.showDialog();


        if (question != null) {
            questions.add(question);
            // Format the question display
            String displayText = String.format("Q%d: %s", questions.size(), question.getText());
            questionListModel.addElement(displayText);
            updateStartButton();
        }
    }

    private void updateStartButton() {
        startQuizButton.setEnabled(!questions.isEmpty());
    }

    private void startQuiz() {
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please add at least one question",
                    "No Questions",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        quiz.setQuestions(questions);

        try {
            gameServer = new GameServer(quiz, quiz.roomCode);
            gameServer.start();


            HostGameScreen gameScreen = new HostGameScreen(quiz, gameServer);
            gameScreen.setVisible(true);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error starting server: " + e.getMessage(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}