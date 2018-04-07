package com.tiger.syntax;

import com.tiger.parser.NTType;
import com.tiger.parser.TigerNT;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;
import com.tiger.type.SymbolTable;
import com.tiger.type.TigerType;
import com.tiger.type.TigerTypeType;

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
                    curr.children.addAll(i, child.children);
                    curr.expectedNumCh -= (child.children.size() - 1);

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

    /* ################################################################################################################### */

    public boolean isWellTyped() {
        SymbolTable typeContext = new SymbolTable();
        SymbolTable typeAliasMap = new SymbolTable();

        typeAliasMap.push("int", new TigerType(TigerTypeType.INT));
        typeAliasMap.push("float", new TigerType(TigerTypeType.FLOAT));
        typeAliasMap.push("unit", new TigerType(TigerTypeType.UNIT));
        typeAliasMap.push("boolean", new TigerType(TigerTypeType.BOOLEAN));

        Node declseg = root.childAt(1);

        Node typedecls = declseg.childAt(0);
        Node vardecls = declseg.childAt(1);
        Node funcdecls = declseg.childAt(2);

        int countPushed = 0;

        while (typedecls.childAt(0).isNT()) {
            Node currNode = typedecls.childAt(0);
            String typeName = currNode.childAt(1).symbol.strValue();

            Node typeDef = currNode.childAt(3);
            typeAliasMap.push(typeName, typeExprGetType(typeDef, typeAliasMap));
            countPushed++;

            typedecls = typedecls.childAt(1);
        }

        while (vardecls.childAt(0).isNT()) {
            Node currNode = typedecls.childAt(0);
            Node typeDef = currNode.childAt(3);
            Node optInit = currNode.childAt(4);

            TigerType type = typeExprGetType(typeDef, typeAliasMap);
            if (optInit.children.size() > 1) {
                TigerType optInitType = getTypeConst(optInit.childAt(1));
                if (!optInitType.equals(type)) {
                    return false;
                }
            }
            Node ids = currNode.childAt(1);

            while (ids.childAt(1).isNT()) {
                typeContext.push(ids.childAt(0).symbol.strValue(), type);
                countPushed++;
                ids = ids.childAt(1);
            }
            typeContext.push(ids.childAt(0).symbol.strValue(), type);
            countPushed++;
        }

        while (funcdecls.childAt(0).isNT()) {
            int paramsCounter = 0;
            Node currNode = typedecls.childAt(0);
            String typeName = currNode.childAt(1).symbol.strValue();
            Node returnType = currNode.childAt(3);

            TigerType type = typeExprGetType(returnType, typeAliasMap);

            List<TigerType> typeParams = new ArrayList<>();
            typeParams.add(type);
            List<Node> singleParamList = new ArrayList<>();

            if (currNode.childAt(0).isNT()) {
                Node singleParamNode;

                Node neparams = currNode.childAt(0).childAt(0);
                while (neparams.childAt(1).isNT()) {
                    singleParamNode = neparams.childAt(0);
                    singleParamList.add(singleParamNode);
                    typeParams.add(typeExprGetType(singleParamNode.childAt(2), typeAliasMap));
                    neparams = neparams.childAt(1);
                }
                singleParamNode = neparams.childAt(0);
                singleParamList.add(singleParamNode);
                typeParams.add(typeExprGetType(singleParamNode.childAt(2), typeAliasMap));
            }

            // push func type
            typeContext.push(typeName, new TigerType(TigerTypeType.FUNCTION, typeParams));
            countPushed++;
            // push func parameter types
            for (Node currSingleParam: singleParamList) {
                typeContext.push(currSingleParam.childAt(0).symbol.strValue(),
                        typeExprGetType(currSingleParam.childAt(2), typeAliasMap));
                paramsCounter++;
            }

            //TODO


            for (Node currSingleParam: singleParamList) {
                typeContext.pop();
            }

        }
        return areStmtsWellTyped(root.childAt(3), typeContext, typeAliasMap);
    }

    private boolean areStmtsWellTyped(Node curr, SymbolTable typeContext, SymbolTable typeAliasMap) {
        return false;
    }

    private TigerType exprGetType(Node curr, SymbolTable typeContext) {
        if (curr.symbol.strValue().equals("factor")) {
            if (curr.children.size() == 1) {
                if (curr.childAt(0).symbol.strValue().equals("const")) {
                    return getTypeConst(curr.childAt(0));
                } else {
                    return typeContext.find(curr.childAt(0).symbol.strValue());
                }

            } else if (curr.childAt(0).getTerminalType() == TokenType.IDENTIFIER) {
                TigerType idType = typeContext.find(curr.childAt(0).symbol.strValue());

                if (curr.childAt(3).symbol.strValue().equals("expr")) {
                    if (idType.getType() != TigerTypeType.ARRAY) {
                        return null;
                    }

                    TigerType indexType = exprGetType(curr.childAt(3), typeContext);
                    if (indexType.getType() != TigerTypeType.INT) {
                        return null;
                    } else {
                        return idType.getTypeParams().get(0);
                    }
                } else {
                    List<TigerType> functionParams = idType.getTypeParams();
                    return null;
                }
            } else {
                return exprGetType(curr.childAt(1), typeContext);
            }

        } else if (curr.children.size() == 1) {
            return exprGetType(curr.childAt(0), typeContext);
        } else {
            TigerType leftSide = exprGetType(curr.childAt(0), typeContext);
            if (leftSide == null) {
                return null;
            }
            TigerType rightSide = exprGetType(curr.childAt(2), typeContext);
            if (rightSide == null) {
                return null;
            }

            Node op = curr.childAt(1);
            if (op.isNT()) {
                op = op.childAt(0);
            }

            switch (op.symbol.strValue()) {
                case "+":
                case "-":
                case "*":
                case "/":
                    if (leftSide.getType() != TigerTypeType.FLOAT && leftSide.getType() == TigerTypeType.INT
                            || rightSide.getType() != TigerTypeType.FLOAT && rightSide.getType() == TigerTypeType.INT) {
                        return null;
                    }
                    return leftSide.getType() == TigerTypeType.FLOAT
                            || rightSide.getType() == TigerTypeType.FLOAT
                            ? new TigerType(TigerTypeType.FLOAT) : new TigerType(TigerTypeType.INT);
                case ">":
                case "<":
                case ">=":
                case "<=":
                case "=":
                case "<>":
                    return new TigerType(TigerTypeType.BOOLEAN);
                case "|":
                case "&":
                    return leftSide.getType() == TigerTypeType.BOOLEAN
                            && rightSide.getType() == TigerTypeType.BOOLEAN
                            ? new TigerType(TigerTypeType.BOOLEAN) : null;
                default:
                    return null;

            }
        }
    }

    private TigerType typeExprGetType(Node curr, SymbolTable typeAliasMap) {
        if (curr.children.size() == 1) {
            Node primitiveNode = curr.childAt(0);
            return typeAliasMap.find(primitiveNode.symbol.strValue());
        } else {
            List<TigerType> params = new ArrayList<>(1);
            params.add(typeExprGetType(curr.childAt(5), typeAliasMap));
            return new TigerType(TigerTypeType.ARRAY, params);
        }
    }

    private TigerType getTypeConst(Node curr) {
        Node valueNode = curr.childAt(0);
        switch (valueNode.getTerminalType()) {
            case INTLIT:
                return new TigerType(TigerTypeType.INT);
            case FLOATLIT:
                return new TigerType(TigerTypeType.FLOAT);
            case TRUE:
                return new TigerType(TigerTypeType.BOOLEAN);
            case FALSE:
                return new TigerType(TigerTypeType.BOOLEAN);
            case UNDERSCORE:
                return new TigerType(TigerTypeType.UNIT);
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}