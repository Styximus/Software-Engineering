package de.quizmaster.ui;

import de.quizmaster.model.Npc;

import java.io.File;

public class NpcMenu {

    private Npc npc;

    public NpcMenu(Npc npc) {
        this.npc = npc;
    }

    public Npc getNpc() {
        return npc;
    }

    public void openNpcMenu() {
        return;
    }

    public void closeMenu() {

    }

    public void startQuiz() {

    }

    public void showStars() {
        
    }

    public File exportNpc() {
        return null;
    }
    
    public File exportQuiz() {
        return null;
    }
}
