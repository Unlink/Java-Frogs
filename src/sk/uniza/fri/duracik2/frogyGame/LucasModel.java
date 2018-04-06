/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.uniza.fri.duracik2.frogyGame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author Unlink
 */
public class LucasModel implements IPegControl {

    private final EBoardContent[] aPlocha;
    private EGameState aStavHry;
    private final Stack<Dvojhodnota<Integer, Integer>> aHistoria;
    private final Set<IChangeListener> aListnerers;
    
    public LucasModel(final int paPocetFigur) {
        aPlocha = new EBoardContent[paPocetFigur*2+1];
        aHistoria = new Stack<>();
        aListnerers = new HashSet<>();
        pripravPlochu();
    }
    
    private void pripravPlochu() {
        aStavHry = aStavHry.RUNNING;
        int pocetFigur = (aPlocha.length -1)/2;
        for (int i=0; i<pocetFigur; i++) {
            aPlocha[i] = EBoardContent.BLUE;
            aPlocha[i+pocetFigur+1] = EBoardContent.RED;
        }
        aPlocha[pocetFigur] = EBoardContent.EMPTY;
        aHistoria.clear();
    }

    public void addChangeListener(IChangeListener paChangeListener) {
        aListnerers.add(paChangeListener);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (EBoardContent c:aPlocha) {
            sb.append(c).append(' ');
        }
        return sb.toString();
    }
    
    public int getBoardSize() {
        return aPlocha.length;
    }
    
    @Override
    public void pegMove(int pegNumber) {
        pegNumber--;
        if (aPlocha[pegNumber] == EBoardContent.EMPTY)
            return;
        if (aPlocha[pegNumber] == EBoardContent.BLUE) {
            if (pegNumber+1 < aPlocha.length && aPlocha[pegNumber+1].isEmpty()) {
                presun(pegNumber, pegNumber+1);
                return;
            }
            else if (pegNumber+2 < aPlocha.length && aPlocha[pegNumber+2].isEmpty()) {
                presun(pegNumber, pegNumber+2);
                return;
            }
        }
        else if (aPlocha[pegNumber] == EBoardContent.RED) {
            if (pegNumber-1 >= 0 && aPlocha[pegNumber-1].isEmpty()) {
                presun(pegNumber, pegNumber-1);
                return;
            }
            else if (pegNumber-2 >= 0 && aPlocha[pegNumber-2].isEmpty()) {
                presun(pegNumber, pegNumber-2);
                return;
            }
        }
        if (!aHistoria.isEmpty() && aHistoria.lastElement().getVal2() == pegNumber) {
            undo();
        }
    }
    
    private void notifikujListenery(int pos) {
        for (IChangeListener c:aListnerers){
            c.modelChanged(pos);
        }
    }
    
    private void presun(int paFrom, int paTo) {
        aHistoria.add(new Dvojhodnota<>(paFrom, paTo));
        swap(paFrom, paTo);
        ocekujStav();
        notifikujListenery(paTo);
    }

    private void swap(int paFrom, int paTo) {
        EBoardContent pom = aPlocha[paFrom];
        aPlocha[paFrom] = aPlocha[paTo];
        aPlocha[paTo] = pom;
    }
    
    private void undo() {
        if (!aHistoria.isEmpty()) {
            Dvojhodnota<Integer, Integer> x = aHistoria.pop();
            swap(x.getVal2(), x.getVal1());
            notifikujListenery(x.getVal1());
        }
    }
    
    public void reset() {
        pripravPlochu();
        notifikujListenery(-1);
    }
    
    public EGameState getState() {
        return aStavHry;
    }
    
    public EBoardContent getFigureAt(final int paPos) {
        return aPlocha[paPos];
    }
    
    private void ocekujStav () {
        int figurok = aPlocha.length/2;
        //Victory
        boolean vas = true;
        for (int i=0; i<figurok; i++) {
            if (aPlocha[i] != EBoardContent.RED || aPlocha[i+figurok+1] != EBoardContent.BLUE) {
                vas = false;
            }
        }
        if (vas) {
            aStavHry = EGameState.VICTORY;
            return;
        }
        for (int i=0; i<aPlocha.length; i++) {
            if (dajMoznostiPohybu(i) >= 0) {
                aStavHry = EGameState.RUNNING;
                return;
            }
        }
        aStavHry = EGameState.LOST;
    }
    
    private int dajMoznostiPohybu(int i) {
        if (getFigureAt(i) == EBoardContent.BLUE) {
            if (i+1 < aPlocha.length && getFigureAt(i+1).isEmpty()){
                return (i+1);
            }
            else if (i+2 < aPlocha.length && getFigureAt(i+2).isEmpty()){
                return (i+2);
            }
        }
        else if (getFigureAt(i) == EBoardContent.RED) {
            if (i-1 >= 0 && getFigureAt(i-1).isEmpty()){
                return (i-1);
            }
            else if (i-2 >= 0 && getFigureAt(i-2).isEmpty()){
                return (i-2);
            }
        }
        return -1;
    }
}
