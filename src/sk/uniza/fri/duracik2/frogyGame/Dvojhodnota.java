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
public class Dvojhodnota<T1, T2> {
    private final T1 aVal1;
    private final T2 aVal2;

    public Dvojhodnota(T1 aVal1, T2 aVal2) {
        this.aVal1 = aVal1;
        this.aVal2 = aVal2;
    }

    public T1 getVal1() {
        return aVal1;
    }

    public T2 getVal2() {
        return aVal2;
    }
    
    
}
