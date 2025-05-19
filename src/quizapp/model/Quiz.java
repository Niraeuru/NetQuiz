package quizapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a quiz with multiple questions.
 */
public class Quiz implements Serializable {
    
    private String name;
    private List<Question> questions;
    
    public Quiz() {
        this.questions = new ArrayList<>();
    }
    
    public Quiz(String name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    public void addQuestion(Question question) {
        questions.add(question);
    }
    
    public void removeQuestion(Question question) {
        questions.remove(question);
    }
    
    public Question getQuestionAt(int index) {
        if (index >= 0 && index < questions.size()) {
            return questions.get(index);
        }
        return null;
    }
    
    public int getQuestionCount() {
        return questions.size();
    }
}