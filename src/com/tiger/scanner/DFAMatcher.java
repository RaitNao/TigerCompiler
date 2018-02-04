package com.tiger.scanner;

import java.util.ArrayList;

public class DFAMatcher implements TokenMatcher {
    private int currentState = 0;
    private int startState = currentState;
    private boolean inAcceptState = false;

    private ArrayList<Integer[]> adjMatrix = new ArrayList<>();

    /**
     * TODO
     * @param transitions
     */
    public void addState(DFATransition[] transitions) {
        Integer[] nextStates = new Integer[Character.MAX_VALUE + 1];
        for (int i = 0; i < nextStates.length; i++) {
            nextStates[i] = -1;
        }
        for (DFATransition transition: transitions) {
            transition.applyDFATransition(nextStates);
        }
        adjMatrix.add(nextStates);
    }

    @Override
    public boolean feedChar(char input) {
        int nextState = adjMatrix.get(currentState)[input];
        if (nextState == -1) {
            return false;
        } else {
            currentState = nextState;
            return true;
        }

    }

    @Override
    public void reset() {
        currentState = startState;
        inAcceptState = false;
    }


    @Override
    public boolean isInAcceptState() {
        return inAcceptState;
    }
}
