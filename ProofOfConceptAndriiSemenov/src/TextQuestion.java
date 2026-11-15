public class TextQuestion extends Question {

    private String answer;

    public TextQuestion(
            int id,
            String questionText,
            int points,
            String explanation,
            String answer
    ) {
        super(id, questionText, points, explanation);
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
        if (answer == null || this.answer == null) {
            return false;
        }

        return answer.trim().equalsIgnoreCase(this.answer.trim());
    }

    @Override
    public QuestionType getType() {
        return QuestionType.TEXT;
    }
}