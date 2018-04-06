/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.uniza.fri.duracik2.frogyGame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 *
 * @author Unlink
 */
public class Program {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame jf = new JFrame("Žabičky");
        LucasModel lm = new LucasModel(3);
        JLucasView lw = new JLucasView(lm);
        lm.addChangeListener(lw);
        jf.getContentPane().add(lw);
        jf.setMinimumSize(new Dimension(750, 400));
        jf.pack();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        
    }
    
}
