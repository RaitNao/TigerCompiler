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
    private static Stack<TigerSymbol> stack = null;

    public static void parse(Reader reader, TigerAST AST) throws ParseException {
        TigerScanner sc = new TigerScanner(reader);
        Iterator<TigerToken[]> iter = sc.iterator();
        stack = new Stack<>();
        TigerToken[] token = iter.next();

        stack.push(LLTable.EOF);

        TigerSymbol top = LLTable.startSymbol;
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
                        throw new ParseException("Error Top of Stack Terminal not matching", -1);
                    }
                }
            } else if (top instanceof TigerNT){
                TigerProduction prod = LLTable.getProduction(((TigerNT) top), token[0]);
                if (prod != null) {
                    AST.addSymbol((TigerNT) top, prod.getSymbols().length);
                    for (int i = prod.getSymbols().length - 1; i >= 0; i--) {
                        stack.push(prod.getSymbols()[i]);
                    }
                    top = stack.pop();
                } else {
                    throw new ParseException("Error expanding Top of Stack Non-Terminal", -1);
                }
            }
        }
    }
}
