package quizapp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import quizapp.ui.WelcomeScreen;

/**
 * Main entry point for the Quiz Application.
 * Initializes the application and displays the welcome screen.
 */
public class QuizApp {
    
    public static void main(String[] args) {
        try {
            // Set the look and feel to the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            WelcomeScreen welcomeScreen = new WelcomeScreen();
            welcomeScreen.setVisible(true);
        });
    }
}