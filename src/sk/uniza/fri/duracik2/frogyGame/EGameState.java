/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.uniza.fri.duracik2.frogyGame;

/**
 *
 * @author Unlink
 */
public enum EGameState {
    RUNNING("Bežiaca"),VICTORY("Zvitazil si"),LOST("Prehral si");
    
    private final String aText;

    private EGameState(String aText) {
        this.aText = aText;
    }

    @Override
    public String toString() {
        return aText;
    }
    
    
}
