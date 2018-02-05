package com.tiger.scanner;

public class MatcherFactory {
    private static TokenMatcher identifier = null;

    private static TokenMatcher matchKeyword(String keyword) {
        return new KeywordMatcher(keyword);
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

        DFATransition mFromS2 = new DFATransition('m', 3);
        DFATransition aFromS3 = new DFATransition('a', 4);
        DFATransition iFromS4 = new DFATransition('i', 5);
        DFATransition nFromS5 = new DFATransition('n', 6);

        DFATransition[] fromState0 = {lowerCaseFromS0, upperCaseFromS0, underscoreFromS0};
        DFATransition[] fromState1 = {lowerCaseFromS1, upperCaseFromS1, digitFromS1, underscoreFromS1};
        DFATransition[] fromState2 = {mFromS2};
        DFATransition[] fromState3 = {aFromS3};
        DFATransition[] fromState4 = {iFromS4};
        DFATransition[] fromState5 = {nFromS5};
        DFATransition[] fromState6 = {};

        DFAMatcher matcher = new DFAMatcher();
        matcher.addState(fromState0, false);
        matcher.addState(fromState1, true);
        matcher.addState(fromState2, false);
        matcher.addState(fromState3, false);
        matcher.addState(fromState4, false);
        matcher.addState(fromState5, false);
        matcher.addState(fromState6, true);

        identifier = matcher;
        return identifier;
    }

    private static TokenMatcher matchStringLiteral() {
        return null;
    }

    private static TokenMatcher matchIntLiteral() {
        DFATransition[] fromState0 = {new DFATransition('0', '9', 1)};
        DFAMatcher matcher = new DFAMatcher();
        matcher.addState(fromState0, true);

        return matcher;
    }

    private static TokenMatcher matchComment() {
        return new CommentMatcher();
    }

    public static TokenMatcher matchToken(TokenType type) {
        if (type.isKeyword()) {
            return matchKeyword(type.getKeywordText());
        } else if (type == TokenType.IDENTIFIER){
            return matchIdentifier();
        } else if (type == TokenType.STRINGLIT) {
            return matchStringLiteral();
        } else if (type == TokenType.INTLIT) {
            return matchIntLiteral();
        } else if (type == TokenType.COMMENT) {
            return matchComment();
        }
        return null;
    }
}
