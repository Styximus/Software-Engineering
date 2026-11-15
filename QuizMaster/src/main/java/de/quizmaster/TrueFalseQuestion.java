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
        if (answer == null) return false;
        String value = answer.trim();
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return false;
        return Boolean.parseBoolean(value) == this.answer;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.TRUE_FALSE;
    }
}
