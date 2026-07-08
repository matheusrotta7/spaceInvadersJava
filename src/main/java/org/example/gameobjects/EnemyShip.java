package org.example.gameobjects;

import org.example.Vector2D;

import java.awt.image.BufferedImage;

import static org.example.SceneRunner.*;

public class EnemyShip extends  GameObject {

    public EnemyShip(Vector2D position, int width, int height, BufferedImage sprite, String tag, Vector2D speed, Vector2D acceleration, int accelerationDamper) {
        super(position, width, height, sprite, tag, speed, acceleration, accelerationDamper);
    }

    @Override
    public void onUpdate() {
        enemyShipMovement();
    }

    private void enemyShipMovement() {
        if (this.getPosition().getX() > SCREEN_WIDTH - PLAYER_SHIP_SPRITE_SIZE) {
            this.setSpeed(new Vector2D(-ENEMY_SHIP_MOVE_SPEED, 0));
        } else if (this.getPosition().getX() < 0) {
            this.setSpeed(new Vector2D(ENEMY_SHIP_MOVE_SPEED, 0));
        }
    }
}
