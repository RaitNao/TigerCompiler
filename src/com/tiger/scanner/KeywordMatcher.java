package com.tiger.scanner;

public class KeywordMatcher implements TokenMatcher {

    private int currentIndex = 0;
    private String keyword;
    private State stateType = State.NORMAL;

    public KeywordMatcher(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public State feedChar(char input) {
        if (stateType == State.ERROR) {
            return stateType;
        }

        if (stateType == State.ACCEPTING) {
            stateType = State.ERROR;
            return stateType;
        }

        if (input == keyword.charAt(currentIndex)) {
            if (currentIndex == keyword.length() - 1) {
                stateType = State.ACCEPTING;
            }
            currentIndex++;
        } else {
            stateType = State.ERROR;
        }

        return stateType;
    }

    @Override
    public void reset(){
        currentIndex = 0;
        stateType = State.NORMAL;
    }

}
