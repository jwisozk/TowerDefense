package com.game.towerdefense;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * Класс, описывающий объект врага.
 **/
public class Enemy {
    private float offsetX;
    private float offsetY;
    private float startX;
    private float startY;
    private String name;
    private Body body;

    public Enemy() { }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public String getName() {
        return name;
    }

    public Body getBody() {
        return body;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

}
