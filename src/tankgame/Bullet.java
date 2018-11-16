package tankgame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.*;
import javax.swing.*;
import java.io.*;
import javafx.scene.media.*;
import javafx.embed.swing.JFXPanel;

public class Bullet {
    static{
        JFXPanel fxPanel = new JFXPanel();
    }

    private Tank tank;
    private int x;
    private int y;
    private int vx;
    private int vy;
    private int ox, oy;
    private int angle;
    private BufferedImage img;
    private MediaPlayer boom;

    public Bullet(Tank tank, int x, int y, int angle) {
        try {
            this.tank = tank;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.vx = (int)(10*Math.cos(Math.toRadians(angle)));
            this.vy = (int)(10*Math.sin(Math.toRadians(angle)));
            this.ox = x;
            this.oy = y;
            this.img = ImageIO.read(new File("resources/Rocket.gif"));
            this.boom = new MediaPlayer(new Media(new File("resources/Explosion_large.wav").toURI().toString()));
        } catch (IOException e) {
            System.err.println("*** could not read file: " + e.getMessage() + " ***");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void playSound() {
        boom.play();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return img.getWidth(); }
    public int getHeight() { return img.getHeight(); }

    // updates bullet position every frame
    public boolean update() {
        x += vx;
        y += vy;

        if (x < 0 || x >= GameState.WIDTH || y < 0 || y >= GameState.HEIGHT)
            return false;

        int dx = x - ox;
        int dy = y - oy;

        if (dx*dx + dy*dy >= 100*100) // travelled 100 units
            return false;

        return true;
    }

    public Tank getTank() {
        return tank;
    }

    public int getDamage() {
        return 10;
    }

    // draws bullet every frame
    public void drawImage(JPanel rs, Graphics g, int ox, int oy) {
        int relx = x - ox;
        int rely = y - oy;
        int renderx = rs.getWidth()/2 + relx;
        int rendery = rs.getHeight()/2 + rely;

        AffineTransform rotation = AffineTransform.getTranslateInstance(renderx, rendery);
        rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, rotation, null);
    }
}
