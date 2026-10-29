package de.quizmaster;

public class QuestionUserInterface {

    private int id;
    private Npc npc;
    private Quiz quiz;
    private int reachedPoints;

    public QuestionUserInterface(int id, Npc npc, Quiz quiz, int reachedPoints) {
        this.id = id;
        this.npc = npc;
        this.quiz = quiz;
        this.reachedPoints = reachedPoints;
    }

    public void setNpc(Npc npc) {
        this.npc = npc;
    }

    public int getId() {
        return id;
    }

    public Npc getNpc() {
        return npc;
    }
    
    public void getQuizFromNpc() {
        return;
    }

    public void randomizeQuestions() {
        return;
    }
    
    public void initializeUi() {
        return;
    }

    public void startGame() {
        return;
    }

    public void evaluateAnswers(String answer) {
        return;
    }

    public void endGame() {
        return;
    }

    public void evaluateStars() {
        return;
    }

    public void closeWindows() {
        return;
    }
}
