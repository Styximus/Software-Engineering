package de.quizmaster;

import java.util.*;

/** One quiz attempt. Evaluation stays in Question; star thresholds stay in Quiz. */
public final class QuizSession {
    public enum State { READY, QUESTION, FEEDBACK, FINISHED }
    private final Quiz quiz;
    private Question[] questions;
    private State state = State.READY;
    private int index, points;
    private boolean correct;
    private boolean randomized;
    private String submittedAnswer;

    public QuizSession(Quiz quiz) {
        this.quiz = Objects.requireNonNull(quiz, "Quiz fehlt.");
        this.questions = quiz.getQuestions();
        for (Question question : questions) {
            if (question == null || question.getQuestionText() == null || question.getQuestionText().isBlank()
                    || question.getPoints() < 0) throw new IllegalArgumentException("Ungültige Quizfrage.");
        }
    }

    public void start() {
        index = 0; points = 0; correct = false; submittedAnswer = null;
        questions = quiz.getQuestions();
        if (randomized) Collections.shuffle(Arrays.asList(questions));
        quiz.calculateStars(0);
        state = questions.length == 0 ? State.FINISHED : State.QUESTION;
    }

    public void submit(String answer) {
        if (state != State.QUESTION) throw new IllegalStateException("Diese Frage wurde bereits beantwortet.");
        if (answer == null || answer.isBlank()) throw new IllegalArgumentException("Bitte beantworte zuerst die Frage.");
        correct = current().checkAnswer(answer);
        submittedAnswer = answer;
        if (correct) points += current().getPoints();
        state = State.FEEDBACK;
    }

    public void next() {
        if (state != State.FEEDBACK) throw new IllegalStateException("Prüfe zuerst deine Antwort.");
        if (index + 1 == questions.length) finish();
        else { index++; state = State.QUESTION; }
    }

    public void finish() { quiz.calculateStars(points); state = State.FINISHED; }
    public void setRandomized(boolean randomized) { this.randomized = randomized; }
    public State state() { return state; }
    public Question current() {
        if (questions.length == 0) throw new IllegalStateException("Dieses Quiz enthält keine Fragen.");
        return questions[index];
    }
    public int index() { return index; }
    public int total() { return questions.length; }
    public int points() { return points; }
    public int maxPoints() { return quiz.getMaxPoints(); }
    public int stars() { return quiz.getStarsAchieved(); }
    public boolean correct() { return correct; }
    public String submittedAnswer() { return submittedAnswer; }
    public int answered() {
        if (state == State.READY) return 0;
        if (state == State.FINISHED) return questions.length;
        return index + (state == State.FEEDBACK ? 1 : 0);
    }
}
