package de.quizmaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Logik des Quiz-Editors (ohne Swing). Alle Methoden werfen eine
 * IllegalArgumentException mit lesbarer Meldung, wenn die Eingabe ungültig ist.
 */
public class QuizEditorService {

    private final List<Question> questions = new ArrayList<>();
    private int nextId = 1;

    public void addTextQuestion(String text, String answer, int points) {
        checkCommon(text, points);
        requireNotBlank(answer, "Die Antwort darf nicht leer sein.");
        questions.add(new TextQuestion(nextId++, text.trim(), answer.trim(), points));
    }

    public void addTrueFalseQuestion(String text, boolean answer, int points) {
        checkCommon(text, points);
        questions.add(new TrueFalseQuestion(nextId++, text.trim(), answer, points));
    }

    /**
     * @param optionsText eine Antwortmöglichkeit pro Zeile
     * @param correctText richtige Optionen als Nummern, z. B. "1,3".
     *                    Die Benutzereingabe zählt ab 1, das Modell ab 0.
     */
    public void addMultipleChoiceQuestion(String text, String optionsText, String correctText, int points) {
        checkCommon(text, points);
        requireNotBlank(optionsText, "Bitte Antwortmöglichkeiten eingeben (eine pro Zeile).");
        requireNotBlank(correctText, "Bitte die richtigen Antworten angeben, z. B. 1,3.");

        String[] options = Arrays.stream(optionsText.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (options.length < 2) {
            throw new IllegalArgumentException("Mindestens zwei Antwortmöglichkeiten nötig.");
        }

        int[] correct;
        try {
            correct = Arrays.stream(correctText.split(",", -1))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .sorted()
                    .distinct()
                    .toArray();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Richtige Antworten als Zahlen angeben, z. B. 1,3.");
        }
        for (int c : correct) {
            if (c < 1 || c > options.length) {
                throw new IllegalArgumentException("Richtige Antwort " + c + " existiert nicht (1 bis " + options.length + ").");
            }
        }

        int[] indices = Arrays.stream(correct).map(number -> number - 1).toArray();
        questions.add(new MultipleChoiceQuestion(nextId++, text.trim(), options, indices, points));
    }

    /**
     * @param text        Text mit Lücken {1}, {2}, ...
     * @param answersText Antworten, getrennt durch ";" (gleiche Reihenfolge wie die Lücken)
     */
    public void addClozeQuestion(String text, String answersText, int points) {
        checkCommon(text, points);
        requireNotBlank(answersText, "Bitte die Antworten eingeben (getrennt durch ;).");

        String[] answers = Arrays.stream(answersText.split(";", -1))
                .map(String::trim)
                .toArray(String[]::new);
        for (String a : answers) {
            requireNotBlank(a, "Keine der Antworten darf leer sein.");
        }

        ClozeTestQuestion question = new ClozeTestQuestion(nextId, text.trim(), answers, points);
        if (question.firstGapNumber() < 0) {
            throw new IllegalArgumentException("Nummeriere die Lücken fortlaufend und gib für jede Nummer eine Antwort an.");
        }
        questions.add(question);
        nextId++;
    }

    public void removeQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            throw new IllegalArgumentException("Keine Frage ausgewählt.");
        }
        questions.remove(index);
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    /** Erzeugt das Quiz aus den gesammelten Fragen. */
    public Quiz buildQuiz(int id) {
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Das Quiz braucht mindestens eine Frage.");
        }
        return new Quiz(id, questions.toArray(new Question[0]));
    }

    private void checkCommon(String text, int points) {
        requireNotBlank(text, "Der Fragetext darf nicht leer sein.");
        if (points <= 0) {
            throw new IllegalArgumentException("Die Punktzahl muss größer als 0 sein.");
        }
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
