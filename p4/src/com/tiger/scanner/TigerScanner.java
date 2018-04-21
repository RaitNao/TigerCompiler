package com.tiger.scanner;


import java.io.*;
import java.text.ParseException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TigerScanner implements Iterable<TigerToken[]> {

    private Reader reader;
    private TigerToken[] lastTokens;
    private TokenMatcher[] matchers = new TokenMatcher[TokenType.values().length];
    private int lastCh = ' '; // ugly hack to read first character
    private int k = 1;

    private Iterator<TigerToken[]> iterator = new Iterator<TigerToken[]>() {
        @Override
        public boolean hasNext() {
            return lastTokens == null || lastTokens[0].getType() != TokenType.EOF;
        }

        @Override
        public TigerToken[] next() {
            try {
                if (!hasNext()) {
                    throw new NoSuchElementException("Reached EOF");
                }

                if (lastTokens == null) {
                    fillLastTokens();
                }

                if (lastCh != -1) {
                    try {
                        pushToLastTokens();
                    } catch (ParseException e) {
                        pushEOF();
                    }

                    if (lastTokens[lastTokens.length - 1] == null) {
                        pushEOF();
                    }
                } else {
                    pushEOF();
                }

                return lastTokens.clone();
            } catch (ParseException e) {
                throw new NoSuchElementException(e.getMessage());
            }
        }
    };

    private void pushEOF() {
        lastTokens[lastTokens.length - 1] = new TigerToken(TokenType.EOF);
    }

    private void fillLastTokens() throws ParseException {
        this.lastTokens = new TigerToken[k];

        for (int i = 1; i < lastTokens.length; i++){
            lastTokens[i] = nextToken();
        }
    }

    private void pushToLastTokens() throws ParseException {
        int i;
        for (i = 0; i < lastTokens.length - 1; i++){
            lastTokens[i] = lastTokens[i + 1];
        }

        lastTokens[i] = nextToken();
    }

    public TigerScanner(Reader reader, int k) {
        this.reader = reader;
        this.k = k;

        for (int i = 0; i < matchers.length; i++) {
            matchers[i] = MatcherFactory.matchToken(TokenType.values()[i]);
        }
    }

    public TigerScanner(Reader reader) {
        this(reader, 1);
    }

    @Override
    public Iterator<TigerToken[]> iterator() {
        return iterator;
    }

    private TigerToken nextToken() throws ParseException {
        try {
            TigerToken longestToken = null;
            boolean allError = true;
            boolean normalAfterAccept = false;

            while (true) {
                boolean whitespace = false;
                while ((lastCh == ' ' || lastCh == '\r' || lastCh == '\n' || lastCh == '\t') && allError) {
                    lastCh = reader.read();
                    whitespace = true;
                    normalAfterAccept = false;
                }

                if (!allError && !whitespace) {
                    lastCh = reader.read();
                } else {
                    normalAfterAccept = false;
                }

                allError = true;

                if (lastCh != -1) {
                    char ch = (char) lastCh;
                    boolean currRoundAccepting = false;
                    for (int i = matchers.length - 1; i >= 0; i--) {
                        State currState = matchers[i].feedChar(ch);
                        if (currState == State.ACCEPTING) {
                            normalAfterAccept = false;
                            currRoundAccepting = true;
                            longestToken = matchers[i].getToken();
                        }

                        if (currState == State.NORMAL && !currRoundAccepting) {
                            normalAfterAccept = true;
                        }

                        if (currState != State.ERROR) {
                            allError = false;
                        }
                    }

                } else {
                    allError = true;
                }

                if (allError) {
                    if (longestToken == null) {
                        if (lastCh == -1) {
                            return null;
                        }
                        throw new ParseException("Read error", -1);
                    } else if (normalAfterAccept) {
                        throw new ParseException("Unexpected token", -1);
                    }

                    for (TokenMatcher matcher : matchers) {
                        matcher.reset();
                    }

                    if (longestToken.getType() == TokenType.COMMENT) {
                        return nextToken();
                    }
                    return longestToken;
                }

            }

        } catch (java.io.IOException e) {
            throw new ParseException("Read error", -1);
        }
    }
}
