package de.quizmaster;

public abstract class Question {

    private int id;
    private String questionText;
    private int points;
    public Question(int id, String questionText, int points) {
        this.id = id;
        this.questionText = questionText;
        this.points = points;
    }

    
    public int getId() {
        return id;
    }
    public String getQuestionText() {
        return questionText;
    }
    public int getPoints(){return points;};
    public abstract  boolean checkAnswer(String answer);
    public abstract QuestionType getType();
}
