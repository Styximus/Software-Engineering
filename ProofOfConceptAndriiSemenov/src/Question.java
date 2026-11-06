import java.util.List;

/** Immutable demo content; exactly one answer is correct. */
public record Question(String text, List<String> answers, int correctIndex, String explanation) {
    public Question {
        answers = List.copyOf(answers);
        if (text == null || text.isBlank() || answers.size() < 2
                || answers.stream().anyMatch(a -> a == null || a.isBlank())
                || correctIndex < 0 || correctIndex >= answers.size()
                || explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("Ungültige Frage.");
        }
    }
}
