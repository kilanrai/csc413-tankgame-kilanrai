package tankgame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javafx.scene.media.*;
import javafx.embed.swing.JFXPanel;

// the player's tank, holds their health, position, and other information
// deals with shooting and moving
public class Tank {
    static{
        JFXPanel fxPanel = new JFXPanel();
    }

    public final int MAX_HP = 100;
    public final int NUM_LIVES = 3;
    public final int MAX_AMMO = 100;
    public final int R = 2;//amplitude
    public final int ROT_SPEED = 4;

    private int hp = MAX_HP;
    private int lives = NUM_LIVES;
    private int x = 0;
    private int y = 0;
    private int vx = 0;
    private int vy = 0;
    private int vel = R;
    private int angle = 0;
    private int ammo = MAX_AMMO;
    private boolean doShoot = false;
    private boolean isDead = false;

    private BufferedImage img;
    private MediaPlayer boom;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    Tank(int x, int y, int angle, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.img = img;
        this.boom = new MediaPlayer(new Media(new File("resources/Explosion_large.wav").toURI().toString()));
    }

    public int getHP() { return hp; }
    public int getLives() { return lives; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getAmmo() { return ammo; }
    public int getWidth() { return this.img.getWidth(); }
    public int getHeight() { return this.img.getHeight(); }

    // moves the tank
    public void teleport(int x, int y) { this.x = x; this.y = y; }

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



    // updates the tank (called every frame) to move it towards where its heading at the given speed
    public void update(GameState state) {
        if (!isDead) {
            if (this.leftPressed) {
                this.rotateLeft();
            } else if (this.rightPressed) {
                this.rotateRight();
            } else if (this.upPressed) {
                this.moveForwards();
            } else if (this.downPressed) {
                this.moveBackwards();
            }

            if (this.doShoot) {
                this.doShoot = false;
                this.ammo -= 1;
                state.makeBullet(this, this.x, this.y, this.angle);
                boom.stop();
                boom.play();
            }
        }
    }

    private void rotateLeft() {
        this.angle -= this.ROT_SPEED;
    }

    private void rotateRight() {
        this.angle += this.ROT_SPEED;
    }

    private void moveBackwards() {
        vx = (int) Math.round(vel * Math.cos(Math.toRadians(angle)));
        vy = (int) Math.round(vel * Math.sin(Math.toRadians(angle)));
        x -= vx;
        y -= vy;
        checkBorder();
    }

    private void moveForwards() {
        vx = (int) Math.round(vel * Math.cos(Math.toRadians(angle)));
        vy = (int) Math.round(vel * Math.sin(Math.toRadians(angle)));

        x += vx;
        y += vy;
        // checkBorder();
    }

    // we dont need this anymore
    private void checkBorder() {
    /*    if (x < 0) {
            x = 0;
        } else if (x >= GameState.WIDTH - getWidth()) {
            x = GameState.WIDTH - getWidth();
        }

        if (y < 0) {
            y = 0;
        } else if (y >= GameState.HEIGHT - getHeight()) {
            y = GameState.HEIGHT - getHeight();
        }*/
    }

    @Override
    public String toString() {
        return "x=" + x + ", y=" + y + ", angle=" + angle;
    }

    // renders the tank on the minimap (called every frame)
    void drawImage(JPanel rs, Graphics g) { // minimap
        // transform [0, WIDTH] -> [0, MINIMAPWIDTH]
        float nx = (float)x/GameState.WIDTH;
        float ny = (float)y/GameState.HEIGHT;
        int sx = (int)(nx * rs.getWidth());
        int sy = (int)(ny * rs.getHeight());

        float scaleX = (float)rs.getWidth()/GameState.WIDTH;
        float scaleY = (float)rs.getHeight()/GameState.HEIGHT;
        int wx = (int)(this.img.getWidth() * scaleX);
        int wy = (int)(this.img.getHeight() * scaleY);

        AffineTransform rotation = AffineTransform.getTranslateInstance(sx, sy);
        rotation.rotate(Math.toRadians(angle), wx / 2.0, wy / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img.getScaledInstance(wx, wy, 0), rotation, null);;
    }

    // shoot if enough ammo
    public void shoot() {
        if (this.ammo > 0 && !isDead)
            this.doShoot = true;
    }

    // check collision between bullet and tank
    public boolean collide(Bullet bullet) {
        if (bullet.getTank() != this) {
            Rectangle r1 = new Rectangle(x, y, getWidth(), getHeight());
            Rectangle r2 = new Rectangle(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());

            if (r1.intersects(r2)) {
                this.hp = Math.max(0, this.hp - bullet.getDamage());
                checkDead();
                return true;
            }
        }
        return false;
    }

    // check collision between tanks
    public boolean collide(Tank tank) {
        Rectangle r1 = new Rectangle(x, y, img.getWidth(), img.getHeight());
        Rectangle r2 = new Rectangle(tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight());

        return r1.intersects(r2);
    }

    // check dead and respawn if possible
    public void checkDead() {
        if (this.hp == 0) {
            this.lives = Math.max(0, this.lives - 1);
            this.hp = MAX_HP;
            teleport(GameState.WIDTH/2, GameState.HEIGHT/2);
        }

        if (this.lives == 0)
            this.isDead = true;
    }

    // render the tank on a screen (called every frame)
    public void drawImage(JPanel rs, Graphics g, int ox, int oy) {
        if (!isDead) {
            int relx = x - ox;
            int rely = y - oy;
            int renderx = rs.getWidth() / 2 + relx;
            int rendery = rs.getHeight() / 2 + rely;

            AffineTransform rotation = AffineTransform.getTranslateInstance(renderx, rendery);
            rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(this.img, rotation, null);

            g2d.setColor(new Color(0, 0, 0));
            g2d.fillRect(renderx + this.img.getWidth() / 2 - 75 / 2, rendery - 20, 75, 10);
            g2d.setColor(new Color(255, 0, 0));
            g2d.fillRect(renderx + this.img.getWidth() / 2 - 75 / 2, rendery - 20, (int) ((float) hp / MAX_HP * 75), 10);
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public void setSpeed(int speed) {
        vel = speed;
    }

    public int getSpeed() {
        return vel;
    }
}
