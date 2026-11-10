package de.quizmaster;

public class TrueFalseQuestion extends Question{
    
    private boolean answer;

    public TrueFalseQuestion(int id, String questionText, boolean answer, int points) {
        super(id, questionText, points);
        this.answer = answer;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }

    @Override
    public boolean checkAnswer(String answer) {
        return Boolean.parseBoolean(answer) == this.answer;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.TRUE_FALSE;
    }
}
