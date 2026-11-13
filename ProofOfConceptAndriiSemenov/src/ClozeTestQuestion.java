import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClozeTestQuestion extends Question {

    private String[] answers;

    public ClozeTestQuestion(
            int id,
            String questionText,
            int points,
            String explanation,
            String[] answers
    ) {
        super(id, questionText, points, explanation);
        this.answers = answers;
    }

    public String[] getAnswers() {
        return answers;
    }

    public void setAnswers(String[] answers) {
        this.answers = answers;
    }

    public int getGapCount() {
        Matcher matcher = Pattern
                .compile("\\{\\d+}")
                .matcher(getQuestionText());

        int count = 0;

        while (matcher.find()) {
            count++;
        }

        return count;
    }

    @Override
    public boolean checkAnswer(String answer) {

        if (answer == null) {
            return false;
        }

        String[] submittedAnswers = answer.split(";", -1);

        if (submittedAnswers.length != answers.length) {
            return false;
        }

        for (int i = 0; i < answers.length; i++) {
            if (!submittedAnswers[i].trim()
                    .equalsIgnoreCase(answers[i].trim())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.CLOZE_TEST;
    }
}