package org.example;

import java.awt.image.BufferedImage;

public class GameObject {

    private int x;
    private int y;

    private int width;
    private int height;

    private BufferedImage sprite;

    private String tag;

    private int speed;
    private int acceleration;
    private int accelerationDamper;
    private int accelerationDamperCounter;

    public GameObject(int x, int y, int width, int height, BufferedImage sprite, String tag, int speed, int acceleration, int accelerationDamper) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
        this.tag = tag;
        this.speed = speed;
        this.acceleration = acceleration;
        this.accelerationDamper = accelerationDamper;
        this.accelerationDamperCounter = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(int acceleration) {
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
}
