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

        void setSymbol(TigerNT symbol) {
            this.symbol = symbol;
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
                    curr.expectedNumCh -= (childNT.children.size() - 1);

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

    public void revertLeftRecursion() {
        revertLeftRecursionTraversal(root);
    }


    private void revertLeftRecursionTraversal(NTNode curr) {
        Node child;
        for (int i = 0; i < curr.children.size(); i++) {
            child = curr.children.get(i);
            if (child instanceof NTNode) {
                NTNode childNT = (NTNode) child;
                revertLeftRecursion(childNT, i);
                revertLeftRecursionTraversal(childNT);
            }
        }
    }

    private void revertLeftRecursion(NTNode curr, int currIndex) {
        Node lastChild = curr.children.get(curr.children.size() - 1);

        // parent of the left recursive node. Used to reattach its child
        NTNode recursiveParent = curr.parent;
        // the left recursive version of a non-terminal.
        // Example: use "expr" to set "expr'" nodes to "expr"
        TigerNT nonRecursiveSymbol = curr.symbol;

        while (lastChild instanceof NTNode && ((NTNode) lastChild).symbol.getType() == NTType.LR) {
            NTNode lastChildNT = (NTNode) lastChild;

            // Restore left recursion of current node
            curr.children.remove(curr.children.size() - 1);
            curr.setSymbol(nonRecursiveSymbol);
            curr.expectedNumCh--;

            if (lastChildNT.children.size() == 1) {
                Node epsilonChild = lastChildNT.children.get(0);
                if (epsilonChild instanceof TNode && ((TNode) epsilonChild).symbol.getType() == TokenType.EPSILON) {
                    // Set type to left recursive
                    recursiveParent.children.set(currIndex, curr);

                    // traverse the children since there might be nested left recursion to revert
                    for (int i = 0; i < curr.children.size(); i++) {
                        if (curr.children.get(i) instanceof NTNode) {
                            NTNode childNT = (NTNode) curr.children.get(i);
                            revertLeftRecursion(childNT, i);
                            revertLeftRecursionTraversal(childNT);
                        }
                    }

                    return;
                }
            }

            // add the current node to the beginning of production
            lastChildNT.children.add(0, curr);
            curr = lastChildNT;

            lastChild = curr.children.get(curr.children.size() - 1);
        }
    }


    @Override
    public String toString() {
        return root.toString();
    }
}