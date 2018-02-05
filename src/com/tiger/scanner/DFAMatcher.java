package com.tiger.scanner;

import java.util.ArrayList;
import java.util.List;

public class DFAMatcher implements TokenMatcher {
    private int currentState = 0;
    private int startState = currentState;
    private State stateType = State.NORMAL;

    private List<Integer[]> adjMatrix = new ArrayList<>();
    private List<Boolean> acceptingStates = new ArrayList<>();

    /**
     * TODO
     * @param transitions
     * @param isAccepting
     */
    public void addState(DFATransition[] transitions, boolean isAccepting) {
        Integer[] nextStates = new Integer[Character.MAX_VALUE + 1];
        for (int i = 0; i < nextStates.length; i++) {
            nextStates[i] = -1;
        }
        for (DFATransition transition: transitions) {
            transition.applyDFATransition(nextStates);
        }
        adjMatrix.add(nextStates);
        acceptingStates.add(isAccepting);
    }

    @Override
    public State feedChar(char input) {
        if (stateType == State.ERROR) {
            return stateType;
        }

        int nextState = adjMatrix.get(currentState)[input];
        if (nextState == -1) {
            stateType = State.ERROR;
        } else {
            currentState = nextState;
            stateType = acceptingStates.get(currentState) ? State.ACCEPTING : State.NORMAL;
        }
        return stateType;
    }

    @Override
    public void reset() {
        currentState = startState;
        stateType = State.NORMAL;
    }

}
