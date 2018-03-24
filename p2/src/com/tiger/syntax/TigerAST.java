package com.tiger.syntax;

import com.tiger.parser.LLTable;
import com.tiger.parser.NTType;
import com.tiger.parser.TigerNT;
import com.tiger.parser.TigerProduction;
import com.tiger.scanner.TigerScanner;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;

import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class TigerAST {
    private abstract class Node {
        NTNode parent;
    }

    private class TNode extends Node {
        TigerToken symbol;

        TNode(NTNode parent, TigerToken symbol) {
            this.parent = parent;
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol.strValue();
        }
    }

    private class NTNode extends Node {
        List<Node> children;
        TigerNT symbol;
        int expectedNumCh;

        public NTNode(NTNode parent, TigerNT symbol, int expectedNumCh) {
            this.parent = parent;
            this.symbol = symbol;
            this.expectedNumCh = expectedNumCh;
            children = new ArrayList<>();
        }

        @Override
        public String toString() {
            if (children.size() == 1 && children.get(0) instanceof TNode && ((TNode) children.get(0)).symbol.getType() == TokenType.EPSILON) {
                return symbol.strValue() + getSpecialType();
            }

            StringBuilder builder = new StringBuilder("(" + symbol.strValue());
            builder.append(getSpecialType());

            for (Node child : children) {
                if (child.toString() != null) {
                    builder.append(" " + child.toString());
                }
            }
            builder.append(")");

            return builder.toString();
        }

        private String getSpecialType() {
            if (this instanceof NTNode) {
                NTType type = ((NTNode) this).symbol.getType();
                if (type == NTType.LF) {
                    return "_lf";
                } else if (type == NTType.LR) {
                    return "'";
                }
            }
            return "";
        }
    }

    /* ################################################################################################################### */

    private NTNode root = null;
    private NTNode leftMost = null;

    public void addSymbol(TigerToken token) throws ParseException {
        if (root == null) {
            throw new ParseException("Adding terminal when Syntax Tree is empty", 0);
        }

        updateLeftMostNT();
        leftMost.children.add(new TNode(leftMost, token));
    }

    public void addSymbol(TigerNT NT, int expectedNumCh) throws ParseException {
        if (root == null) {
            root = new NTNode(null, NT, expectedNumCh);
            leftMost = root;
        } else {
            updateLeftMostNT();
            NTNode newNTNode= new NTNode(leftMost, NT, expectedNumCh);
            leftMost.children.add(newNTNode);

            // After adding need to expand new non-terminal
            leftMost = newNTNode;
        }
    }


    private void updateLeftMostNT() throws ParseException {
        while (leftMost.children.size() == leftMost.expectedNumCh) {
            if (leftMost == root) {
                throw new ParseException("Syntax Tree full", -1);
            }
            leftMost = leftMost.parent;
        }
    }

    /* ################################################################################################################### */

    private Stack<TigerSymbol> stack = null;

    public String parse(Reader reader) throws ParseException {
        TigerScanner sc = new TigerScanner(reader);
        Iterator<TigerToken[]> iter = sc.iterator();
        stack = new Stack<>();
        TigerToken[] token = iter.next();

        stack.push(LLTable.EOF);
        stack.push(LLTable.startSymbol);

        TigerSymbol top = stack.pop();
        while (iter.hasNext()) {
            if (top instanceof TigerToken && ((TigerToken) top).getType() == TokenType.EOF) {
                break;
            } else if (top instanceof TigerToken) {
                if (token[0].equals(top)) {
                    addSymbol((TigerToken) top);

                    top = stack.pop();
                    token = iter.next();
                } else {
                    // Case for Tokens that are IDs, INTLITS, FLOATLITS
                    // Because the Top Of Stack doesn't have actual value of those
                    TokenType topType = ((TigerToken) top).getType();
                    if (topType == token[0].getType() && topType == TokenType.IDENTIFIER || topType == TokenType.INTLIT || topType == TokenType.FLOATLIT) {
                        addSymbol(token[0]);

                        top = stack.pop();
                        token = iter.next();
                    } else if (topType == TokenType.EPSILON) {
                        // If Epsilon Node was on stack, just pop it + add to Syntax Tree
                        addSymbol((TigerToken) top);
                        top = stack.pop();
                    } else {
                        throw new ParseException("Error Top of Stack Terminal not matching", -1);
                    }
                }
            } else if (top instanceof TigerNT){
                TigerProduction prod = LLTable.getProduction(((TigerNT) top), token[0]);
                if (prod != null) {
                    addSymbol((TigerNT) top, prod.getSymbols().length);
                    for (int i = prod.getSymbols().length - 1; i >= 0; i--) {
                        TigerSymbol symbol = prod.getSymbols()[i];
                        stack.push(prod.getSymbols()[i]);
                    }
                    top = stack.pop();
                } else {
                    throw new ParseException("Error expanding Top of Stack Non-Terminal", -1);
                }
            }
        }
        return root.toString();
    }

}