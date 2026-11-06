import java.util.List;

/** Quiz state and scoring. Deliberately independent of Swing. */
public final class QuizService {
    public enum State { READY, QUESTION, FEEDBACK, FINISHED }

    private final List<Question> questions;
    private State state = State.READY;
    private int index;
    private int score;
    private boolean lastCorrect;

    public QuizService(List<Question> questions) {
        this.questions = List.copyOf(questions);
        if (this.questions.isEmpty()) throw new IllegalArgumentException("Das Quiz ist leer.");
    }

    public static QuizService demo() {
        return new QuizService(List.of(
            new Question("Welcher Baum trägt Eicheln?",
                List.of("Die Buche", "Die Eiche", "Die Birke", "Die Fichte"), 1,
                "Eicheln sind die Früchte der Eiche. Sie sind Nahrung für viele Waldtiere."),
            new Question("Was brauchen Pflanzen für die Fotosynthese?",
                List.of("Mondlicht", "Salz", "Sonnenlicht", "Sand"), 2,
                "Pflanzen nutzen Lichtenergie, um aus Wasser und Kohlenstoffdioxid Zucker zu bilden."),
            new Question("Welches dieser Tiere ist ein Säugetier?",
                List.of("Der Delfin", "Der Frosch", "Die Forelle", "Die Eidechse"), 0,
                "Delfine atmen mit Lungen und säugen ihre Jungen. Sie gehören zu den Säugetieren.")
        ));
    }

    public void start() {
        index = 0;
        score = 0;
        lastCorrect = false;
        state = State.QUESTION;
    }

    public boolean submit(int answer) {
        require(State.QUESTION);
        if (answer < 0 || answer >= currentQuestion().answers().size()) {
            throw new IllegalArgumentException("Bitte wähle eine Antwort aus.");
        }
        lastCorrect = answer == currentQuestion().correctIndex();
        if (lastCorrect) score++;
        state = State.FEEDBACK;
        return lastCorrect;
    }

    public void next() {
        require(State.FEEDBACK);
        if (index + 1 == questions.size()) state = State.FINISHED;
        else {
            index++;
            state = State.QUESTION;
        }
    }

    private void require(State expected) {
        if (state != expected) throw new IllegalStateException("Aktion im aktuellen Zustand nicht möglich.");
    }

    public Question currentQuestion() { return questions.get(index); }
    public State state() { return state; }
    public int index() { return index; }
    public int total() { return questions.size(); }
    public int score() { return score; }
    public boolean lastCorrect() { return lastCorrect; }
    public int answered() { return index + (state == State.FEEDBACK || state == State.FINISHED ? 1 : 0); }
}
