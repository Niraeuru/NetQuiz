package quizapp.model;

import java.io.Serializable;

/**
 * Represents a single question in the quiz.
 */
public class Question implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String text;
    private String[] options;
    private int correctAnswerIndex;
    private int timeLimit; // in seconds
    
    public Question(String text, String[] options, int correctAnswerIndex, int timeLimit) {
        this.text = text;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.timeLimit = timeLimit;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String[] getOptions() {
        return options;
    }
    
    public void setOptions(String[] options) {
        this.options = options;
    }
    
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
    
    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public boolean isCorrectAnswer(int answerIndex) {
        return answerIndex == correctAnswerIndex;
    }
}