package tankgame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

abstract public class Powerup {
    private int x;
    private int y;
    private BufferedImage img;

    public Powerup(int x, int y) {
        try {
            this.x = x;
            this.y = y;
            this.img = ImageIO.read(new File("resources/Pickup.gif"));
        } catch (IOException e) {
            System.err.println("*** could not read file: " + e.getMessage() + " ***");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // draw the powerup
    public void drawImage(JPanel rs, Graphics g, int ox, int oy) {
        int relx = x - ox;
        int rely = y - oy;
        int renderx = rs.getWidth()/2 + relx;
        int rendery = rs.getHeight()/2 + rely;

        AffineTransform rotation = AffineTransform.getTranslateInstance(renderx, rendery);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, rotation, null);
    }

    // draw the powerup on the minimap
    public void drawImage(JPanel rs, Graphics g) { // minimap
        // transform [0, WIDTH] -> [0, MINIMAPWIDTH]
        float nx = (float)x/GameState.WIDTH;
        float ny = (float)y/GameState.HEIGHT;
        int sx = (int)(nx * rs.getWidth());
        int sy = (int)(ny * rs.getHeight());

        float scaleX = (float)rs.getWidth()/GameState.WIDTH;
        float scaleY = (float)rs.getHeight()/GameState.HEIGHT;
        int wx = (int)(img.getWidth() * scaleX);
        int wy = (int)(img.getHeight() * scaleY);

        AffineTransform rotation = AffineTransform.getTranslateInstance(sx, sy);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img.getScaledInstance(wx, wy, 0), rotation, null);
    }

    // check for collision between tank and powerup
    public boolean collides(Tank tank) {
        Rectangle r1 = new Rectangle(x, y, img.getWidth(), img.getHeight());
        Rectangle r2 = new Rectangle(tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight());

        return r1.intersects(r2);
    }

    // execute functionality of powerup
    abstract void execute(Tank tank);
}

class BoostPowerup extends Powerup {
    public BoostPowerup(int x, int y) {
        super(x, y);
    }

    // add 2 to speed
    @Override
    void execute(Tank tank) {
        tank.setSpeed(tank.getSpeed() + 2);
    }
}
