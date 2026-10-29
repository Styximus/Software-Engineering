package de.quizmaster;

public class TrueFalseQuestion extends Question{
    
    private boolean answer;

    public TrueFalseQuestion(int id, Object format, boolean answer) {
        super(id, format);
        this.answer = answer;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }
}
