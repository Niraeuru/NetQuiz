package quizapp.ui.player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import quizapp.network.GameClient;
import quizapp.ui.WelcomeScreen;
import quizapp.util.ColorScheme;

/**
 * Screen for players to join a quiz by entering a room code and player name.
 */
public class PlayerJoinScreen extends JFrame {

    private JPanel mainPanel;
    private JTextField nameField;
    private JTextField roomCodeField;
    private JTextField hostIPField;  // New field for host IP
    private JButton joinButton;
    private JButton backButton;
    private JLabel statusLabel;

    public PlayerJoinScreen() {
        initComponents();
        setupLayout();
        addListeners();

        setTitle("Join Quiz - Player");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setBackground(ColorScheme.BACKGROUND);

        JLabel titleLabel = new JLabel("Join a Quiz");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(ColorScheme.PRIMARY_TEXT);

        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setForeground(ColorScheme.PRIMARY_TEXT);

        nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel roomCodeLabel = new JLabel("Room Code:");
        roomCodeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        roomCodeLabel.setForeground(ColorScheme.PRIMARY_TEXT);

        roomCodeField = new JTextField(20);
        roomCodeField.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel hostIPLabel = new JLabel("Host IP Address:");
        hostIPLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        hostIPLabel.setForeground(ColorScheme.PRIMARY_TEXT);

        hostIPField = new JTextField(20);
        hostIPField.setFont(new Font("Arial", Font.PLAIN, 16));
        hostIPField.setText("localhost");  // Default to localhost

        joinButton = new JButton("Join Quiz");
        joinButton.setBackground(ColorScheme.PRIMARY);
        joinButton.setForeground(Color.black);
        joinButton.setFont(new Font("Arial", Font.BOLD, 16));
        joinButton.setFocusPainted(false);
        joinButton.setPreferredSize(new Dimension(150, 40));

        backButton = new JButton("Back");
        backButton.setBackground(Color.LIGHT_GRAY);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(150, 40));

        statusLabel = new JLabel("Enter room code and your name to join");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(ColorScheme.SECONDARY_TEXT);
    }

    private void setupLayout() {
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(new JLabel("Join a Quiz"), gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Your Name:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Room Code:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(roomCodeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Host IP:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(hostIPField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(ColorScheme.BACKGROUND);
        buttonPanel.add(joinButton);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, gbc);

        gbc.gridy = 5;
        mainPanel.add(statusLabel, gbc);

        getContentPane().add(mainPanel);
    }

    private void addListeners() {
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinQuiz();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });
    }

    private void joinQuiz() {
        String name = nameField.getText().trim();
        String roomCode = roomCodeField.getText().trim();
        String hostIP = hostIPField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (roomCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the room code", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (hostIP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the host IP address", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("Connecting to game...");
        joinButton.setEnabled(false);

        // Try to connect to the game
        GameClient client = new GameClient(name, roomCode, hostIP);

        try {
            boolean connected = client.connect(hostIP);

            if (connected) {
                PlayerGameScreen gameScreen = new PlayerGameScreen(client);
                gameScreen.setVisible(true);
                dispose();
            } else {
                statusLabel.setText("Could not connect to the game room");
                joinButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Could not join the game. Check the room code and host IP and try again.",
                        "Connection Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            statusLabel.setText("Error connecting to game");
            joinButton.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void goBack() {
        WelcomeScreen welcomeScreen = new WelcomeScreen();
        welcomeScreen.setVisible(true);
        dispose();
    }
}