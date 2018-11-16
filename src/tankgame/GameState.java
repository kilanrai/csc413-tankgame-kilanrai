package tankgame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.ArrayList;

// entire state of the game: players, walls, bullets, powerups, etc.
// main function continues to update and render the game every frame
// it also adds powerups randomly on the map so there's always 10
// the main loop is terminated if anyone dies
public class GameState {

    public static final int WIDTH = 1280*2;
    public static final int HEIGHT = 960*2;

    private Tank winner = null;
    private JFrame window;
    private PlayerState player1, player2;
    private JPanel minimap;
    private BufferedImage background;
    private ArrayList<Wall> walls;
    private ArrayList<Bullet> bullets;
    private ArrayList<Powerup> powerups;

    public static void main(String[] args) {
        GameState state = new GameState();

        try {
            while (true) {
                while (state.powerups.size() < 10)
                    state.powerups.add(new BoostPowerup((int)(WIDTH * Math.random()), (int)(HEIGHT * Math.random())));

                state.window.revalidate();
                state.update();
                state.window.repaint();

                if (state.player1.getTank().isDead()) {
                    state.winner = state.player2.getTank();
                    break;
                } else if (state.player2.getTank().isDead()) {
                    state.winner = state.player1.getTank();
                    break;
                }

                Thread.sleep(1000 / 144);
            }
        } catch (InterruptedException ignored) {

        }
    }

    public GameState() {
        JLayeredPane layout;

        walls = new ArrayList<>();
        bullets = new ArrayList<>();
        powerups = new ArrayList<>();
        window = new JFrame("Tank Game");
        minimap = new Minimap(this);

        layout = new JLayeredPane();

        window.setSize(new Dimension(1280, 960));
        window.setUndecorated(true);

        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(layout);

        try {
            background = ImageIO.read(new File("resources/Background.jpg"));

            player1 = new PlayerState(this, background, new Tank(WIDTH/4, HEIGHT/2, 0, ImageIO.read(new File("resources/Tank1.gif"))));
            player2 = new PlayerState(this, background, new Tank(3*WIDTH/4, HEIGHT/2, 0, ImageIO.read(new File("resources/Tank2.gif"))));
        } catch (Exception e) {
            System.err.println("*** could not read file: " + e.getMessage() + " ***");
            e.printStackTrace();
            System.exit(1);
        }

        minimap.setLocation(1280/2 - 1280/8, 10);
        minimap.setSize(new Dimension(1280/4, 960/4));
        layout.add(minimap);

        player1.setLocation(0, 0);
        player1.setSize(new Dimension(1280/2, 960));
        layout.add(player1);

        player2.setLocation(1280/2, 0);
        player2.setSize(new Dimension(1280/2, 960));
        layout.add(player2);

        TankControl tc1 = new TankControl(player2.getTank(), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
        TankControl tc2 = new TankControl(player1.getTank(), KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        window.addKeyListener(tc1);
        window.addKeyListener(tc2);

        /// Generate Walls
        {
            for (int y = -5*Wall.HEIGHT; y <= 5*Wall.HEIGHT; y += Wall.HEIGHT) {
                for (int x = -5 * Wall.WIDTH; x <= 5 * Wall.WIDTH; x += 10 * Wall.WIDTH) {
                    walls.add(new Wall(WIDTH/2 + x, HEIGHT/2 + y, Math.random() < 0.75));
                }
            }

            walls.add(new Wall(0, 0, false, WIDTH));
            walls.add(new Wall(0, HEIGHT - Wall.HEIGHT, false, WIDTH));
            walls.add(new Wall(0, 0, false, Wall.WIDTH, HEIGHT));
            walls.add(new Wall(WIDTH - Wall.WIDTH, 0, false, Wall.WIDTH, HEIGHT));
        }

        window.setVisible(true);
    }

    // called every frame to update the game
    public void update() {
        // update player1
        {
            int px = player1.getTank().getX();
            int py = player1.getTank().getY();

            player1.getTank().update(this);

            if (collidesWall(player1.getTank()) != null)
                player1.getTank().teleport(px, py);

            if (player1.getTank().collide(player2.getTank()))
                player1.getTank().teleport(px, py);
        }

        // update player2
        {
            int px = player2.getTank().getX();
            int py = player2.getTank().getY();

            player2.getTank().update(this);

            if (collidesWall(player2.getTank()) != null)
                player2.getTank().teleport(px, py);

            if (player2.getTank().collide(player1.getTank()))
                player2.getTank().teleport(px, py);
        }

        // check for bullet collisions and update bullets
        {
            for (int idx = bullets.size() - 1; idx >= 0; idx--) {
                Bullet bullet = bullets.get(idx);

                bullet.update();

                if (player1.getTank().collide(bullet) || player2.getTank().collide(bullet)) {
                    bullet.playSound();
                    bullets.remove(idx);
                    continue;
                }

                for (int idx2 = walls.size() - 1; idx2 >= 0; idx2--) {
                    Wall wall = walls.get(idx2);
                    int act = wall.damage(bullet); // 0 - nothing, 1 - collide, 2 - die

                    if (act != 0) {
                        if (act == 2)
                            walls.remove(idx2);

                        bullet.playSound();
                        bullets.remove(idx);
                        break;
                    }
                }
            }
        }

        // check for powerup collisions
        {
            for (int idx = powerups.size() - 1; idx >= 0; idx--) {
                Powerup powerup = powerups.get(idx);

                if (powerup.collides(player1.getTank())) {
                    powerup.execute(player1.getTank());
                    powerups.remove(idx);
                    continue;
                } else if (powerup.collides(player2.getTank())) {
                    powerup.execute(player2.getTank());
                    powerups.remove(idx);
                    continue;
                }
            }
        }
    }

    // creates a new bullet
    public void makeBullet(Tank t, int x, int y, int angle) {
        bullets.add(new Bullet(t, x, y, angle));
    }

    // check if a tank collides with any wall and return the wall if so
    public Wall collidesWall(Tank tank) {
        for (Wall wall : walls)
            if (wall.collide(tank))
                return wall;

        return null;
    }

    // called every frame to render the minimap
    public void render(JPanel rs, Graphics g) { // for minimap
        player1.getTank().drawImage(rs, g);
        player2.getTank().drawImage(rs, g);

        g.setFont(new Font("TimesRoman", Font.PLAIN, 20));

        // Player 1 HUD
        {
            String ammo = "Ammo: " + player1.getTank().getAmmo();
            String lives = "Lives: " + player1.getTank().getLives();

            g.setColor(new Color(255, 255, 255));
            g.drawString(ammo, 10, rs.getHeight() - 30);
            g.drawString(lives, 10, rs.getHeight() - 10);
        }

        // Player 2 HUD
        {
            String ammo = "Ammo: " + player2.getTank().getAmmo();
            String lives = "Lives: " + player2.getTank().getLives();

            g.setColor(new Color(255, 255, 255));
            g.drawString(ammo, rs.getWidth() - g.getFontMetrics().stringWidth(ammo) - 10, rs.getHeight() - 30);
            g.drawString(lives, rs.getWidth() - g.getFontMetrics().stringWidth(lives) - 10, rs.getHeight() - 10);
        }

        // walls on minimap
        for (Wall wall : walls)
            wall.drawImage(rs, g);

        // powerups on minimap
        for (Powerup powerup : powerups)
            powerup.drawImage(rs, g);
    }

    // called every frame by the players to render the world
    public void render(JPanel rs, Graphics g, int ox, int oy) {
        // render tanks
        player1.getTank().drawImage(rs, g, ox, oy);
        player2.getTank().drawImage(rs, g, ox, oy);

        // render walls
        for (Wall wall : walls)
            wall.drawImage(rs, g, ox, oy);

        // render bullets
        for (Bullet bullet : bullets)
            bullet.drawImage(rs, g, ox, oy);

        // render powerups
        for (Powerup powerup : powerups)
            powerup.drawImage(rs, g, ox, oy);
    }

    public Tank getWinner() {
        return winner;
    }
}

// holds player information and is the screen
class PlayerState extends JPanel {
    private BufferedImage background;
    private GameState state;
    private Tank tank;

    public PlayerState(GameState state, BufferedImage background, Tank tank) {
        this.background = background;
        this.state = state;
        this.tank = tank;
    }

    public Tank getTank() {
        return tank;
    }

    // renders this player's screen
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int sx = GameState.WIDTH / getWidth();
        int sy = GameState.HEIGHT / getHeight();

        for (int x = 0; x <= sx; x++) {
            for (int y = 0; y <= sy; y++) {
                int px = getWidth() * x - tank.getX();
                int py = getHeight() * y - tank.getY();

                g.drawImage(background, px, py, getWidth(), getHeight(), null);
            }
        }

        state.render(this, g, tank.getX(), tank.getY());

        if (state.getWinner() != null) {
            String s = state.getWinner() == tank ? "You win!" : "You lose!";
            g.setFont(new Font("TimesRoman", Font.PLAIN, 80));

            g.setColor(new Color(0, 0, 0, 0.25f));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.drawString(s, getWidth()/2 - g.getFontMetrics().stringWidth(s)/2, getHeight()/2);
        }
    }
}

// minimap screen
class Minimap extends JPanel {
    private GameState state;

    public Minimap(GameState state) {
        this.state = state;
    }

    // renders the minimap
    @Override
    public void paintComponent(Graphics g) {
     //   super.paintComponent(g);

        g.setColor(new Color(0, 0, 0, 0.5f));
        g.fillRect(0, 0, getWidth(), getHeight());

        state.render(this, g);
    }
}