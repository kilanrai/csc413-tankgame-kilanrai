package tankgame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Tank {
    public final int MAX_HP = 100;
    public final int NUM_LIVES = 3;
    public final int MAX_AMMO = 10;
    public final int R = 2;//amplitude
    public final int ROT_SPEED = 4;

    private int hp = MAX_HP;
    private int lives = NUM_LIVES;
    private int x = 0;
    private int y = 0;
    private int vx = 0;
    private int vy = 0;
    private int angle = 0;
    private int ammo = MAX_AMMO;

    private BufferedImage img;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    Tank(int x, int y, int angle, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.img = img;
    }

    public int getHP() { return hp; }
    public int getLives() { return lives; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getAmmo() { return ammo; }

    void toggleUpPressed() {
        this.upPressed = true;
    }

    void toggleDownPressed() {
        this.downPressed = true;
    }

    void toggleRightPressed() {
        this.rightPressed = true;
    }

    void toggleLeftPressed() {
        this.leftPressed = true;
    }

    void unToggleUpPressed() {
        this.upPressed = false;
    }

    void unToggleDownPressed() {
        this.downPressed = false;
    }

    void unToggleRightPressed() {
        this.rightPressed = false;
    }

    void unToggleLeftPressed() {
        this.leftPressed = false;
    }



    public void update() {
        if (this.leftPressed) {
            this.rotateLeft();
        } else if (this.rightPressed) {
            this.rotateRight();
        } else if (this.upPressed) {
            this.moveForwards();
        } else if (this.downPressed) {
            this.moveBackwards();
        }
    }

    private void rotateLeft() {
        this.angle -= this.ROT_SPEED;
    }

    private void rotateRight() {
        this.angle += this.ROT_SPEED;
    }

    private void moveBackwards() {
        vx = (int) Math.round(R * Math.cos(Math.toRadians(angle)));
        vy = (int) Math.round(R * Math.sin(Math.toRadians(angle)));
        x -= vx;
        y -= vy;
        checkBorder();
    }

    private void moveForwards() {
        vx = (int) Math.round(R * Math.cos(Math.toRadians(angle)));
        vy = (int) Math.round(R * Math.sin(Math.toRadians(angle)));
        x += vx;
        y += vy;
        checkBorder();
    }

    private void checkBorder() {
        if (x < -500) {
            x = -500;
        } else if (x > 500) {
            x = 500;
        }

        if (y < -500) {
            y = -500;
        } else if (y > 500) {
            y = 500;
        }
    }

    @Override
    public String toString() {
        return "x=" + x + ", y=" + y + ", angle=" + angle;
    }


    void drawImage(RenderState rs, Graphics g, int ox, int oy) {
        drawImage(rs, g, ox, oy, 1.0f);
    }

    void drawImage(RenderState rs, Graphics g, int ox, int oy, float scale) {
        int relx = (int)(scale * (x - ox));
        int rely = (int)(scale * (y - oy));
        int renderx = rs.getWidth()/2 + relx;
        int rendery = rs.getHeight()/2 + rely;

        AffineTransform rotation = AffineTransform.getTranslateInstance(renderx, rendery);
        rotation.rotate(Math.toRadians(angle), scale*this.img.getWidth() / 2.0, scale*this.img.getHeight() / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img.getScaledInstance((int)(scale*this.img.getWidth()), (int)(scale*this.img.getHeight()), 0), rotation, null);
    }



}
