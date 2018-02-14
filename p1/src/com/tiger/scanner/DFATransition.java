package com.tiger.scanner;

public class DFATransition {
    private char rangeStart;
    private char rangeEnd; // inclusive

    private int targetState;

    public DFATransition(char rangeStart, char rangeEnd, int targetState) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;

        if (rangeEnd < rangeStart)
            throw new IndexOutOfBoundsException("idi nahui");

        this.targetState = targetState;
    }

    public DFATransition(char symbol, int targetState) {
        this(symbol, symbol, targetState);
    }

    public void applyDFATransition(Integer[] arr){
        for (int i = rangeStart; i <= rangeEnd && i < arr.length; i++) {
            arr[i] = targetState;
        }
    }
}
