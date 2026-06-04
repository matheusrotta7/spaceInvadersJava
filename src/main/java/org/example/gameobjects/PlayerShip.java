package org.example.gameobjects;

import org.example.SceneRunner;
import org.example.Vector2D;

import java.awt.image.BufferedImage;

public class PlayerShip extends GameObject {


    public PlayerShip(Vector2D position, int width, int height, BufferedImage sprite, String tag, Vector2D speed, Vector2D acceleration, int accelerationDamper) {
        super(position, width, height, sprite, tag, speed, acceleration, accelerationDamper);
    }

    @Override
    public void onCollision(GameObject other) {
        if (other.getTag().equals(SceneRunner.ENEMY_BULLET)) {
            this.markForDeletion();
            other.markForDeletion();
        }
    }
}
