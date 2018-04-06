/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.uniza.fri.duracik2.frogyGame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 *
 * @author Unlink
 */
public class JLucasView extends JComponent implements IChangeListener {
    
    private LucasModel aLucasModel;
    /**
     * Rozmer policka
     */
    private int aRozmer;
    private int aDelta;
    /**
     * Odsadenie od vrchu
     */
    private int aBottomcap;
    /**
     * Oblasť reset tlacidka
     */
    private Rectangle aResetButton;
    /**
     * Oblasti policok
     */
    private Rectangle[] aAreas;
    
    /**
     * Cache obrazkov scalovaných
     */
    private HashMap<String, BufferedImage> aCache;
    private Font aFont;
    
    /**
     * Animacia
     */
    private int[] aAnimacia;
    private int aAnimaciaSteps = 50;
    private int aCurrStep;
    
    private Lock aLock = new ReentrantLock();
    
    /**
     * Debuging
     */
    private boolean DEBUG = false;
    
    public JLucasView() {
        this(null);
    }

    public JLucasView(final LucasModel aLucasModel) {
        this.aLucasModel = aLucasModel;
        aRozmer = 0;
        aDelta = 0;
        aBottomcap = 0;
        aResetButton = new Rectangle();
        aCache = new HashMap<>();
        //aAnimacia = new int[]{2,3};
        if (aLucasModel != null) {
            aAreas = new Rectangle[aLucasModel.getBoardSize()];
            for (int i=0; i<aAreas.length; i++)
                aAreas[i] = new Rectangle();
        }
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                aCache.clear();
                spocitajProporcie();
            }
            
        });
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (aLucasModel == null)
                    return;
                if (aLock.tryLock()) {
                     try {
                        Point p = e.getPoint();
                        if (aResetButton.contains(p)) {
                            aLucasModel.reset();
                        }
                        else {
                            for (int i=0; i<aAreas.length; i++) {
                                if (aAreas[i].contains(p)) {
                                    FrogSoundPlayer.playSound();
                                    aLucasModel.pegMove(i+1);
                                    return;
                                }
                            }
                        }
                     }
                     finally {
                         aLock.unlock();
                     }
                }
            }
        });
        
        addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() > '0' && e.getKeyChar() <= '9') {
                    if (aLucasModel == null)
                        return;
                    if (aLock.tryLock()) {
                        try {
                            FrogSoundPlayer.playSound();
                            aLucasModel.pegMove(Integer.valueOf(""+e.getKeyChar()));
                        }
                        finally {
                             aLock.unlock();
                        }
                    }
                }
            }
            
        });
        
        setFocusable(true);
        requestFocusInWindow();
        
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        //Load font
        try {
            aFont = Font.createFont(Font.TRUETYPE_FONT, (getClass().getResource("/resources/URANIUM ___.ttf").openStream()));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(aFont);
        } catch (FontFormatException | IOException ex) {
            aFont = new Font("Arial", Font.BOLD, 15);
        }
        
        spocitajProporcie();
    }
    
    public void setModel(LucasModel paLucasModel) {
        aLucasModel = paLucasModel;
        aAreas = new Rectangle[aLucasModel.getBoardSize()];
        for (int i=0; i<aAreas.length; i++)
            aAreas[i] = new Rectangle();
        spocitajProporcie();
        repaint();
    }
    
    public LucasModel getModel() {
        return aLucasModel;
    }

    @Override
    public void modelChanged(int pos) {
        if (pos >= 0) {
            int x = 0;
            for (int i=0; i<aLucasModel.getBoardSize(); i++)
                if (aLucasModel.getFigureAt(i) == EBoardContent.EMPTY)
                    x = i;
            
            aAnimacia = new int[]{x,pos};
            animuj();
        }
        else {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        
        //Vykreslíme pozadie
        vykresliPozadie(g2);
        
        //Reset button
        vykresliReset(g2);
        
        if (aRozmer == 0)
            return;
        
        
        if (aLucasModel.getState() != EGameState.RUNNING && aAnimacia == null) {
            g2.setColor(Color.WHITE);
            g2.setFont(aFont);
                
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight())/2;
            int x = (getWidth() - fm.stringWidth(aLucasModel.getState().toString()))/2;
            y += fm.getAscent();

            g2.drawString(aLucasModel.getState().toString(), x, y);
        }
        else {
            for (int i=0; i<aLucasModel.getBoardSize(); i++) {
                vykresliLekno(g2, i);
            }
            for (int i=0; i<aLucasModel.getBoardSize(); i++) {
                vykresliZabu(g2, aLucasModel.getFigureAt(i), i);
                if (DEBUG) {
                    g2.setColor(Color.GRAY);
                    g2.draw(aAreas[i]);
                }
            }
            for (int i=0; i<aLucasModel.getBoardSize(); i++) {
                vykresliCislo(g2, i);
            }
        }
    }
    
    private void spocitajProporcie() {
        aBottomcap = (int) (getHeight()*0.4);
        if (aLucasModel != null) {
            aRozmer = Math.min(getHeight()-aBottomcap, getWidth()/aLucasModel.getBoardSize());
            aDelta = (int) (aRozmer*0.2);
            for (int i=0; i<aLucasModel.getBoardSize(); i++) {
                aAreas[i].setBounds(i*aRozmer, aBottomcap, aRozmer-2, aRozmer-2);
            }
        }
        aFont = aFont.deriveFont(getHeight()*0.3f);
    }
    
    private void loadAndScale(String path, BufferedImage bi) throws IOException {
        bi.getGraphics().drawImage(ImageIO.read(getClass().getResource(path)), 0, 0, bi.getWidth(), bi.getHeight(), null);
    }
    
    private void vykresliPozadie(Graphics2D g2) {
        if (!aCache.containsKey("pozadie")) {
            BufferedImage pozadie = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            try {
                loadAndScale("/resources/background.png", pozadie);
            } catch (IOException ex) {
            }
            aCache.put("pozadie", pozadie);
        }
        g2.drawImage(aCache.get("pozadie"), 0, 0, null);
    }

    private void vykresliReset(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int fw = fm.stringWidth("Reset");
        g2.drawString("Reset", getWidth()-fw-10, 20);
        aResetButton.setBounds(getWidth()-fw-10, 10, fw, fm.getHeight());
        if (DEBUG) {
            g2.setColor(Color.GRAY);
            g2.draw(aResetButton);
        }
    }

    private void vykresliLekno(Graphics2D g2, int i) {
        if (!aCache.containsKey("lekno")) {
            BufferedImage pozadie = new BufferedImage(aRozmer, aRozmer, BufferedImage.TYPE_INT_ARGB);
            try {
                loadAndScale("/resources/lekno.png", pozadie);
            } catch (IOException ex) {
            }
            aCache.put("lekno", pozadie);
        }
        g2.drawImage(aCache.get("lekno"), aAreas[i].x, aAreas[i].y, null);
    }

    private void vykresliZabu(Graphics2D g2, EBoardContent figureAt, int i) {
        if (!aCache.containsKey("figurka_"+figureAt)) {
            BufferedImage pozadie = new BufferedImage(aRozmer-2*aDelta, aRozmer-2*aDelta, BufferedImage.TYPE_INT_ARGB);
            try {
                if (figureAt == EBoardContent.BLUE)
                    loadAndScale("/resources/blue.png", pozadie);
                else if (figureAt == EBoardContent.RED)
                    loadAndScale("/resources/green.png", pozadie);
            } catch (IOException ex) {
            }
            aCache.put("figurka_"+figureAt, pozadie);
        }
        
        
        if (aAnimacia == null || aAnimacia[1] != i) {
            g2.drawImage(aCache.get("figurka_"+figureAt), aAreas[i].x+aDelta, aAreas[i].y+aDelta, null);
        }
        else {
            int multiplier = Math.abs(aAnimacia[0] - aAnimacia[1]);
            int znamienko = (aAnimacia[0] > aAnimacia[1]) ? -1 : 1;
            
            
            int x = (int) (aAreas[aAnimacia[0]].x + aDelta + znamienko*multiplier*aRozmer*((aCurrStep)/(double)aAnimaciaSteps));
            int y = (int) (aAreas[aAnimacia[0]].y + aDelta - multiplier*(aRozmer*(Math.sin(Math.toRadians(180.f/aAnimaciaSteps * aCurrStep))))/2);
            
            g2.drawImage(aCache.get("figurka_"+figureAt), x, y, null);
        }
    }
    
    private void vykresliCislo(Graphics2D g2, int i) {
        int polsir = (aRozmer-aDelta) / 6;
        g2.setColor(new Color(149, 165, 166, 125));
        g2.fill(new Ellipse2D.Double(aAreas[i].x+aDelta+polsir*1.2, aAreas[i].y+aDelta+polsir*1.5, 2*polsir, 2*polsir));
        g2.setColor(new Color(149, 165, 166, 255));
        g2.draw(new Ellipse2D.Double(aAreas[i].x+aDelta+polsir*1.2, aAreas[i].y+aDelta+polsir*1.5, 2*polsir, 2*polsir));
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(polsir*1f));
        g2.drawString(""+(i+1), aAreas[i].x+aDelta+polsir*1.91f, aAreas[i].y+aDelta+polsir*2.91f);
    }
    
    
    private void animuj() {
        (new Thread() {

            @Override
            public void run() {
                aLock.lock();
                try {
                    for (int i=0; i<aAnimaciaSteps; i++) {
                        aCurrStep = i;
                        repaint();

                        try {
                            Thread.sleep(1000/aAnimaciaSteps);
                        } catch (InterruptedException ex) {
                        }
                    }
                    aAnimacia = null;
                    repaint();
                }
                finally {
                    aLock.unlock();
                }
            }
            
        }).start();
    }
    
}
