package com.tiger.scanner;


import java.io.*;
import java.text.ParseException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TigerScanner implements Iterable<TigerToken[]> {

    private Reader reader;
    private TigerToken lastToken;
    private TokenMatcher[] matchers = new TokenMatcher[TokenType.values().length];
    private int lastCh = ' '; // ugly hack to read first character
    private String errorMsg = null;

    private Iterator<TigerToken[]> iterator = new Iterator<TigerToken[]>() {
        private TigerToken[] tokens;

        @Override
        public boolean hasNext() {
            if (tokens != null && errorMsg == null && tokens[1] == null) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public TigerToken[] next() {
            try {
                if (!hasNext()) {
                    throw new NoSuchElementException("Reached EOF");
                }

                if (errorMsg != null) {
                    throw new NoSuchElementException(errorMsg);
                }

                if (lastToken == null) {
                    lastToken = nextToken();
                }

                tokens = new TigerToken[2];
                tokens[0] = lastToken;

                if (lastCh != -1) {
                    try {
                        lastToken = nextToken();
                    } catch (ParseException e) {
                        errorMsg = e.getMessage();
                        lastToken = null;
                    }
                    tokens[1] = lastToken;
                }

                return tokens;
            } catch (ParseException e) {
                throw new NoSuchElementException(e.getMessage());
            }
        }
    };

    public TigerScanner(Reader reader) {
        this.reader = reader;

        for (int i = 0; i < matchers.length; i++) {
            matchers[i] = MatcherFactory.matchToken(TokenType.values()[i]);
        }
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
