package com.tiger.scanner;

/**
 * For nested comment lexicon
 */

public class CommentMatcher implements TokenMatcher {

    private Character previousChar;
    private int stack = 0;
    private State stateType = State.NORMAL;
    private TigerToken token;

    private int readCounter = -1;

    @Override
    public State feedChar(char input) {
        readCounter++;
        if (stateType == State.ERROR) {
            return stateType;
        }

        if (stateType == State.ACCEPTING || stack == 0 && readCounter > 1) {
            stateType = State.ERROR;
            return stateType;
        }

        if (readCounter == 0 && input != '/' ||
                readCounter == 1 && previousChar != null && previousChar == '/' && input != '*') {
            stateType = State.ERROR;
            return stateType;
        }

        if (previousChar != null && previousChar == '/' && input == '*') {
            stack++;
            previousChar = null;
            stateType = State.NORMAL;
        } else if (previousChar != null && previousChar == '*' && input == '/'){
            stack--;
            // very important detail here TODO
            previousChar = null;
            if (stack == 0) {
                stateType = State.ACCEPTING;
            }
        } else {
            previousChar = input;
        }

        if (stateType == State.ACCEPTING) {
            token = new TigerToken(TokenType.COMMENT);
        }
        return stateType;
    }

    @Override
    public void reset() {
        stack = 0;
        previousChar = null;
        readCounter = -1;
        stateType = State.NORMAL;
    }

    @Override
    public TigerToken getToken() {
        return token;
    }
}
