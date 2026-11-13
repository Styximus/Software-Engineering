
package de.quizmaster;

import java.util.Arrays;

public class Quiz {

    private int id;
    private int maxPoints;
    private Question[] questions;
    private int starsAchieved;

    public Quiz(int id, Question[] questions) {
        this.id = id;
        this.questions = questions.clone();
        this.starsAchieved = 0;
        determineMaxPoints();
    }

    public int getId() {
        return id;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public Question[] getQuestions() {
        return questions.clone();
    }

    public int getStarsAchieved() {
        return starsAchieved;
    }

    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }

        questions = Arrays.copyOf(questions, questions.length + 1);
        questions[questions.length - 1] = question;

        determineMaxPoints();
    }

    public void removeQuestion(int questionNumber) {
        if (questionNumber < 0 || questionNumber >= questions.length) {
            return;
        }

        Question[] newQuestions = new Question[questions.length - 1];

        for (int i = 0, j = 0; i < questions.length; i++) {
            if (i != questionNumber) {
                newQuestions[j++] = questions[i];
            }
        }

        questions = newQuestions;
        determineMaxPoints();
    }

    private void determineMaxPoints() {
        maxPoints = 0;

        for (Question question : questions) {
            maxPoints += question.getPoints();
        }
    }

    public void calculateStars(int achievedPoints) {
        if (maxPoints == 0) {
            starsAchieved = 0;
            return;
        }

        double percentage = (double) achievedPoints / maxPoints;

        if (percentage >= 0.9) {
            starsAchieved = 3;
        } else if (percentage >= 0.7) {
            starsAchieved = 2;
        } else if (percentage >= 0.5) {
            starsAchieved = 1;
        } else {
            starsAchieved = 0;
        }
    }
}
