package com.tiger.scanner;

public class CommentMatcher implements TokenMatcher {

    private Character previousChar;
    private int stack = 0;
    private boolean commentStart = true;
    private boolean inAcceptState = false;

    @Override
    public boolean feedChar(char input) {
        if (inAcceptState == true || stack == 0 && !(input == '/' ||  (input == '*' && previousChar != null && previousChar == '/'))) {
            inAcceptState = false;
            return false;
        }

        if (commentStart && input != '/' ||
                previousChar != null && previousChar == '/' && input != '*') {
            return false;
        }

        if (previousChar != null && previousChar == '/' && input == '*') {
            stack++;
            previousChar = null;
            inAcceptState = false;
        } else if (previousChar != null && previousChar == '*' && input == '/'){
            stack--;
            // very important detail here TODO
            previousChar = null;
            if (stack == 0) {
                inAcceptState = true;
            }
        } else {
            previousChar = input;
        }

        commentStart = false;
        return true;
    }

    @Override
    public void reset() {
        stack = 0;
        previousChar = null;
        inAcceptState = false;
    }

    @Override
    public boolean isInAcceptState() {
        return inAcceptState;
    }
}
