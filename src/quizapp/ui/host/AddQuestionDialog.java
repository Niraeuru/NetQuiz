package quizapp.ui.host;

import javax.swing.*;
import java.awt.*;
import quizapp.model.Question;
import quizapp.util.ColorScheme;

public class AddQuestionDialog extends JDialog {

    private JTextField questionField;
    private JTextField[] optionFields;
    private JRadioButton[] correctAnswerRadios;
    private JButton saveButton;
    private JButton cancelButton;
    private Question result;
    private JSpinner timerSpinner;
    private int time = 10;

    public AddQuestionDialog(JFrame parent,int globalTime) {
        super(parent, "Add Question", true);
        time = globalTime;
        initComponents();
        setupLayout();
        addListeners();

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        // Question input
        questionField = new JTextField(30);

        // Options with radio buttons
        optionFields = new JTextField[4];
        correctAnswerRadios = new JRadioButton[4];
        ButtonGroup radioGroup = new ButtonGroup();

        //Timer Field
        System.out.println(time);
        timerSpinner = new JSpinner(new SpinnerNumberModel( time, 5, 60, 5));
        timerSpinner.setPreferredSize(new Dimension(60, 25));

        for (int i = 0; i < 4; i++) {
            optionFields[i] = new JTextField(25);
            correctAnswerRadios[i] = new JRadioButton("Correct");
            radioGroup.add(correctAnswerRadios[i]);
        }

        // Default select first option
        correctAnswerRadios[0].setSelected(true);

        // Buttons
        saveButton = new JButton("Save Question");
        saveButton.setBackground(ColorScheme.PRIMARY);
        saveButton.setForeground(Color.black);

        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.LIGHT_GRAY);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Question
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        mainPanel.add(new JLabel("Question:"), gbc);

        gbc.gridy = 1;
        mainPanel.add(questionField, gbc);

        // Options
        String[] optionLabels = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            gbc.gridy = i * 2 + 2;
            gbc.gridwidth = 1;
            mainPanel.add(new JLabel("Option " + optionLabels[i] + ":"), gbc);

            gbc.gridy = i * 2 + 3;
            gbc.gridwidth = 2;
            mainPanel.add(optionFields[i], gbc);

            gbc.gridx = 2;
            gbc.gridwidth = 1;
            mainPanel.add(correctAnswerRadios[i], gbc);

            gbc.gridx = 0;
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(new JLabel("Timer:"));
        System.out.println(time);
        buttonPanel.add(timerSpinner);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        saveButton.addActionListener(e -> {
            if (validateInput()) {
                saveQuestion();
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            result = null;
            dispose();
        });
    }

    private boolean validateInput() {
        if (questionField.getText().trim().isEmpty()) {
            showError("Please enter a question");
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (optionFields[i].getText().trim().isEmpty()) {
                showError("Please enter all four options");
                return false;
            }
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private void saveQuestion() {
        String questionText = questionField.getText().trim();
        String[] options = new String[4];
        int correctAnswerIndex = 0;

        for (int i = 0; i < 4; i++) {
            options[i] = optionFields[i].getText().trim();
            if (correctAnswerRadios[i].isSelected()) {
                correctAnswerIndex = i;
            }
        }

        // Get timer value from HostSetupScreen
        result = new Question(questionText, options, correctAnswerIndex, (Integer) timerSpinner.getValue());
    }

    public Question showDialog() {
        setVisible(true);
        return result;
    }
}