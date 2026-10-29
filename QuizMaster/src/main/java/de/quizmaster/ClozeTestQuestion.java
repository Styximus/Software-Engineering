package de.quizmaster;

public class ClozeTestQuestion extends Question{

    private String answer;

    public ClozeTestQuestion(int id, Object format, String answer) {
        super(id, format);
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
