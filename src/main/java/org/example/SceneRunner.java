package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SceneRunner extends Canvas implements Runnable {

    private static final Logger logger = Logger.getLogger(SceneRunner.class.toString());

    public static final int BULLET_WIDTH = 50;
    public static final int BULLET_HEIGHT = 80;
    private static final double ENEMY_SHIP_MOVE_SPEED = 4;

    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final int SHIP_SPRITE_SIZE = 96;
    public static final String ENEMY_BULLET = "ENEMY_BULLET";
    public static final int ENEMY_BULLET_SPEED_VERTICAL = +3;
    public static final int ENEMY_BULLET_SPEED_HORIZONTAL = 2;
    private final int MOVE_SPEED = 5;
    private final int FPS = 120;
    private final int BULLET_COOLDOWN = 150;
    private final int ENEMY_BULLET_COOLDOWN = 500;
    public static final String ENEMY_SHIP_4 = "enemy_ship_4";
    public static final String BULLET = "bullet";

    private boolean movingLeft  = false;
    private boolean movingRight = false;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean shootingBullets = false;

    private int playerX = 800;
    private int playerY = 800;

    private BufferedImage shipImage;
    private BufferedImage bulletSpriteImage;
    private BufferedImage enemyShipImage;
    private BufferedImage enemyBulletImage;
    private BufferedImage backBuffer;     // off-screen buffer for double buffering

    private ArrayList<GameObject> activeGameObjects;

    // ── Constructor ────────────────────────────────────────────────────────────
    public SceneRunner() {
        // Load image once here, not on every paint call
        try {
            shipImage = ImageIO.read(
                    new File("src/main/java/org/example/SpaceShips/Ship_1.png"));
        } catch (IOException e) {
            throw new RuntimeException("Could not load ship image", e);
        }

        Thread animThread = new Thread(this);
        animThread.setDaemon(true);
        animThread.start();
        setBackground(Color.BLACK);
        this.activeGameObjects = new ArrayList<>();



        BufferedImage bulletSprite = null;
        try {
            bulletSprite = ImageIO.read(new File("src/main/java/org/example/LaserSprites/01.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bulletSpriteImage = ImageUtilities.rotateBy(bulletSprite, ImageUtilities.Direction.WEST);


        try {
            enemyShipImage = ImageIO.read(new File("src/main/java/org/example/SpaceShips/Ship_4.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedImage rotatedEnemyShipImage = ImageUtilities.rotateBy(enemyShipImage, ImageUtilities.Direction.SOUTH);

        try {
            enemyBulletImage = ImageIO.read(new File("src/main/java/org/example/LaserSprites/12.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        enemyBulletImage = ImageUtilities.rotateBy(enemyBulletImage, ImageUtilities.Direction.EAST);

        this.activeGameObjects.add(new GameObject(new Vector2D(100, 100), SHIP_SPRITE_SIZE, SHIP_SPRITE_SIZE, rotatedEnemyShipImage, ENEMY_SHIP_4, new Vector2D(ENEMY_SHIP_MOVE_SPEED, 0), new Vector2D(0, 0), 0));
    }

    // ── Entry point ────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        Frame frame = new Frame("Sprite Visualization");
        SceneRunner game = new SceneRunner();
        frame.add(game);
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A)     game.movingLeft  = true;
                if (e.getKeyCode() == KeyEvent.VK_D)     game.movingRight = true;
                if (e.getKeyCode() == KeyEvent.VK_S)     game.movingDown = true;
                if (e.getKeyCode() == KeyEvent.VK_W)     game.movingUp = true;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) game.shootingBullets = true;
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A)     game.movingLeft  = false;
                if (e.getKeyCode() == KeyEvent.VK_D)     game.movingRight = false;
                if (e.getKeyCode() == KeyEvent.VK_S)     game.movingDown = false;
                if (e.getKeyCode() == KeyEvent.VK_W)     game.movingUp = false;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) game.shootingBullets = false;
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    // ── Double-buffered paint ──────────────────────────────────────────────────
    @Override
    public void update(Graphics g) {
        // Intercept AWT's default clear+paint so we drive everything ourselves
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        // Lazily create / recreate the back-buffer if the canvas was resized
        if (backBuffer == null
                || backBuffer.getWidth()  != getWidth()
                || backBuffer.getHeight() != getHeight()) {
            backBuffer = new BufferedImage(getWidth(), getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        }

        // Draw everything onto the off-screen image …
        Graphics2D g2 = backBuffer.createGraphics();
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());   // clear

        if (shipImage != null) {
            g2.drawImage(shipImage, playerX, playerY,
                    SHIP_SPRITE_SIZE, SHIP_SPRITE_SIZE, null);
        }

        for (GameObject gameObject : activeGameObjects) {
            g2.drawImage(gameObject.getSprite(), (int) Math.floor(gameObject.getPosition().getX()), (int) Math.floor(gameObject.getPosition().getY()), gameObject.getWidth(), gameObject.getHeight(), null);
        }
        g2.dispose();

        // … then blit the finished frame to the screen in one shot
        g.drawImage(backBuffer, 0, 0, null);
    }

    // ── Game loop ──────────────────────────────────────────────────────────────
    @Override
    public void run() {
        long frameDelay = 1000 / FPS;

        long saveLastTimeMillis = 0;
        long curTimeMillis = 0;
        long deltaTimeMillis = 0;
        long timeSinceLastShotEnemy = 0;
        long timeSinceLastShotPlayer = 0;
        while (true) {
            curTimeMillis = System.currentTimeMillis();
            if (saveLastTimeMillis == 0) {
                saveLastTimeMillis = curTimeMillis; //initiate saveLastTime in first frame
            }
            deltaTimeMillis = curTimeMillis - saveLastTimeMillis;
            saveLastTimeMillis = curTimeMillis;

            try {
                Thread.sleep(frameDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (movingLeft && playerX > 0) {
                playerX -= MOVE_SPEED;
            }
            if (movingRight && playerX < SCREEN_WIDTH - SHIP_SPRITE_SIZE - 20) {
                playerX += MOVE_SPEED;
            }
            if (movingDown && playerY < SCREEN_HEIGHT - SHIP_SPRITE_SIZE - SHIP_SPRITE_SIZE) {
                playerY += MOVE_SPEED;
            }
            if (movingUp && playerY > 0) {
                playerY -= MOVE_SPEED;
            }

            if (shootingBullets) {
                if (timeSinceLastShotPlayer > BULLET_COOLDOWN) {
                    spawnBullet(playerX, playerY);
                    timeSinceLastShotPlayer = 0;
                } else {
                    timeSinceLastShotPlayer += deltaTimeMillis;
                }
            } else {
                timeSinceLastShotPlayer += deltaTimeMillis;
            }

            enemyShipMovement();

            if (timeSinceLastShotEnemy > ENEMY_BULLET_COOLDOWN) {
                enemyShipFireShots();
                timeSinceLastShotEnemy = 0;
            } else {
                timeSinceLastShotEnemy += deltaTimeMillis;
            }

            gameObjectCinematicsAndOffsceneRemoval();
            detectCollisionsBetweenBulletsAndEnemyShips();
//            logger.info("Number of active gameobjects: " + activeGameObjects.size());

            repaint();
        }
    }

    private void enemyShipFireShots() {
        GameObject enemyShip = retrieveGameObjectWithTag(ENEMY_SHIP_4);
        if (enemyShip == null) {
            return;
        }
        this.activeGameObjects.add(new GameObject(Vector2D.sum(enemyShip.getPosition(), new Vector2D(+23, +64)), BULLET_WIDTH, BULLET_HEIGHT, enemyBulletImage, ENEMY_BULLET, new Vector2D(0, ENEMY_BULLET_SPEED_VERTICAL), new Vector2D(0, 0), 0));
        this.activeGameObjects.add(new GameObject(Vector2D.sum(enemyShip.getPosition(), new Vector2D(+23, +64)), BULLET_WIDTH, BULLET_HEIGHT, ImageUtilities.rotateBy(enemyBulletImage, Math.toDegrees(Math.atan((double) -ENEMY_BULLET_SPEED_HORIZONTAL/ (double) ENEMY_BULLET_SPEED_VERTICAL))), ENEMY_BULLET, new Vector2D(+ENEMY_BULLET_SPEED_HORIZONTAL, ENEMY_BULLET_SPEED_VERTICAL), new Vector2D(0, 0), 0));
        this.activeGameObjects.add(new GameObject(Vector2D.sum(enemyShip.getPosition(), new Vector2D(+23, +64)), BULLET_WIDTH, BULLET_HEIGHT, ImageUtilities.rotateBy(enemyBulletImage, Math.toDegrees(Math.atan((double) +ENEMY_BULLET_SPEED_HORIZONTAL/ (double) ENEMY_BULLET_SPEED_VERTICAL))), ENEMY_BULLET, new Vector2D(-ENEMY_BULLET_SPEED_HORIZONTAL, ENEMY_BULLET_SPEED_VERTICAL), new Vector2D(0, 0), 0));
    }

    private GameObject retrieveGameObjectWithTag(String tag) {
        for (GameObject gameObject : activeGameObjects) {
            if (gameObject.getTag().equals(tag)) {
                return gameObject;
            }
        }

        return null;
    }

    private void gameObjectCinematicsAndOffsceneRemoval() {
        ArrayList<GameObject> bulletsToBeRemoved = new ArrayList<>();
        for (GameObject gameObject : activeGameObjects) {
            if (true) {
                gameObject.setPosition(Vector2D.sum(gameObject.getPosition(), gameObject.getSpeed()));
                if (gameObject.getPosition().getY() < 0) {
                    bulletsToBeRemoved.add(gameObject);
                }
                if (gameObject.getAccelerationDamperCounter() == gameObject.getAccelerationDamper()) {
                    gameObject.setSpeed(Vector2D.sum(gameObject.getSpeed(),gameObject.getAcceleration()));
                    gameObject.setAccelerationDamperCounter(0);
                } else {
                    gameObject.setAccelerationDamperCounter(gameObject.getAccelerationDamperCounter() + 1);
                }
            }
        }
        activeGameObjects.removeAll(bulletsToBeRemoved);
    }

    private void enemyShipMovement() {
        for (GameObject gameObject : activeGameObjects) {
            if (gameObject.getTag().equals(ENEMY_SHIP_4)) {
                if (gameObject.getPosition().getX() > SCREEN_WIDTH - SHIP_SPRITE_SIZE) {
                    gameObject.setSpeed(new Vector2D(-ENEMY_SHIP_MOVE_SPEED, 0));
                } else if (gameObject.getPosition().getX() < 0) {
                    gameObject.setSpeed(new Vector2D(ENEMY_SHIP_MOVE_SPEED, 0));
                }
            }
        }
    }

    private void detectCollisionsBetweenBulletsAndEnemyShips() {
        for (int i = 0; i < activeGameObjects.size(); i++) {
            GameObject curGameObject = activeGameObjects.get(i);
            if (curGameObject.getTag().equals(BULLET) || curGameObject.getTag().equals(ENEMY_SHIP_4)) { //"collidable" objects for now
                for (int j = i + 1; j < activeGameObjects.size(); j++) {
                    GameObject otherGameObject = activeGameObjects.get(j);
                    if (gameObjectsOverlap(curGameObject, otherGameObject)) {
                        if ((curGameObject.getTag().equals(BULLET) && otherGameObject.getTag().equals(ENEMY_SHIP_4))
                                ||
                            (curGameObject.getTag().equals(ENEMY_SHIP_4) && otherGameObject.getTag().equals(BULLET))
                        ) {
                            activeGameObjects.remove(curGameObject);
                            activeGameObjects.remove(otherGameObject);
                        }
                    }
                }
            }
        }
    }

    boolean gameObjectsOverlap(GameObject a, GameObject b) {
        return a.getPosition().getX()              < b.getPosition().getX() + b.getWidth()  &&
                a.getPosition().getX() + a.getWidth()  > b.getPosition().getX()              &&
                a.getPosition().getY()              < b.getPosition().getY() + b.getHeight() &&
                a.getPosition().getY() + a.getHeight() > b.getPosition().getY();
    }

    private void spawnBullet(int playerX, int playerY) {
        this.activeGameObjects.add(new GameObject(new Vector2D(playerX+33, playerY-8), BULLET_WIDTH, BULLET_HEIGHT, bulletSpriteImage, BULLET, new Vector2D(0, -3), new Vector2D(0,-3), 10));
    }

}