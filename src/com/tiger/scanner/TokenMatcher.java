package com.tiger.scanner;

public interface TokenMatcher {
    boolean feedChar(char input);
    void reset();
    boolean isInAcceptState();

}