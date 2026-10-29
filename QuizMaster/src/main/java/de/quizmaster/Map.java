package de.quizmaster;

import java.io.File;
import java.util.HashMap;

public class Map {

    private HashMap<Npc, Position> npcs;
    private Player player;

    public Map(HashMap<Npc, Position> npcs, Player player) {
        this.npcs = npcs;
        this.player = player;
    }

    public void movePlayer() {
        return;
    }

    public void placeNpc(Npc npc, Position position) {
        
    }

    public boolean checkCollision(Position position) {
        return npcs.containsValue(position);
    }

    public void populateSavestate() {
        return;
    }
    
    public void interact() {
        return;
    }

    public Player getPlayer() {
        return player;
    }

    public HashMap<Npc, Position> getNpcs() {
        return npcs;
    }
    
    public void saveGame() {
        return;
    }

    public File exportNpcs() {
        return null;
    }
}
