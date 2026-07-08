package org.example.gameobjects;

import org.example.ImageUtilities;
import org.example.Vector2D;

import java.awt.image.BufferedImage;

import static org.example.SceneRunner.*;

public class EnemyShip extends  GameObject {

    private long timeSinceLastShotEnemy = 0;

    public EnemyShip(Vector2D position, int width, int height, BufferedImage sprite, String tag, Vector2D speed, Vector2D acceleration, int accelerationDamper) {
        super(position, width, height, sprite, tag, speed, acceleration, accelerationDamper);
    }



    @Override
    public void onUpdate(long deltaTimeMillis) {
        enemyShipMovement();

        if (timeSinceLastShotEnemy > ENEMY_BULLET_COOLDOWN) {
            enemyShipFireShots();
            timeSinceLastShotEnemy = 0;
        } else {
            timeSinceLastShotEnemy += deltaTimeMillis;
        }
    }

    private void enemyShipMovement() {
        if (this.getPosition().getX() > SCREEN_WIDTH - PLAYER_SHIP_SPRITE_SIZE) {
            this.setSpeed(new Vector2D(-ENEMY_SHIP_MOVE_SPEED, 0));
        } else if (this.getPosition().getX() < 0) {
            this.setSpeed(new Vector2D(ENEMY_SHIP_MOVE_SPEED, 0));
        }
    }

    private void enemyShipFireShots() {

        gameObjectsToBeCreated.add(new GameObject(Vector2D.sum(this.getPosition(), new Vector2D(+23, +64)), BULLET_WIDTH, BULLET_HEIGHT, enemyBulletImage, ENEMY_BULLET, new Vector2D(0, ENEMY_BULLET_SPEED_VERTICAL), new Vector2D(0, 0), 0));
        gameObjectsToBeCreated.add(new GameObject(Vector2D.sum(this.getPosition(), new Vector2D(+23, +64)), BULLET_WIDTH, BULLET_HEIGHT, ImageUtilities.rotateBy(enemyBulletImage, Math.toDegrees(Math.atan((double) -ENEMY_BULLET_SPEED_HORIZONTAL/ (double) ENEMY_BULLET_SPEED_VERTICAL))), ENEMY_BULLET, new Vector2D(+ENEMY_BULLET_SPEED_HORIZONTAL, ENEMY_BULLET_SPEED_VERTICAL), new Vector2D(0, 0), 0));
        gameObjectsToBeCreated.add(new GameObject(Vector2D.sum(this.getPosition(), new Vector2D(+23, +64)), BULLET_WIDTH, BULLET_HEIGHT, ImageUtilities.rotateBy(enemyBulletImage, Math.toDegrees(Math.atan((double) +ENEMY_BULLET_SPEED_HORIZONTAL/ (double) ENEMY_BULLET_SPEED_VERTICAL))), ENEMY_BULLET, new Vector2D(-ENEMY_BULLET_SPEED_HORIZONTAL, ENEMY_BULLET_SPEED_VERTICAL), new Vector2D(0, 0), 0));
    }
}
