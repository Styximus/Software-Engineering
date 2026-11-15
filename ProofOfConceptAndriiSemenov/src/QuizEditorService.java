
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logic of the quiz editor, independent of the user interface.
 */
public class QuizEditorService {

    private final List<Question> questions = new ArrayList<>();
    private int nextId = 1;

    public void addTextQuestion(
            String text,
            String answer,
            int points
    ) {
        checkCommon(text, points);
        requireNotBlank(answer, "Die Antwort darf nicht leer sein.");

        questions.add(new TextQuestion(
                nextId++,
                text.trim(),
                points,
                "",
                answer.trim()
        ));
    }

    public void addTrueFalseQuestion(
            String text,
            boolean answer,
            int points
    ) {
        checkCommon(text, points);

        questions.add(new TrueFalseQuestion(
                nextId++,
                text.trim(),
                points,
                "",
                answer
        ));
    }

    /**
     * @param optionsText one answer option per line
     * @param correctText correct option numbers, e.g. "1,3"
     *                    The input is 1-based; internally indices are 0-based.
     */
    public void addMultipleChoiceQuestion(
            String text,
            String optionsText,
            String correctText,
            int points
    ) {
        checkCommon(text, points);
        requireNotBlank(
                optionsText,
                "Bitte Antwortmöglichkeiten eingeben (eine pro Zeile)."
        );
        requireNotBlank(
                correctText,
                "Bitte die richtigen Antworten angeben, z. B. 1,3."
        );

        List<String> options = Arrays.stream(optionsText.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (options.size() < 2) {
            throw new IllegalArgumentException(
                    "Mindestens zwei Antwortmöglichkeiten nötig."
            );
        }

        Set<Integer> correctIndices = new LinkedHashSet<>();

        try {
            for (String value : correctText.split(",")) {
                int number = Integer.parseInt(value.trim());

                if (number < 1 || number > options.size()) {
                    throw new IllegalArgumentException(
                            "Richtige Antwort " + number
                                    + " existiert nicht (1 bis "
                                    + options.size() + ")."
                    );
                }

                correctIndices.add(number - 1);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Richtige Antworten als Zahlen angeben, z. B. 1,3."
            );
        }

        if (correctIndices.isEmpty()) {
            throw new IllegalArgumentException(
                    "Mindestens eine richtige Antwort auswählen."
            );
        }

        questions.add(new MultipleChoiceQuestion(
                nextId++,
                text.trim(),
                points,
                "",
                options,
                correctIndices
        ));
    }

    /**
     * @param text        text containing gaps such as {1}, {2}, ...
     * @param answersText answers separated by semicolons
     */
    public void addClozeQuestion(
            String text,
            String answersText,
            int points
    ) {
        checkCommon(text, points);
        requireNotBlank(
                answersText,
                "Bitte die Antworten eingeben (getrennt durch ;)."
        );

        String[] answers = Arrays.stream(answersText.split(";", -1))
                .map(String::trim)
                .toArray(String[]::new);

        for (String answer : answers) {
            requireNotBlank(
                    answer,
                    "Keine der Antworten darf leer sein."
            );
        }

        Matcher matcher = Pattern.compile("\\{\\d+}")
                .matcher(text);

        int gapCount = 0;
        while (matcher.find()) {
            gapCount++;
        }

        if (gapCount != answers.length) {
            throw new IllegalArgumentException(
                    "Der Text hat " + gapCount
                            + " Lücke(n), es wurden aber "
                            + answers.length + " Antwort(en) angegeben."
            );
        }

        questions.add(new ClozeTestQuestion(
                nextId++,
                text.trim(),
                points,
                "",
                answers
        ));
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

    /**
     * Returns a validated, immutable list of questions
     * for the quiz session.
     */
    public List<Question> buildQuestions() {
        if (questions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Das Quiz braucht mindestens eine Frage."
            );
        }

        return List.copyOf(questions);
    }

    private void checkCommon(String text, int points) {
        requireNotBlank(
                text,
                "Der Fragetext darf nicht leer sein."
        );

        if (points <= 0) {
            throw new IllegalArgumentException(
                    "Die Punktzahl muss größer als 0 sein."
            );
        }
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
