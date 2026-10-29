package de.quizmaster;

import java.awt.Image;

public class Player {
    
    private Position position;
    private Image skin;

    public Player(Position position, Image skin) {
        this.position = position;
        this.skin = skin;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Image getSkin() {
        return skin;
    }

    public void setSkin(Image skin) {
        this.skin = skin;
    }
}
