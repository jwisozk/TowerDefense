package com.game.towerdefense;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Класс, описывающий объект оружия.
 **/
public class Weapon {
    private String name;
    private Body body;
    private float x;
    private float y;
    private boolean found;
    private Vector2 sub;

    public void setFound(boolean found) {
        this.found = found;
    }

    public void setSub(Vector2 sub) {
        this.sub = sub;
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
    }

    public boolean isFound() {
        return found;
    }

    public Vector2 getSub() {
        return sub;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    private Enemy enemy;

    public void setName(String name) {
        this.name = name;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public Body getBody() {
        return body;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
