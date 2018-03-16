package com.tiger.parser;

import java.util.Map;
import java.util.HashMap;
import com.tiger.scanner.TokenType;

public class LLTable {
    public static final Map<String, Map<TokenType, Integer>> map = createMap();
    private static Map<String, Map<TokenType, Integer>> createMap() {
        Map<String, Map<TokenType, Integer>> map = new HashMap<>();
		map.put("fullstmt", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.BREAK, 1);
				put(TokenType.WHILE, 1);
				put(TokenType.RETURN, 1);
				put(TokenType.FOR, 1);
				put(TokenType.IDENTIFIER, 1);
				put(TokenType.IF, 1);
			}
		});
		map.put("stmts_lf", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.ENDDO, 2);
				put(TokenType.ELSE, 2);
				put(TokenType.END, 2);
				put(TokenType.ENDIF, 2);
				put(TokenType.BREAK, 3);
				put(TokenType.WHILE, 3);
				put(TokenType.RETURN, 3);
				put(TokenType.FOR, 3);
				put(TokenType.IDENTIFIER, 3);
				put(TokenType.IF, 3);
			}
		});
		map.put("typedecl", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.BOOLEAN, 4);
				put(TokenType.INT, 4);
				put(TokenType.ARRAY, 4);
				put(TokenType.FLOAT, 4);
				put(TokenType.IDENTIFIER, 4);
				put(TokenType.UNIT, 4);
			}
		});
		map.put("factor", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.INTLIT, 46);
				put(TokenType.TRUE, 46);
				put(TokenType.FALSE, 46);
				put(TokenType.FLOATLIT, 46);
				put(TokenType.UNDERSCORE, 46);
				put(TokenType.LPAREN, 47);
				put(TokenType.IDENTIFIER, 48);
			}
		});
		map.put("neparams", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.IDENTIFIER, 5);
			}
		});
		map.put("linop", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.PLUS, 6);
				put(TokenType.MINUS, 7);
			}
		});
		map.put("pred_lf", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.DO, 8);
				put(TokenType.AND, 8);
				put(TokenType.RPAREN, 8);
				put(TokenType.COMMA, 8);
				put(TokenType.TO, 8);
				put(TokenType.THEN, 8);
				put(TokenType.SEMICOLON, 8);
				put(TokenType.RSQBR, 8);
				put(TokenType.OR, 8);
				put(TokenType.MEQ, 9);
				put(TokenType.NOTEQUAL, 9);
				put(TokenType.LEQ, 9);
				put(TokenType.EQUAL, 9);
				put(TokenType.LESS, 9);
				put(TokenType.MORE, 9);
			}
		});
		map.put("term'", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.STAR, 10);
				put(TokenType.FWSLASH, 10);
				put(TokenType.DO, 11);
				put(TokenType.MEQ, 11);
				put(TokenType.THEN, 11);
				put(TokenType.NOTEQUAL, 11);
				put(TokenType.LEQ, 11);
				put(TokenType.RSQBR, 11);
				put(TokenType.AND, 11);
				put(TokenType.RPAREN, 11);
				put(TokenType.PLUS, 11);
				put(TokenType.MINUS, 11);
				put(TokenType.COMMA, 11);
				put(TokenType.LESS, 11);
				put(TokenType.TO, 11);
				put(TokenType.SEMICOLON, 11);
				put(TokenType.EQUAL, 11);
				put(TokenType.OR, 11);
				put(TokenType.MORE, 11);
			}
		});
		map.put("neexprs", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FALSE, 12);
				put(TokenType.INTLIT, 12);
				put(TokenType.LPAREN, 12);
				put(TokenType.TRUE, 12);
				put(TokenType.FLOATLIT, 12);
				put(TokenType.UNDERSCORE, 12);
				put(TokenType.IDENTIFIER, 12);
			}
		});
		map.put("ids_lf", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.COLON, 13);
				put(TokenType.COMMA, 14);
			}
		});
		map.put("aexpr", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FALSE, 18);
				put(TokenType.INTLIT, 18);
				put(TokenType.LPAREN, 18);
				put(TokenType.TRUE, 18);
				put(TokenType.FLOATLIT, 18);
				put(TokenType.UNDERSCORE, 18);
				put(TokenType.IDENTIFIER, 18);
			}
		});
		map.put("const", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.TRUE, 19);
				put(TokenType.FALSE, 20);
				put(TokenType.UNDERSCORE, 21);
				put(TokenType.INTLIT, 22);
				put(TokenType.FLOATLIT, 23);
			}
		});
		map.put("expr'", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.OR, 24);
				put(TokenType.DO, 25);
				put(TokenType.THEN, 25);
				put(TokenType.RPAREN, 25);
				put(TokenType.COMMA, 25);
				put(TokenType.TO, 25);
				put(TokenType.RSQBR, 25);
				put(TokenType.SEMICOLON, 25);
			}
		});
		map.put("stmts", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.BREAK, 26);
				put(TokenType.WHILE, 26);
				put(TokenType.RETURN, 26);
				put(TokenType.FOR, 26);
				put(TokenType.IDENTIFIER, 26);
				put(TokenType.IF, 26);
			}
		});
		map.put("lvalue", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.IDENTIFIER, 27);
			}
		});
		map.put("vardecl", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.VAR, 28);
			}
		});
		map.put("param", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.IDENTIFIER, 29);
			}
		});
		map.put("program", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.LET, 30);
			}
		});
		map.put("params", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.RPAREN, 31);
				put(TokenType.IDENTIFIER, 32);
			}
		});
		map.put("clause", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FALSE, 33);
				put(TokenType.INTLIT, 33);
				put(TokenType.LPAREN, 33);
				put(TokenType.TRUE, 33);
				put(TokenType.FLOATLIT, 33);
				put(TokenType.UNDERSCORE, 33);
				put(TokenType.IDENTIFIER, 33);
			}
		});
		map.put("optinit", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.SEMICOLON, 34);
				put(TokenType.ASSIGN, 35);
			}
		});
		map.put("type", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.BOOLEAN, 36);
				put(TokenType.INT, 37);
				put(TokenType.FLOAT, 38);
				put(TokenType.UNIT, 39);
				put(TokenType.IDENTIFIER, 40);
				put(TokenType.ARRAY, 41);
			}
		});
		map.put("nonlinop", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.STAR, 42);
				put(TokenType.FWSLASH, 43);
			}
		});
		map.put("neexprs_lf", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.RPAREN, 44);
				put(TokenType.COMMA, 45);
			}
		});
		map.put("factor_lf", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.DO, 15);
				put(TokenType.MEQ, 15);
				put(TokenType.THEN, 15);
				put(TokenType.NOTEQUAL, 15);
				put(TokenType.LEQ, 15);
				put(TokenType.RSQBR, 15);
				put(TokenType.AND, 15);
				put(TokenType.RPAREN, 15);
				put(TokenType.PLUS, 15);
				put(TokenType.STAR, 15);
				put(TokenType.MINUS, 15);
				put(TokenType.COMMA, 15);
				put(TokenType.FWSLASH, 15);
				put(TokenType.OR, 15);
				put(TokenType.TO, 15);
				put(TokenType.SEMICOLON, 15);
				put(TokenType.EQUAL, 15);
				put(TokenType.LESS, 15);
				put(TokenType.MORE, 15);
				put(TokenType.LSQBR, 16);
				put(TokenType.LPAREN, 17);
			}
		});
		map.put("neparams_lf", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.RPAREN, 49);
				put(TokenType.COMMA, 50);
			}
		});
		map.put("stmt", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.IDENTIFIER, 51);
				put(TokenType.WHILE, 52);
				put(TokenType.FOR, 53);
				put(TokenType.BREAK, 54);
				put(TokenType.RETURN, 55);
				put(TokenType.IF, 56);
			}
		});
		map.put("clause'", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.AND, 57);
				put(TokenType.DO, 58);
				put(TokenType.THEN, 58);
				put(TokenType.RPAREN, 58);
				put(TokenType.COMMA, 58);
				put(TokenType.TO, 58);
				put(TokenType.SEMICOLON, 58);
				put(TokenType.RSQBR, 58);
				put(TokenType.OR, 58);
			}
		});
		map.put("stmt_lf", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.ENDIF, 59);
				put(TokenType.ELSE, 60);
			}
		});
		map.put("funcdecls", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.IN, 61);
				put(TokenType.FUNC, 62);
			}
		});
		map.put("vardecls", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FUNC, 63);
				put(TokenType.IN, 63);
				put(TokenType.VAR, 64);
			}
		});
		map.put("typedecls", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.VAR, 65);
				put(TokenType.FUNC, 65);
				put(TokenType.IN, 65);
				put(TokenType.BOOLEAN, 66);
				put(TokenType.INT, 66);
				put(TokenType.ARRAY, 66);
				put(TokenType.FLOAT, 66);
				put(TokenType.IDENTIFIER, 66);
				put(TokenType.UNIT, 66);
			}
		});
		map.put("term", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FALSE, 67);
				put(TokenType.INTLIT, 67);
				put(TokenType.LPAREN, 67);
				put(TokenType.TRUE, 67);
				put(TokenType.FLOATLIT, 67);
				put(TokenType.UNDERSCORE, 67);
				put(TokenType.IDENTIFIER, 67);
			}
		});
		map.put("declseg", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.BOOLEAN, 69);
				put(TokenType.INT, 69);
				put(TokenType.FLOAT, 69);
				put(TokenType.ARRAY, 69);
				put(TokenType.IDENTIFIER, 69);
				put(TokenType.UNIT, 69);
			}
		});
		map.put("aexpr'", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.PLUS, 70);
				put(TokenType.MINUS, 70);
				put(TokenType.DO, 71);
				put(TokenType.MEQ, 71);
				put(TokenType.THEN, 71);
				put(TokenType.NOTEQUAL, 71);
				put(TokenType.LEQ, 71);
				put(TokenType.RSQBR, 71);
				put(TokenType.AND, 71);
				put(TokenType.RPAREN, 71);
				put(TokenType.COMMA, 71);
				put(TokenType.LESS, 71);
				put(TokenType.TO, 71);
				put(TokenType.SEMICOLON, 71);
				put(TokenType.EQUAL, 71);
				put(TokenType.OR, 71);
				put(TokenType.MORE, 71);
			}
		});
		map.put("optoffset", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.ASSIGN, 72);
				put(TokenType.LSQBR, 73);
			}
		});
		map.put("ids", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.IDENTIFIER, 74);
			}
		});
		map.put("expr", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FALSE, 77);
				put(TokenType.INTLIT, 77);
				put(TokenType.LPAREN, 77);
				put(TokenType.TRUE, 77);
				put(TokenType.FLOATLIT, 77);
				put(TokenType.UNDERSCORE, 77);
				put(TokenType.IDENTIFIER, 77);
			}
		});
		map.put("exprs", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.RPAREN, 75);
				put(TokenType.FALSE, 76);
				put(TokenType.INTLIT, 76);
				put(TokenType.LPAREN, 76);
				put(TokenType.TRUE, 76);
				put(TokenType.FLOATLIT, 76);
				put(TokenType.UNDERSCORE, 76);
				put(TokenType.IDENTIFIER, 76);
			}
		});
		map.put("pred", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FALSE, 68);
				put(TokenType.INTLIT, 68);
				put(TokenType.LPAREN, 68);
				put(TokenType.TRUE, 68);
				put(TokenType.FLOATLIT, 68);
				put(TokenType.UNDERSCORE, 68);
				put(TokenType.IDENTIFIER, 68);
			}
		});
		map.put("funcdecl", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.FUNC, 78);
			}
		});
		map.put("cmp", new HashMap<TokenType, Integer>() {
			{
				put(TokenType.EQUAL, 79);
				put(TokenType.NOTEQUAL, 80);
				put(TokenType.LEQ, 81);
				put(TokenType.MEQ, 82);
				put(TokenType.LESS, 83);
				put(TokenType.MORE, 84);
			}
		});
		return map;
	}
}
