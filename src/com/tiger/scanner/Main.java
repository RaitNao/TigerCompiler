package com.tiger.scanner;

public class Main {

    public static void main(String[] args) {
	// write your code here
        String s = "/* huy /*v plecho */ */ asd  *";
        CommentMatcher match = new CommentMatcher();
        for (int i = 0; i < s.length(); i++) {
            //System.out.println(s.charAt(i) + ": " + match.feedChar(s.charAt(i)));
        }

        String keyword = "function";
        KeywordMatcher m = new KeywordMatcher(keyword);
        for (char el: (keyword + "123").toCharArray()) {
            System.out.println(el + ": " + m.feedChar(el));
        }
    }

}
