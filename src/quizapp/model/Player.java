package quizapp.model;

import java.io.Serializable;

/**
 * Represents a player in the quiz game.
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int correctAnswers;
    private boolean hasAnswered;
    private long answeredTime;

    public Player(String name) {
        this.name = name;
        this.correctAnswers = 0;
        this.hasAnswered = false;
        this.answeredTime = 0;
    }

    public String getName() {
        return name;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void incrementCorrectAnswers() {
        this.correctAnswers++;
    }

    public boolean hasAnswered() {
        return hasAnswered;
    }

    public void setHasAnswered(boolean hasAnswered) {
        this.hasAnswered = hasAnswered;
    }

    public void resetForNextQuestion() {
        this.hasAnswered = false;
        this.answeredTime = 0;
    }

    public long getAnsweredTime() {
        return answeredTime;
    }

    public void setAnsweredTime(long answeredTime) {
        this.answeredTime = answeredTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            return ((Player) obj).getName().equals(this.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}