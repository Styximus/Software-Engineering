package de.quizmaster;

public class TextQuestion extends Question{

    private String answer;

    public TextQuestion(int id,String questionText, String answer, int points) {
        super(id, questionText,points);
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public boolean checkAnswer(String answer) {
        return answer.trim().equalsIgnoreCase(this.answer.trim());
    }

    @Override
    public QuestionType getType() {
        return QuestionType.TEXT;
    }
}
