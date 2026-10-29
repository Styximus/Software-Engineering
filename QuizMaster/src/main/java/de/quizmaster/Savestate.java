package de.quizmaster;

public class Savestate {
    
    private Map map;
    private int[] unlocks;
    private int stars;

    public Savestate(Map map, int[] unlocks, int stars) {
        this.map = map;
        this.unlocks = unlocks;
        this.stars = stars;
    }
    
    public void delete() {
        return;
    }

    public void save() {

    }
}
