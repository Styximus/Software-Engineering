package de.quizmaster;

public class MultipleChoiceQuestion extends Question{
    
    private int questionAmount;
    private int[] answers;

    public MultipleChoiceQuestion(int id, int questionAmount, int[] answers, Object format) {
        super(id, format);
        this.questionAmount = questionAmount;
        this.answers = answers;
    }

    public int[] getAnswers() {
        return answers;
    }

    public void setAnswers(int[] answers) {
        this.answers = answers;
    }
}
