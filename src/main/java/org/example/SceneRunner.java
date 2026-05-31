package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SceneRunner extends Canvas implements Runnable {

    private boolean movingLeft  = false;
    private boolean movingRight = false;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean shootingBullets = false;

    private int playerX = 100;
    private int playerY = 100;
    private final int MOVE_SPEED = 5;
    private final int FPS = 120;

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
                    96 * scale, 96 * scale, null);
        }

        for (GameObject gameObject : activeGameObjects) {
            g2.drawImage(gameObject.getSprite(), gameObject.getX(), gameObject.getY(), gameObject.getWidth(), gameObject.getHeight(), null);

        }
        g2.dispose();

        // … then blit the finished frame to the screen in one shot
        g.drawImage(backBuffer, 0, 0, null);
    }

    // ── Game loop ──────────────────────────────────────────────────────────────
    @Override
    public void run() {
        long frameDelay = 1000 / FPS;

        while (true) {
            try {
                Thread.sleep(frameDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (movingLeft)  playerX -= MOVE_SPEED;
            if (movingRight) playerX += MOVE_SPEED;
            if (movingDown) playerY += MOVE_SPEED;
            if (movingUp) playerY -= MOVE_SPEED;
            if (shootingBullets) {
                spawnBullet(playerX, playerY);
            }

            for (GameObject gameObject : activeGameObjects) {
                if (gameObject.getTag().equals("bullet")) {
                    gameObject.setY(gameObject.getY()-1);
                }
            }

            repaint();
        }
    }

    private void spawnBullet(int playerX, int playerY) {
        BufferedImage buf = null;
        try {
            buf = ImageIO.read(new File("src/main/java/org/example/LaserSprites/01.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.activeGameObjects.add(new GameObject(playerX, playerY, 30, 30, buf, "bullet"));
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
    }

    // ── Entry point ────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        Frame frame = new Frame("Sprite Visualization");
        SceneRunner game = new SceneRunner();
        frame.add(game);
        frame.setSize(1920, 1080);
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

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
    }
}