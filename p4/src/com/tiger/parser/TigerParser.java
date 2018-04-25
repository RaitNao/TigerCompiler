package com.tiger.parser;

import com.tiger.scanner.TigerScanner;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;
import com.tiger.syntax.TigerAST;
import com.tiger.syntax.TigerSymbol;

import java.io.Reader;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Stack;

public class TigerParser {

    public static void parse(Iterator<TigerToken[]> iter, TigerAST AST, TigerSymbol top) {
        Stack<TigerSymbol> stack = new Stack<>();
        TigerToken[] token = iter.next();

        stack.push(LLTable.EOF);
        try {
            while (iter.hasNext()) {
                if (top instanceof TigerToken && ((TigerToken) top).getType() == TokenType.EOF) {
                    break;
                } else if (top instanceof TigerToken) {
                    TigerToken topToken = (TigerToken) top;
                    if (token[0].getType() == topToken.getType()) {
                        AST.addSymbol(token[0]);

                        top = stack.pop();
                        token = iter.next();
                    } else {
                        // If Epsilon Node was on stack, just pop it + add to Syntax Tree
                        if (topToken.getType() == TokenType.EPSILON) {
                            AST.addSymbol(topToken);
                            top = stack.pop();
                        } else {
                            System.err.println("Unable to process token " + token[0].strValue());
                            System.exit(1);
                        }
                    }
                } else if (top instanceof TigerNT) {
                    TigerProduction prod = LLTable.getProduction(((TigerNT) top), token[0]);
                    if (prod != null) {
                        AST.addSymbol((TigerNT) top, prod.getSymbols().length);
                        for (int i = prod.getSymbols().length - 1; i >= 0; i--) {
                            stack.push(prod.getSymbols()[i]);
                        }
                        top = stack.pop();
                    } else {
                        System.err.println("Unable to process token " + token[0].strValue());
                        System.exit(1);
                    }
                }
            }
        } catch (ParseException e) {
            System.err.println("Unable to process token " + token[0].strValue());
            System.exit(1);
        }
    }

    public static void parse(Reader reader, TigerAST AST) {
        TigerScanner sc = new TigerScanner(reader);
        Iterator<TigerToken[]> iter = sc.iterator();

        TigerSymbol top = LLTable.startSymbol;
        parse(iter, AST, top);
    }
}
