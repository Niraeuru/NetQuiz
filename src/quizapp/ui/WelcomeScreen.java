package quizapp.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import quizapp.ui.host.HostSetupScreen;
import quizapp.ui.player.PlayerJoinScreen;
import quizapp.util.ColorScheme;

/**
 * The welcome screen that allows users to choose between Host and Player roles.
 */
public class WelcomeScreen extends JFrame {
    
    private JPanel mainPanel;
    private JButton hostButton;
    private JButton playerButton;
    private JLabel titleLabel;
    
    public WelcomeScreen() {
        initComponents();
        setupLayout();
        addListeners();
        
        setTitle("Quiz Game");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setBackground(ColorScheme.BACKGROUND);
        
        titleLabel = new JLabel("Quiz Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(ColorScheme.PRIMARY_TEXT);
        
        hostButton = new JButton("Create a Quiz (Host)");
        styleButton(hostButton, ColorScheme.PRIMARY);
        
        playerButton = new JButton("Join a Quiz (Player)");
        styleButton(playerButton, ColorScheme.SECONDARY);
    }
    
    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(250, 60));
    }
    
    private void setupLayout() {
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 40, 0);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(hostButton, gbc);
        
        gbc.gridy = 2;
        mainPanel.add(playerButton, gbc);
        
        getContentPane().add(mainPanel);
    }
    
    private void addListeners() {
        hostButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openHostScreen();
            }
        });
        
        playerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPlayerScreen();
            }
        });
    }
    
    private void openHostScreen() {
        HostSetupScreen hostScreen = new HostSetupScreen();
        hostScreen.setVisible(true);
        dispose();
    }
    
    private void openPlayerScreen() {
        PlayerJoinScreen playerScreen = new PlayerJoinScreen();
        playerScreen.setVisible(true);
        dispose();
    }
}