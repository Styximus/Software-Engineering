package de.quizmaster;

public class TextQuestion extends Question{

    private String answer;

    public TextQuestion(int id, String answer, Object format) {
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
