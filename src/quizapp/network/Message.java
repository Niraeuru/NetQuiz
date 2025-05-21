package quizapp.network;

import java.io.Serializable;
import java.util.List;
import quizapp.model.Player;
import quizapp.model.Question;

/**
 * Message class for communication between client and server.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String playerName;
    private String roomCode;
    private String errorMessage;

    // For questions
    private Question question;
    private int questionNumber;
    private int totalQuestions;

    // For answers
    private int answerIndex;

    // For timer
    private int timeRemaining;

    // For results
    private List<Player> playerResults;

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(MessageType type, String errorMessage) {
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(int answerIndex) {
        this.answerIndex = answerIndex;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public List<Player> getPlayerResults() {
        return playerResults;
    }

    public void setPlayerResults(List<Player> playerResults) {
        this.playerResults = playerResults;
    }
}