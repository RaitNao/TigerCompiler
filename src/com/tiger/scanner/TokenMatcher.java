package com.tiger.scanner;

public interface TokenMatcher {
    State feedChar(char input);
    void reset();
}