package org.example;

import org.example.gameobjects.EnemyShip;
import org.example.gameobjects.GameObject;
import org.example.gameobjects.PlayerBullet;
import org.example.gameobjects.PlayerShip;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SceneRunner extends Canvas implements Runnable {

    private static final Logger logger = Logger.getLogger(SceneRunner.class.toString());

    public static final int BULLET_WIDTH = 50;
    public static final int BULLET_HEIGHT = 80;
    public static final double ENEMY_SHIP_MOVE_SPEED = 4;

    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final int SCREEN_MARGIN_BEFORE_DELETION = 100;
    public static final int PLAYER_SHIP_SPRITE_SIZE = 96;
    public static final int ENEMY_BULLET_SPEED_VERTICAL = +1;
    public static final int ENEMY_BULLET_SPEED_HORIZONTAL = 1;
    private static final int MOVE_SPEED = 5;
    private static final int FPS = 120;
    private static final int BULLET_COOLDOWN = 150;
    public static final int ENEMY_BULLET_COOLDOWN = 1000;
    private static final int PLAYER_INITIAL_X = 800;
    private static final int PLAYER_INITIAL_Y = 800;

    public static final String PLAYER_SHIP = "player_ship";
    public static final String ENEMY_SHIP_4 = "enemy_ship_4";
    public static final String PLAYER_BULLET = "player_bullet";
    public static final String ENEMY_BULLET = "enemy_bullet";

    private boolean movingLeft  = false;
    private boolean movingRight = false;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean shootingBullets = false;


    public static BufferedImage shipImage;
    public static BufferedImage bulletSpriteImage;
    public static BufferedImage enemyShipImage;
    public static BufferedImage enemyBulletImage;
    public static BufferedImage backBuffer;     // off-screen buffer for double buffering

    public static ArrayList<GameObject> activeGameObjects;
    public static ArrayList<GameObject> gameObjectsToBeCreated = new ArrayList<>();

    // ── Constructor ────────────────────────────────────────────────────────────
    public SceneRunner() {


        activeGameObjects = new ArrayList<>();
        // Load image once here, not on every paint call
        try {
            shipImage = ImageIO.read(
                    new File("src/main/java/org/example/SpaceShips/Ship_1.png"));
        } catch (IOException e) {
            throw new RuntimeException("Could not load ship image", e);
        }

        activeGameObjects.add(new PlayerShip(new Vector2D(PLAYER_INITIAL_X, PLAYER_INITIAL_Y), PLAYER_SHIP_SPRITE_SIZE, PLAYER_SHIP_SPRITE_SIZE, shipImage, PLAYER_SHIP, new Vector2D(0, 0), new Vector2D(0, 0), 0));

        Thread animThread = new Thread(this);
        animThread.setDaemon(true);
        animThread.start();
        setBackground(Color.BLACK);



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

        activeGameObjects.add(new EnemyShip(new Vector2D(100, 100), PLAYER_SHIP_SPRITE_SIZE, PLAYER_SHIP_SPRITE_SIZE, rotatedEnemyShipImage, ENEMY_SHIP_4, new Vector2D(ENEMY_SHIP_MOVE_SPEED, 0), new Vector2D(0, 0), 0));
        activeGameObjects.add(new EnemyShip(new Vector2D(200, 300), PLAYER_SHIP_SPRITE_SIZE, PLAYER_SHIP_SPRITE_SIZE, rotatedEnemyShipImage, ENEMY_SHIP_4, new Vector2D(ENEMY_SHIP_MOVE_SPEED, 0), new Vector2D(0, 0), 0));
        activeGameObjects.add(new EnemyShip(new Vector2D(400, 400), PLAYER_SHIP_SPRITE_SIZE, PLAYER_SHIP_SPRITE_SIZE, rotatedEnemyShipImage, ENEMY_SHIP_4, new Vector2D(ENEMY_SHIP_MOVE_SPEED, 0), new Vector2D(0, 0), 0));
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

            playerShipMovement();

            if (shootingBullets) {
                if (timeSinceLastShotPlayer > BULLET_COOLDOWN) {
                    GameObject playerShip = retrieveGameObjectWithTag(PLAYER_SHIP);
                    if (playerShip != null) {
                        spawnBullet(playerShip.getPosition().getX(), playerShip.getPosition().getY());
                    }
                    timeSinceLastShotPlayer = 0;
                } else {
                    timeSinceLastShotPlayer += deltaTimeMillis;
                }
            } else {
                timeSinceLastShotPlayer += deltaTimeMillis;
            }

            for (GameObject go : activeGameObjects) {
                go.onUpdate(deltaTimeMillis);
            }

            activeGameObjects.addAll(gameObjectsToBeCreated); //add here after the update loop otherwise there is co-modification



            gameObjectCinematicsAndOffsceneRemoval();
            detectCollisionsBetweenGameObjects();
            deleteGameObjectsMarkedForDeletion();
//            logger.info("Number of active gameobjects: " + activeGameObjects.size());

            repaint();
        }
    }

    private void playerShipMovement() {

        GameObject playerShip = retrieveGameObjectWithTag(PLAYER_SHIP);

        if (playerShip != null) {
            if (movingLeft && playerShip.getPosition().getX() > 0) {
                playerShip.setPosition(new Vector2D(playerShip.getPosition().getX() - MOVE_SPEED, playerShip.getPosition().getY()));
            }
            if (movingRight && playerShip.getPosition().getX() < SCREEN_WIDTH - PLAYER_SHIP_SPRITE_SIZE - 20) {
                playerShip.setPosition(new Vector2D(playerShip.getPosition().getX() + MOVE_SPEED, playerShip.getPosition().getY()));
            }
            if (movingDown && playerShip.getPosition().getY() < SCREEN_HEIGHT - PLAYER_SHIP_SPRITE_SIZE - PLAYER_SHIP_SPRITE_SIZE) {
                playerShip.setPosition(new Vector2D(playerShip.getPosition().getX(), playerShip.getPosition().getY() + MOVE_SPEED));
            }
            if (movingUp && playerShip.getPosition().getY() > 0) {
                playerShip.setPosition(new Vector2D(playerShip.getPosition().getX(), playerShip.getPosition().getY() - MOVE_SPEED));
            }

        }

    }

    private void deleteGameObjectsMarkedForDeletion() {
        ArrayList<GameObject> result = new ArrayList<>();
        for (GameObject gameObject : activeGameObjects) {
            if (!gameObject.isToBeDeleted()) {
                result.add(gameObject);
            }
        }
        this.activeGameObjects = result;
    }



    public static GameObject retrieveGameObjectWithTag(String tag) {
        for (GameObject gameObject : activeGameObjects) {
            if (gameObject.getTag().equals(tag)) {
                return gameObject;
            }
        }

        return null;
    }

    private void gameObjectCinematicsAndOffsceneRemoval() {
        for (GameObject gameObject : activeGameObjects) {
            gameObject.setPosition(Vector2D.sum(gameObject.getPosition(), gameObject.getSpeed()));
            if (gameObjectIsOffScene(gameObject)) {
                gameObject.markForDeletion();
            }
            if (gameObject.getAccelerationDamperCounter() == gameObject.getAccelerationDamper()) {
                gameObject.setSpeed(Vector2D.sum(gameObject.getSpeed(),gameObject.getAcceleration()));
                gameObject.setAccelerationDamperCounter(0);
            } else {
                gameObject.setAccelerationDamperCounter(gameObject.getAccelerationDamperCounter() + 1);
            }
        }
    }

    private static boolean gameObjectIsOffScene(GameObject gameObject) {
        return gameObject.getPosition().getY() < -SCREEN_MARGIN_BEFORE_DELETION || gameObject.getPosition().getY() > SCREEN_HEIGHT+SCREEN_MARGIN_BEFORE_DELETION || gameObject.getPosition().getX() < -SCREEN_MARGIN_BEFORE_DELETION || gameObject.getPosition().getX() > SCREEN_WIDTH+SCREEN_MARGIN_BEFORE_DELETION;
    }



    private void detectCollisionsBetweenGameObjects() {
        for (int i = 0; i < activeGameObjects.size(); i++) {
            GameObject curGameObject = activeGameObjects.get(i);
            for (int j = i + 1; j < activeGameObjects.size(); j++) {
                GameObject otherGameObject = activeGameObjects.get(j);
                if (gameObjectsOverlap(curGameObject, otherGameObject)) {
                    curGameObject.onCollision(otherGameObject);
                    otherGameObject.onCollision(curGameObject);
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

    private void spawnBullet(double playerX, double playerY) {
        this.activeGameObjects.add(new PlayerBullet(new Vector2D(playerX+33, playerY-8), BULLET_WIDTH, BULLET_HEIGHT, bulletSpriteImage, PLAYER_BULLET, new Vector2D(0, -3), new Vector2D(0,-3), 10));
    }

}