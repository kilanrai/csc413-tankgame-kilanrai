package tankgame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public class Wall {
    public static final int WIDTH = 50;
    public static final int HEIGHT = 50;

    private int x;
    private int y;
    private int w;
    private int h;
    private BufferedImage current;
    private Image img;
    private int state;

    // wall with default width and height
    public Wall(int x, int y, boolean breakable) {
        this(x, y, breakable, WIDTH, HEIGHT);
    }

    // wall with default height
    public Wall(int x, int y, boolean breakable, int width) {
        this(x, y, breakable, width, HEIGHT);
    }

    public Wall(int x, int y, boolean breakable, int width, int height) {
        try {
            this.x = x;
            this.y = y;
            this.w = width;
            this.h = height;
            this.current = breakable ? ImageIO.read(new File("resources/Wall2.gif")) : ImageIO.read(new File("resources/Wall1.gif"));
            this.state = breakable ? 2 : -1;
            this.img = this.current.getScaledInstance(width, height, 0);
        } catch(IOException e) {
            System.err.println("*** could not read file: " + e.getMessage() + " ***");
            e.printStackTrace();
            System.exit(1);
        }

    }

    // check collision between wall and tank
    public boolean collide(Tank tank) {
        Rectangle r1 = new Rectangle(x, y, w, h);
        Rectangle r2 = new Rectangle(tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight());

        return r1.intersects(r2);
    }

    // check collision between wall and bullet
    public boolean collide(Bullet bullet) {
        Rectangle r1 = new Rectangle(x, y, w, h);
        Rectangle r2 = new Rectangle(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());

        return r1.intersects(r2);
    }

    // damage the wall when it's hit by a bullet if it's breakable
    public int damage(Bullet bullet) { // 0 - nothing, 1 - collide, 2 - die
        if (collide(bullet)) {
            if (this.state == 2) // is breakable, damage
                this.state = 1;
            else if (this.state == 1) // is breakable, destroy after damaged
                return 2; // collided, destroy wall

            return 1; // collided
        }

        return 0; // no collision
    }

    // render wall on minimap (called every frame)
    public void drawImage(JPanel rs, Graphics g) { // minimap
        // transform [0, WIDTH] -> [0, MINIMAPWIDTH]
        float nx = (float)x/GameState.WIDTH;
        float ny = (float)y/GameState.HEIGHT;
        int sx = (int)(nx * rs.getWidth());
        int sy = (int)(ny * rs.getHeight());

        float scaleX = (float)rs.getWidth()/GameState.WIDTH;
        float scaleY = (float)rs.getHeight()/GameState.HEIGHT;
        int wx = (int)(w * scaleX);
        int wy = (int)(h * scaleY);

        AffineTransform rotation = AffineTransform.getTranslateInstance(sx, sy);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.current.getScaledInstance(wx, wy, 0), rotation, null);
    }

    // render wall (called every frame)
    public void drawImage(JPanel rs, Graphics g, int ox, int oy) {
        int relx = x - ox;
        int rely = y - oy;
        int renderx = rs.getWidth()/2 + relx;
        int rendery = rs.getHeight()/2 + rely;

        AffineTransform rotation = AffineTransform.getTranslateInstance(renderx, rendery);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, rotation, null);
    }
}
