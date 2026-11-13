
import java.util.List;

/**
 * Manages the state and scoring of a quiz.
 * This class is independent of the user interface.
 */
public final class QuizService {

    public enum State {
        READY,
        QUESTION,
        FEEDBACK,
        FINISHED
    }

    private final List<Question> questions;

    private State state = State.READY;

    private int index = 0;
    private int score = 0;

    private boolean lastCorrect = false;

    /**
     * Creates a new quiz session.
     *
     * @param questions the questions belonging to this quiz
     */
    public QuizService(List<Question> questions) {

        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException(
                    "The quiz must contain at least one question."
            );
        }

        this.questions = List.copyOf(questions);
    }

    /**
     * Starts or restarts the quiz.
     */
    public void start() {

        index = 0;
        score = 0;
        lastCorrect = false;

        state = State.QUESTION;
    }

    /**
     * Submits an answer for the current question.
     *
     * @param answer the answer provided by the user
     * @return true if the answer is correct, otherwise false
     */
    public boolean submit(String answer) {

        require(State.QUESTION);

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter an answer."
            );
        }

        Question question = currentQuestion();

        lastCorrect = question.checkAnswer(answer);

        if (lastCorrect) {
            score += question.getPoints();
        }

        state = State.FEEDBACK;

        return lastCorrect;
    }

    /**
     * Moves to the next question.
     * If there are no more questions, the quiz is finished.
     */
    public void next() {

        require(State.FEEDBACK);

        if (index + 1 >= questions.size()) {
            state = State.FINISHED;
        } else {
            index++;
            state = State.QUESTION;
        }
    }

    /**
     * Checks whether an operation is allowed
     * in the current quiz state.
     */
    private void require(State expected) {

        if (state != expected) {
            throw new IllegalStateException(
                    "This action is not allowed in the current quiz state."
            );
        }
    }

    /**
     * Returns the currently active question.
     */
    public Question currentQuestion() {
        return questions.get(index);
    }

    /**
     * Returns the current quiz state.
     */
    public State state() {
        return state;
    }

    /**
     * Returns the current question index (starting at 0).
     */
    public int index() {
        return index;
    }

    /**
     * Returns the total number of questions.
     */
    public int total() {
        return questions.size();
    }

    /**
     * Returns the current score.
     */
    public int score() {
        return score;
    }

    /**
     * Returns the maximum possible score.
     */
    public int maxScore() {

        return questions.stream()
                .mapToInt(Question::getPoints)
                .sum();
    }

    /**
     * Returns whether the last submitted answer was correct.
     */
    public boolean lastCorrect() {
        return lastCorrect;
    }

    /**
     * Returns the number of questions already answered.
     */
    public int answered() {

        return index + (
                state == State.FEEDBACK || state == State.FINISHED
                        ? 1
                        : 0
        );
    }
}