import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.ArrayList;

public class MultipleChoiceQuestion extends Question {

    private List<String> answers;
    private List<Integer> correctIndices;

    public MultipleChoiceQuestion(
            int id,
            String questionText,
            int points,
            String explanation,
            List<String> answers,
            Set<Integer> correctIndices
    ) {
        super(id, questionText, points, explanation);

        if (answers == null || answers.size() < 2
                || correctIndices == null || correctIndices.isEmpty()
                || correctIndices.stream().anyMatch(
                i -> i == null || i < 0 || i >= answers.size()
        )) {
            throw new IllegalArgumentException("Ungültige Frage.");
        }

        this.answers = new ArrayList<>(answers);
        this.correctIndices = new ArrayList<>(correctIndices);
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        if (answers == null || answers.size() < 2
                || correctIndices.stream().anyMatch(
                i -> i < 0 || i >= answers.size()
        )) {
            throw new IllegalArgumentException("Ungültige Antworten.");
        }

        this.answers = new ArrayList<>(answers);
    }

    public List<Integer> getCorrectIndices() {
        return correctIndices;
    }

    public void setCorrectIndices(Set<Integer> correctIndices) {
        if (correctIndices == null || correctIndices.isEmpty()
                || correctIndices.stream().anyMatch(
                i -> i == null || i < 0 || i >= answers.size()
        )) {
            throw new IllegalArgumentException("Ungültige Lösungen.");
        }

        this.correctIndices = new ArrayList<>(correctIndices);
    }

    @Override
    public boolean checkAnswer(String answer) {

        if (answer == null || answer.isBlank()) {
            return false;
        }

        try {
            List<Integer> selected = Arrays.stream(answer.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .distinct()
                    .sorted()
                    .toList();

            return selected.equals(
                    correctIndices.stream().sorted().toList()
            );

        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public QuestionType getType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}