import java.util.ArrayList;
import java.util.List;

public class QuizEditorService {

    private final List<String> questions = new ArrayList<>();

    public boolean addQuestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        questions.add(question.trim());
        return true;
    }

    public List<String> getQuestions() {
        return questions;
    }
}
