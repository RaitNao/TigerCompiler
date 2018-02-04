package com.tiger.scanner;

public class KeywordMatcher implements TokenMatcher {

    private int currentIndex = 0;
    private String keyword;
    private boolean inAcceptState = false;

    public KeywordMatcher(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean feedChar(char input) {
        if (input == keyword.charAt(currentIndex)) {
            currentIndex++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset(){
        currentIndex = 0;
        inAcceptState = false;
    }

    @Override
    public boolean isInAcceptState() {
        return inAcceptState;
    }
}
