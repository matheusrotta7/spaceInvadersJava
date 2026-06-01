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

    Logger logger = Logger.getLogger(SceneRunner.class.toString());

    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final int SHIP_SPRITE_SIZE = 96;
    public static final String ENEMY_SHIP_4 = "enemy_ship_4";
    public static final String BULLET = "bullet";
    private boolean movingLeft  = false;
    private boolean movingRight = false;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean shootingBullets = false;

    private int playerX = 800;
    private int playerY = 800;
    private final int MOVE_SPEED = 5;
    private final int FPS = 120;
    private final int BULLET_COOLDOWN = 150;

    private BufferedImage shipImage;      // loaded once at startup
    private BufferedImage backBuffer;     // off-screen buffer for double buffering

    private ArrayList<GameObject> activeGameObjects;

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
            int scale = 1;
            g2.drawImage(shipImage, playerX, playerY,
                    SHIP_SPRITE_SIZE * scale, SHIP_SPRITE_SIZE * scale, null);
        }

        for (GameObject gameObject : activeGameObjects) {

            if (gameObject.getTag().equals(ENEMY_SHIP_4)) { //todo fix 180 degrees rotation
//                g2.setTransform(new AffineTransform(-1.0, 0.0, 0.0, 1.0, 0.0, 0.0));
                g2.drawImage(gameObject.getSprite(), gameObject.getX(), gameObject.getY(), gameObject.getWidth(), gameObject.getHeight(), null);
//                g2.setTransform(new AffineTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0));
            } else {
                g2.drawImage(gameObject.getSprite(), gameObject.getX(), gameObject.getY(), gameObject.getWidth(), gameObject.getHeight(), null);
            }

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
        while (true) {
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

            long currentTimeMillis = System.currentTimeMillis();
            if (shootingBullets) {
                if (Math.abs(saveLastTimeMillis - currentTimeMillis) > BULLET_COOLDOWN  || saveLastTimeMillis == 0L) {
                    spawnBullet(playerX, playerY);
                    saveLastTimeMillis = System.currentTimeMillis();
                }
            }

            updateBulletPositionsAndRemoveOffSceneBullets();
            detectCollisionsBetweenBulletsAndEnemyShips();
//            logger.info("Number of active gameobjects: " + activeGameObjects.size());

            repaint();
        }
    }

    private void updateBulletPositionsAndRemoveOffSceneBullets() {
        ArrayList<GameObject> bulletsToBeRemoved = new ArrayList<>();
        for (GameObject gameObject : activeGameObjects) {
            if (gameObject.getTag().equals(BULLET)) {
                gameObject.setY(gameObject.getY() + gameObject.getSpeed());
                if (gameObject.getY() < 0) {
                    bulletsToBeRemoved.add(gameObject);
                }
                if (gameObject.getAccelerationDamperCounter() == gameObject.getAccelerationDamper()) {
                    gameObject.setSpeed(gameObject.getSpeed() + gameObject.getAcceleration());
                    gameObject.setAccelerationDamperCounter(0);
                } else {
                    gameObject.setAccelerationDamperCounter(gameObject.getAccelerationDamperCounter()+1);
                }
            }
        }
        activeGameObjects.removeAll(bulletsToBeRemoved);
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
        return a.getX()              < b.getX() + b.getWidth()  &&
                a.getX() + a.getWidth()  > b.getX()              &&
                a.getY()              < b.getY() + b.getHeight() &&
                a.getY() + a.getHeight() > b.getY();
    }

    private void spawnBullet(int playerX, int playerY) {
        BufferedImage buf = null;
        try {
            buf = ImageIO.read(new File("src/main/java/org/example/LaserSprites/01.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.activeGameObjects.add(new GameObject(playerX+33, playerY-8, 30, 30, buf, BULLET, -3, -3, 10));
    }

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

        BufferedImage enemyShip4Sprite = null;
        try {
            enemyShip4Sprite = ImageIO.read(new File("src/main/java/org/example/SpaceShips/Ship_4.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.activeGameObjects.add(new GameObject(100, 100, SHIP_SPRITE_SIZE, SHIP_SPRITE_SIZE, enemyShip4Sprite, ENEMY_SHIP_4, 0, 0, 0));
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
}