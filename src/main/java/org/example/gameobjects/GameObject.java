package org.example.gameobjects;

import org.example.Vector2D;

import java.awt.image.BufferedImage;

public class GameObject {

    private Vector2D position;

    private int width;
    private int height;

    private BufferedImage sprite;

    private String tag;

    private Vector2D speed;
    private Vector2D acceleration;
    private int accelerationDamper;
    private int accelerationDamperCounter;

    private boolean toBeDeleted;

    public GameObject(Vector2D position, int width, int height, BufferedImage sprite, String tag, Vector2D speed, Vector2D acceleration, int accelerationDamper) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
        this.tag = tag;
        this.speed = speed;
        this.acceleration = acceleration;
        this.accelerationDamper = accelerationDamper;
        this.accelerationDamperCounter = 0;
        this.toBeDeleted = false;
    }

    public void onCollision(GameObject other) {

    }

    public void markForDeletion() {
        this.toBeDeleted = true;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public BufferedImage getSprite() {
        return sprite;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Vector2D getSpeed() {
        return speed;
    }

    public void setSpeed(Vector2D speed) {
        this.speed = speed;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    public int getAccelerationDamper() {
        return accelerationDamper;
    }

    public void setAccelerationDamper(int accelerationDamper) {
        this.accelerationDamper = accelerationDamper;
    }

    public int getAccelerationDamperCounter() {
        return accelerationDamperCounter;
    }

    public void setAccelerationDamperCounter(int accelerationDamperCounter) {
        this.accelerationDamperCounter = accelerationDamperCounter;
    }

    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }
}
