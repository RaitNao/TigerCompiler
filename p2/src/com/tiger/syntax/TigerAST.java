package com.tiger.syntax;

import com.tiger.parser.NTType;
import com.tiger.parser.TigerNT;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TigerAST {
    private enum NodeType {
        NT_NODE, T_NODE
    }

    private class Node {
        Node parent;
        List<Node> children;
        TigerSymbol symbol;
        NodeType type;
        int expectedNumCh;

        Node(Node parent, TigerNT symbol, int expectedNumCh) {
            this.parent = parent;
            this.symbol = symbol;
            this.expectedNumCh = expectedNumCh;
            type = NodeType.NT_NODE;
            children = new ArrayList<>();
        }

        Node(Node parent, TigerToken token) {
            this.parent = parent;
            this.symbol = token;
            this.expectedNumCh = 0;
            type = NodeType.T_NODE;
            children = new ArrayList<>();
        }

        void setSymbol(TigerSymbol symbol) {
            this.symbol = symbol;
        }

        TokenType getTerminalType() {
            return ((TigerToken) this.symbol).getType();
        }

        Node childAt(int i) {
            return children.get(i);
        }

        boolean isLR() {
            if (!(this.symbol instanceof TigerNT) || this.type != NodeType.NT_NODE) {
                return false;
            }
            return ((TigerNT) this.symbol).getType() == NTType.LR;
        }

        boolean isLF() {
            if (!(this.symbol instanceof TigerNT) || this.type != NodeType.NT_NODE) {
                return false;
            }
            return ((TigerNT) this.symbol).getType() == NTType.LF;
        }

        boolean isNT() {
            return this.type == NodeType.NT_NODE;
        }

        @Override
        public String toString() {

            if (this.type == NodeType.T_NODE) {
                return this.symbol.strValue();
            } else {

                if (children.size() == 1
                        && !childAt(0).isNT()
                        && childAt(0).getTerminalType() == TokenType.EPSILON) {
                    return symbol.strValue();
                }

                StringBuilder builder = new StringBuilder("(" + symbol.strValue());

                for (Node child : children) {
                    String chStr = child.toString();

                    if (chStr != null) {
                        builder.append(" ").append(chStr);
                    }
                }
                builder.append(")");

                return builder.toString();
            }
        }
    }

    /* ################################################################################################################### */

    private Node root = null;
    private Node leftMost = null;

    public void addSymbol(TigerToken token) throws ParseException {
        if (root == null) {
            throw new ParseException("Adding terminal when Syntax Tree is empty", 0);
        }

        updateLeftMostNT();
        leftMost.children.add(new Node(leftMost, token));
    }

    public void addSymbol(TigerNT NT, int expectedNumCh) throws ParseException {
        if (root == null) {
            root = new Node(null, NT, expectedNumCh);
            leftMost = root;
        } else {
            updateLeftMostNT();
            Node newNTNode= new Node(leftMost, NT, expectedNumCh);
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

    private boolean revertLeftFactoring(Node curr) {
        Node child;
        for (int i = 0; i < curr.children.size(); i++) {
            child = curr.childAt(i);
            if (child.isNT()) {
                if (revertLeftFactoring(child)) {
                    child.children.forEach(grandchild -> grandchild.parent = curr);
                    curr.children.remove(i);
                    if (child.childAt(0).isNT() || child.childAt(0).getTerminalType() != TokenType.EPSILON) {
                        curr.children.addAll(i, child.children);
                    } else {
                        curr.expectedNumCh--;
                    }
                    curr.children.addAll(i, child.children);
                    curr.expectedNumCh += (child.children.size() - 1);

                    // Make sure for loop goes through first "new" child
                    i--;
                }
            }
        }

        if (curr.isLF()) {
            return curr.parent.symbol.strValue().equals(curr.symbol.strValue());
        }
        return false;
    }

    public void revertLeftRecursion() {
        revertLeftRecursionTraversal(root);
    }


    private void revertLeftRecursionTraversal(Node curr) {
        Node child;
        for (int i = 0; i < curr.children.size(); i++) {
            child = curr.childAt(i);
            if (child.isNT()) {
                revertLeftRecursion(child, i);
                revertLeftRecursionTraversal(child);
            }
        }
    }

    private void revertLeftRecursion(Node curr, int currIndex) {
        Node lastChild = curr.childAt(curr.children.size() - 1);

        // parent of the left recursive node. Used to reattach its child
        Node recursiveParent = curr.parent;
        // the left recursive version of a non-terminal.
        // Example: use "expr" to set "expr'" nodes to "expr"
        TigerNT nonRecursiveSymbol = (TigerNT) curr.symbol;

        while (lastChild.isLR()) {

            // Restore left recursion of current node
            curr.children.remove(curr.children.size() - 1);
            curr.setSymbol(nonRecursiveSymbol);
            curr.expectedNumCh--;

            if (lastChild.children.size() == 1) {
                Node epsilonChild = lastChild.childAt(0);
                if (!epsilonChild.isNT() && epsilonChild.getTerminalType() == TokenType.EPSILON) {
                    // Set type to left recursive
                    recursiveParent.children.set(currIndex, curr);

                    // traverse the children since there might be nested left recursion to revert
                    for (int i = 0; i < curr.children.size(); i++) {
                        if (curr.childAt(i).isNT()) {
                            Node childNT = curr.childAt(i);
                            revertLeftRecursion(childNT, i);
                            revertLeftRecursionTraversal(childNT);
                        }
                    }

                    return;
                }
            }

            // add the current node to the beginning of production
            lastChild.children.add(0, curr);
            curr = lastChild;

            lastChild = curr.childAt(curr.children.size() - 1);
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}