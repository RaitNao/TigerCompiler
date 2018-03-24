package com.tiger.syntax;

import com.tiger.parser.NTType;
import com.tiger.parser.TigerNT;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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

        NTNode(NTNode parent, TigerNT symbol, int expectedNumCh) {
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
                    builder.append(" ").append(child.toString());
                }
            }
            builder.append(")");

            return builder.toString();
        }

        private String getSpecialType() {
            NTType type = symbol.getType();
            if (type == NTType.LF) {
                return "_lf";
            } else if (type == NTType.LR) {
                return "'";
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

    public void revertLeftFactoring() {
        revertLeftFactoring(root);
    }

    private boolean revertLeftFactoring(NTNode curr) {
        Node child;
        for (int i = 0; i < curr.children.size(); i++) {
            child = curr.children.get(i);
            if (child instanceof NTNode) {
                NTNode childNT = (NTNode) child;
                if (revertLeftFactoring(childNT)) {
                    childNT.children.forEach(grandchild -> grandchild.parent = curr);
                    curr.children.remove(i);
                    curr.children.addAll(i, childNT.children);
                    curr.expectedNumCh = curr.children.size();

                    // Make sure for loop goes through first "new" child
                    i--;
                }
            }
        }

        if (curr.symbol.getType() == NTType.LF) {
            if (curr.parent.symbol.strValue().equals(curr.symbol.strValue())) {
                return true;
            }
        }
        return false;
    }

    /* ################################################################################################################### */

    @Override
    public String toString() {
        return root.toString();
    }
}