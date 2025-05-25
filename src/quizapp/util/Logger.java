package quizapp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String CSV_DIRECTORY = "logs/";
    private String csvFileName;
    private FileWriter csvWriter;

    public Logger(String roomCode) {
        initializeCSV(roomCode);
    }

    private void initializeCSV(String roomCode) {
        try {
            // Create logs directory if it doesn't exist
            java.io.File directory = new java.io.File(CSV_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create filename with room code and timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            csvFileName = CSV_DIRECTORY + "quiz_" + roomCode + "_" + timestamp + ".csv";

            // Initialize CSV writer and write header
            csvWriter = new FileWriter(csvFileName);
            csvWriter.write("Timestamp,PlayerName,QuestionNumber,Answer,CorrectAnswer,TimeTaken\n");
            csvWriter.flush();
        } catch (IOException e) {
            System.err.println("Error initializing CSV file: " + e.getMessage());
        }
    }

    public void logAnswer(String playerName, int questionNumber, String answer, String correctAnswer, long timeTaken) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String line = String.format("%s,%s,%d,%s,%s,%d\n",
                    timestamp, playerName, questionNumber, answer, correctAnswer, timeTaken);
            csvWriter.write(line);
            csvWriter.flush();
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing CSV file: " + e.getMessage());
        }
    }
}
