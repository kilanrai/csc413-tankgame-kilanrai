package tankgame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GameState {

    private JFrame window;
    private PlayerState player1, player2;

    public static void main(String[] args) {
        GameState state = new GameState();

        try {
            while (true) {
                state.window.revalidate();
                state.update();
                state.render();
                Thread.sleep(1000 / 144);
            }
        } catch (InterruptedException ignored) {

        }
    }

    public GameState() {
        JLayeredPane layout;
        GridBagConstraints constraints;

        window = new JFrame("Tank Game");
        layout = new JLayeredPane();
        constraints = new GridBagConstraints();

        window.setSize(new Dimension(1280, 960));
        window.setUndecorated(true);

        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(layout);

        try {
            BufferedImage background = ImageIO.read(new File("resources/Background.bmp"));

            player1 = new PlayerState(this, background, ImageIO.read(new File("resources/Tank1.gif")));
            player2 = new PlayerState(this, background, ImageIO.read(new File("resources/Tank2.gif")));

        } catch (Exception e) {
            System.err.println("*** could not read file: " + e.getMessage() + " ***");
            e.printStackTrace();
            System.exit(1);
        }

        //BufferedImage img = getWorld().getSubimage(0,0, 1200,920);// adding for mini map
         //buffer.drawImage(img.getScaledInstance(1000, 1000, 0), null,null);
        player1.getWorld().setLocation(0, 0);
        player1.getWorld().setSize(new Dimension(1280/2, 960));
        layout.add(player1.getWorld());

        player2.getWorld().setLocation(1280/2, 0);
        player2.getWorld().setSize(new Dimension(1280/2, 960));
        layout.add(player2.getWorld());

        player1.getMap().setSize(new Dimension(1280/4, 960/4));
        player1.getMap().setLocation(1280/2 - 1280/4 - 10, 10);
        layout.add(player1.getMap(), Integer.valueOf(10));
        /*
        player1.getMap().setSize(new Dimension(1280/4, 960/4));
        player1.getMap().setLocation(1280/2 - 1280/4 - 10, 10);
        layout.add(player1.getMap(), Integer.valueOf(10));*/
        //player2.getMap().setSize(new Dimension(1280/4, 960/4));
        //player2.getMap().setLocation(1280 - 1280/4 - 10, 10);
        //layout.add(player2.getMap(), Integer.valueOf(10));

        player1.getHud().setSize(new Dimension(150, 10));
        player1.getHud().setLocation(10, 900);
        layout.add(player1.getHud(), Integer.valueOf(10));

        player2.getHud().setSize(new Dimension(150, 10));
        player2.getHud().setLocation(1280/2 + 10, 900);
        layout.add(player2.getHud(), Integer.valueOf(10));

        TankControl tc1 = new TankControl(player1.getTank(), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
        TankControl tc2 = new TankControl(player2.getTank(), KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        window.addKeyListener(tc1);
        window.addKeyListener(tc2);

        window.setVisible(true);
    }

    public PlayerState getPlayer1() {
        return player1;
    }

    public PlayerState getPlayer2() {
        return player2;
    }

    public void update() {
        player1.getTank().update();
        player2.getTank().update();
    }

    public void render() {
        player1.render();
        player2.render();
    }
}

class PlayerState {
    private Tank tank;
    private World world;
    private Map map;
    private HUD hud;

    public PlayerState(GameState state, BufferedImage background, BufferedImage sprite) {
        this.tank = new Tank(0, 0, 0, sprite);
        this.world = new World(state, this, background);
        this.map = new Map(state, this, background);
        this.hud = new HUD(state, this, background);
    }

    public void render() {
        world.repaint();
        map.repaint();
        hud.repaint();
    }

    public World getWorld() {
        return world;
    }

    public Map getMap() {
        return map;
    }

    public HUD getHud() {
        return hud;
    }

    public Tank getTank() {
        return tank;
    }
}

class RenderState extends JPanel {
    protected final GameState state;
    protected final PlayerState player;
    protected final BufferedImage background;

    public RenderState(GameState state, PlayerState player, BufferedImage background) {
        this.state = state;
        this.player = player;
        this.background = background;
    }
}

class World extends RenderState {
    public World(GameState state, PlayerState player, BufferedImage background) {
        super(state, player, background);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        //BufferedImage img = world.getSubimage(0,0, SCREEN_WIDTH,SCREEN_HEIGHT);// adding for mini map
         //buffer.drawImage(img.getScaledInstance(1000, 1000, 0), null,null);
        super.paintComponent(g);

        g2.drawImage(background, -500 - player.getTank().getX(), -500 - player.getTank().getY(), this.getWidth()+1000, this.getHeight()+1000, null);
        state.getPlayer1().getTank().drawImage(this, g, player.getTank().getX(), player.getTank().getY());
        state.getPlayer2().getTank().drawImage(this, g, player.getTank().getX(), player.getTank().getY());
    }
}

class Map extends RenderState {

    public Map(GameState state, PlayerState player, BufferedImage background) {
        super(state, player, background);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g);

        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        state.getPlayer1().getTank().drawImage(this, g, player.getTank().getX(), player.getTank().getY(), 0.25f);
        state.getPlayer2().getTank().drawImage(this, g, player.getTank().getX(), player.getTank().getY(), 0.25f);
    }
}

class HUD extends RenderState {
    public HUD(GameState state, PlayerState player, BufferedImage background) {
        super(state, player, background);
    }
/*
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g);

        g2.setFont(new Font("TimesRoman", Font.PLAIN, 40));
        g2.setColor(new Color(0, 0, 0));
        g2.drawString("Ammo: " + player.getTank().getAmmo(), 120, 0);
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, 100, 10);
        g2.setColor(new Color(255, 0, 0));
        g2.fillRect(0, 0, (int)((float)player.getTank().getHP()/player.getTank().MAX_HP * 100), 10);
    }*/
}
