public abstract class Question {

    private final int id;
    private final String questionText;
    private final int points;
    private final String explanation;

    public Question(int id, String questionText, int points) {
        this(id, questionText, points, "");
    }

    public Question(
            int id,
            String questionText,
            int points,
            String explanation
    ) {
        this.id = id;
        this.questionText = questionText;
        this.points = points;
        this.explanation = explanation;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return questionText;
    }

    // Retained for compatibility with existing code
    public String getQuestionText() {
        return questionText;
    }

    public int getPoints() {
        return points;
    }

    public String getExplanation() {
        return explanation;
    }

    public abstract boolean checkAnswer(String answer);

    public abstract QuestionType getType();
}
