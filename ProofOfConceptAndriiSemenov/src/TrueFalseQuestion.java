public class TrueFalseQuestion extends Question {

    private boolean answer;

    public TrueFalseQuestion(
            int id,
            String questionText,
            int points,
            String explanation,
            boolean answer
    ) {
        super(id, questionText, points, explanation);
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
        if (answer == null) {
            return false;
        }

        if (!answer.trim().equalsIgnoreCase("true")
                && !answer.trim().equalsIgnoreCase("false")) {
            return false;
        }

        return Boolean.parseBoolean(answer.trim()) == this.answer;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.TRUE_FALSE;
    }
}