/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tankgame;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;

import java.util.Objects;

import static javax.imageio.ImageIO.read;

public class TankGame extends JPanel  {


    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 960;

    private BufferedImage world;
    private Graphics2D buffer;
    private JFrame jf;
    private Tank t1, t2;


    public static void main(String[] args) {
        Thread x;
        TankGame trex = new TankGame();
        trex.init();
        try {

            while (true) {
                // trex.paintComponent(trex.world.createGraphics());//added this line to clear trail
                trex.t1.update();
                trex.t2.update();
                trex.repaint();

                //System.out.println(trex.t1);
                Thread.sleep(1000 / 144);
            }
        } catch (InterruptedException ignored) {

        }

    }


    private void init() {
        this.jf = new JFrame("Tank Rotation");
        this.world = new BufferedImage(TankGame.SCREEN_WIDTH, TankGame.SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
        BufferedImage t1img = null, t2img = null;
        try {
            BufferedImage tmp;
            System.out.println(System.getProperty("user.dir"));
            /*
             * note class loaders read files from the out folder (build folder in netbeans) and not the
             * current working directory.
             */
            t1img = read(new File("resources/Tank1.gif"));


        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        t1 = new Tank(200, 200, 0, t1img);
        t2 = new Tank(400, 400, 0, t1img);

        TankControl tc1 = new TankControl(t1, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
        TankControl tc2 = new TankControl(t2, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);

        this.jf.setLayout(new BorderLayout());
        this.jf.add(this);


        this.jf.addKeyListener(tc1);
        this.jf.addKeyListener(tc2);


        this.jf.setSize(TankGame.SCREEN_WIDTH, TankGame.SCREEN_HEIGHT + 30);
        this.jf.setResizable(false);
        jf.setLocationRelativeTo(null);

        this.jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.jf.setVisible(true);


    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
       buffer = world.createGraphics();
       // super.paintComponent(g2);

        //BufferedImage img = world.getSubimage(0,0, SCREEN_WIDTH,SCREEN_HEIGHT);// adding for mini map
       // buffer.drawImage(img.getScaledInstance(1000, 1000, 0), null,null);
       super.paintComponent(g2);
        //just testing
        g2.drawImage(world,0,0,null);
        //this.t1.drawImage(g2);
      //  this.t2.drawImage(g2);

    }


}
