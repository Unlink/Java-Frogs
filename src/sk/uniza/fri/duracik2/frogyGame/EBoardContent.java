/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.uniza.fri.duracik2.frogyGame;

/**
 *
 * @author Unlink
 */
public enum EBoardContent {
    EMPTY('_'), BLUE('B'), RED('R');
    private final char aTextReprezentation;

    private EBoardContent(char aTextReprezentation) {
        this.aTextReprezentation = aTextReprezentation;
    }

    public char getTextReprezentation() {
        return aTextReprezentation;
    }

    @Override
    public String toString() {
        return ""+aTextReprezentation;
    }
    
    public boolean isEmpty() {
        return this == EMPTY;
    }
    
}
