package de.quizmaster;

import java.awt.Image;

public class Npc {
    
    private int id;
    private Position position;
    private String role;
    private String greeting;
    private String name;
    private Image skin;
    private Quiz quiz;

    public Npc(int id, Position position, String role, String greeting, String name, Image skin, Quiz quiz) {
        this.id = id;
        this.position = position;
        this.role = role;
        this.greeting = greeting;
        this.name = name;
        this.skin = skin;
        this.quiz = quiz;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Image getSkin() {
        return skin;
    }

    public void setSkin(Image skin) {
        this.skin = skin;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
