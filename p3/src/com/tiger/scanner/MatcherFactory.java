package com.tiger.scanner;

public class MatcherFactory {
    private static TokenMatcher identifier = null;

    private static TokenMatcher matchKeyword(TokenType type) {
        return new KeywordMatcher(type);
    }

    private static TokenMatcher matchIdentifier() {
        if (identifier != null) {
            return identifier;
        }
        DFATransition lowerCaseFromS0 = new DFATransition('a', 'z', 1);
        DFATransition upperCaseFromS0 = new DFATransition('A', 'Z', 1);
        DFATransition underscoreFromS0 = new DFATransition('_', 2);

        DFATransition lowerCaseFromS1 = new DFATransition('a', 'z', 1);
        DFATransition upperCaseFromS1 = new DFATransition('A', 'Z', 1);
        DFATransition digitFromS1 = new DFATransition('0', '9', 1);
        DFATransition underscoreFromS1 = new DFATransition('_', 1);

        DFATransition lowerCaseFromS2 = new DFATransition('a', 'z', 1);
        DFATransition upperCaseFromS2 = new DFATransition('A', 'Z', 1);
        DFATransition digitFromS2 = new DFATransition('0', '9', 1);
        DFATransition underscoreFromS2 = new DFATransition('_', 2);

        DFATransition[] fromState0 = {lowerCaseFromS0, upperCaseFromS0, underscoreFromS0};
        DFATransition[] fromState1 = {lowerCaseFromS1, upperCaseFromS1, digitFromS1, underscoreFromS1};
        DFATransition[] fromState2 = {lowerCaseFromS2, upperCaseFromS2, digitFromS2, underscoreFromS2};

        DFAMatcher matcher = new DFAMatcher(TokenType.IDENTIFIER);
        matcher.addState(fromState0, false);
        matcher.addState(fromState1, true);
        matcher.addState(fromState2, false);

        identifier = matcher;
        return identifier;
    }

    private static TokenMatcher matchIntLiteral() {
        DFATransition[] fromState0 = {new DFATransition('0', '9', 0)};
        DFAMatcher matcher = new DFAMatcher(TokenType.INTLIT);
        matcher.addState(fromState0, true);

        return matcher;
    }

    private static TokenMatcher matchFloatLiteral() {
        DFATransition oneToNineFromS0 = new DFATransition('1', '9', 1);
        DFATransition zeroFrom0 = new DFATransition('0', '0', 2);

        DFATransition digitsFromS1 = new DFATransition('0', '9', 1);
        DFATransition dotFromS1 = new DFATransition('.', '.', 3);

        DFATransition dotFromS2 = dotFromS1;

        DFATransition digitsFromS3 = new DFATransition('0', '9', 3);

        DFATransition[] fromState0 = {oneToNineFromS0, zeroFrom0};
        DFATransition[] fromState1 = {digitsFromS1, dotFromS1};
        DFATransition[] fromState2 = {dotFromS2};
        DFATransition[] fromState3 = {digitsFromS3};

        DFAMatcher matcher = new DFAMatcher(TokenType.FLOATLIT);
        matcher.addState(fromState0, false);
        matcher.addState(fromState1, false);
        matcher.addState(fromState2, false);
        matcher.addState(fromState3, true);

        return matcher;
    }

    private static TokenMatcher matchComment() {

        DFATransition slashFromS0 = new DFATransition('/', '/', 1);

        DFATransition starFromS1 = new DFATransition('*', '*', 2);

        DFATransition notStar1FromS2 = new DFATransition(Character.MIN_VALUE, (char)('*' - 1),  2);
        DFATransition notStar2FromS2 = new DFATransition((char)('*' + 1), Character.MAX_VALUE,  2);
        DFATransition starFromS2 = new DFATransition('*', '*', 3);

        DFATransition slashFromS3 = new DFATransition('/', '/', 4);
        DFATransition notSlash1FromS3 = new DFATransition(Character.MIN_VALUE, (char)('/' - 1),  2);
        DFATransition notSlash2FromS3 = new DFATransition((char)('/' + 1), Character.MAX_VALUE,  2);

        DFATransition[] fromState0 = {slashFromS0};
        DFATransition[] fromState1 = {starFromS1};
        DFATransition[] fromState2 = {notStar1FromS2, notStar2FromS2, starFromS2};
        DFATransition[] fromState3 = {slashFromS3, notSlash1FromS3, notSlash2FromS3};
        DFATransition[] fromState4 = {};

        DFAMatcher matcher = new DFAMatcher(TokenType.COMMENT);
        matcher.addState(fromState0, false);
        matcher.addState(fromState1, false);
        matcher.addState(fromState2, false);
        matcher.addState(fromState3, false);
        matcher.addState(fromState4, true);

        return matcher;
    }

    private static TokenMatcher matchNothing(TokenType tokenType) {
        return new TokenMatcher() {
            @Override
            public State feedChar(char input) {
                return State.ERROR;
            }

            @Override
            public void reset() {

            }

            @Override
            public TigerToken getToken() {
                return new TigerToken(tokenType);
            }
        };
    }

    public static TokenMatcher matchToken(TokenType type) {
        if (type.isKeyword()) {
            return matchKeyword(type);
        } else if (type == TokenType.IDENTIFIER){
            return matchIdentifier();
        } else if (type == TokenType.FLOATLIT) {
            return matchFloatLiteral();
        } else if (type == TokenType.INTLIT) {
            return matchIntLiteral();
        } else if (type == TokenType.COMMENT) {
            return matchComment();
        } else if (type == TokenType.EPSILON || type == TokenType.EOF) {
            return matchNothing(type);
        }
        return null;
    }


}
