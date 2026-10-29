package de.quizmaster;

public class Quiz {

    private int id;
    private int maxPoints;
    private Question[] questions;
    private int starsAchieved;

    public Quiz(int id, int maxPoints, Question[] questions, int starsAchieved) {
        this.id = id;
        this.maxPoints = maxPoints;
        this.questions = questions;
        this.starsAchieved = starsAchieved;
    }

    public int getId() {
        return id;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public Question[] getQuestions() {
        return questions;
    }

    public int getStarsAchieved() {
        return starsAchieved;
    }
    
    public void addQuestion(Question question) {
        return;
    }

    public void removeQuestion(int questionNumber) {
        return;
    }

    private void determineMaxPoints() {
        return;
    }
}
