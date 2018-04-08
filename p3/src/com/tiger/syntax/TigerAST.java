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
                    child.children.forEach(grandchild ->grandchild.parent = curr);
                    curr.children.remove(i);
                    if (child.childAt(0).isNT() || child.childAt(0).getTerminalType() != TokenType.EPSILON) {
                        curr.children.addAll(i, child.children);
                    }
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

    private class MayReturn {
        TigerType type;
        boolean isMustReturn;

        MayReturn(TigerType type, boolean isMustReturn) {
            this.type = type;
            this.isMustReturn = isMustReturn;
        }

        MayReturn() {
        }
    }

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

        while (typedecls.childAt(0).isNT()) {
            Node currNode = typedecls.childAt(0);
            String typeName = currNode.childAt(1).symbol.strValue();

            Node typeDef = currNode.childAt(3);
            TigerType typeDefType = typeExprGetType(typeDef, typeAliasMap);
            if (typeDefType == null) {
                return false;
            }
            typeAliasMap.push(typeName, typeDefType);

            typedecls = typedecls.childAt(1);
        }

        while (vardecls.childAt(0).isNT()) {
            Node currNode = vardecls.childAt(0);
            Node typeDef = currNode.childAt(3);
            Node optInit = currNode.childAt(4);

            TigerType type = typeExprGetType(typeDef, typeAliasMap);
            if (type == null) {
                return false;
            }
            if (optInit.children.size() > 1) {
                TigerType optInitType = getTypeConst(optInit.childAt(1));
                if (optInitType == null || !optInitType.equals(type)) {
                    return false;
                }
            }
            Node ids = currNode.childAt(1);

            while (ids.children.size() > 1) {
                typeContext.push(ids.childAt(0).symbol.strValue(), type);
                ids = ids.childAt(2);
            }
            typeContext.push(ids.childAt(0).symbol.strValue(), type);

            vardecls = vardecls.childAt(1);
        }

        while (funcdecls.childAt(0).isNT()) {
            Node currNode = funcdecls.childAt(0);
            String typeName = currNode.childAt(1).symbol.strValue();

            Node params = currNode.childAt(3);
            TigerType returnType = typeExprGetType(currNode.childAt(6), typeAliasMap);

            if (returnType == null) {
                return false;
            }

            List<TigerType> typeParams = new ArrayList<>();
            List<Node> singleParamList = new ArrayList<>();

            if (params.childAt(0).isNT()) {
                Node singleParamNode;

                Node neparams = params.childAt(0);
                while (neparams.children.size() > 1) {
                    singleParamNode = neparams.childAt(0);
                    singleParamList.add(singleParamNode);
                    TigerType singleParamType = typeExprGetType(singleParamNode.childAt(2), typeAliasMap);
                    if (singleParamType == null) {
                        return false;
                    }
                    typeParams.add(singleParamType);
                    neparams = neparams.childAt(2);
                }
                singleParamNode = neparams.childAt(0);
                singleParamList.add(singleParamNode);
                TigerType singleParamType = typeExprGetType(singleParamNode.childAt(2), typeAliasMap);
                if (singleParamType == null) {
                    return false;
                }
                typeParams.add(singleParamType);
            }
            typeParams.add(returnType);

            // push func type
            typeContext.push(typeName, new TigerType(TigerTypeType.FUNCTION, typeParams));
            // push func parameter types
            for (Node currSingleParam: singleParamList) {
                typeContext.push(currSingleParam.childAt(0).symbol.strValue(),
                        typeExprGetType(currSingleParam.childAt(2), typeAliasMap));
            }

            MayReturn funcType = areStmtsWellTyped(currNode.childAt(8), typeContext);

            if (funcType == null || !funcType.type.equals(returnType)) {
                return false;
            }

            for (Node currSingleParam: singleParamList) {
                typeContext.pop();
            }

            funcdecls = funcdecls.childAt(1);
        }

        MayReturn programType = areStmtsWellTyped(root.childAt(3), typeContext);
        return programType != null && programType.type == null;
    }

    private MayReturn areStmtsWellTyped(Node curr, SymbolTable typeContext) {
        TigerType lastType = null;

        while (curr.children.size() > 1) {
            Node stmt = curr.childAt(0).childAt(0);
            MayReturn returnType = processStmt(stmt, typeContext);

            if (returnType == null) {
                return null;
            }

            if (lastType != null) {
                if (returnType.type != null
                        && !returnType.type.equals(lastType)) {
                    return null;
                }
            }
            lastType = returnType.type;

            if (returnType.isMustReturn) {
                return returnType;
            }



            curr = curr.childAt(1);
        }
        Node stmt = curr.childAt(0).childAt(0);
        return processStmt(stmt, typeContext);
    }

    private MayReturn processStmt(Node stmt, SymbolTable typeContext) {
        if (!stmt.childAt(0).isNT()) {
            switch (stmt.childAt(0).getTerminalType()) {
                case RETURN:
                    return new MayReturn(exprGetType(stmt.childAt(1), typeContext), true);
                case BREAK:
                    return new MayReturn();
                case WHILE:
                case IF:
                    // check if expression returns Boolean
                    TigerType condition = exprGetType(stmt.childAt(1), typeContext);
                    if (condition == null || condition.getType() != TigerTypeType.BOOLEAN) {
                        return null;
                    }

                    MayReturn ifType = areStmtsWellTyped(stmt.childAt(3), typeContext);
                    if (ifType == null) {
                        return null;
                    }
                    if (stmt.children.size() == 5) {
                        return new MayReturn(ifType.type, false);
                    } else {
                        MayReturn elseType = areStmtsWellTyped(stmt.childAt(5), typeContext);
                        if (elseType == null) {
                            return null;
                        }

                        if (ifType.type == null) {
                            return new MayReturn(elseType.type, false);
                        } else {
                            if (elseType.type == null) {
                                return new MayReturn(ifType.type, false);
                            } else if (elseType.type.equals(ifType.type)) {
                                return new MayReturn(elseType.type, elseType.isMustReturn && ifType.isMustReturn);
                            }
                            return null;
                        }
                    }
                case FOR:
                    TigerType id = typeContext.find(stmt.childAt(1).symbol.strValue());
                    TigerType startCount = exprGetType(stmt.childAt(3), typeContext);
                    TigerType endCount = exprGetType(stmt.childAt(5), typeContext);

                    if (id == null || startCount == null || endCount == null) {
                        return null;
                    }

                    if (!(id.getType() == TigerTypeType.INT
                            && startCount.getType() == TigerTypeType.INT
                            && endCount.getType() == TigerTypeType.INT)) {
                        return null;
                    }
                    MayReturn forType = areStmtsWellTyped(stmt.childAt(7), typeContext);
                    if (forType == null) {
                        return null;
                    }
                    return new MayReturn(forType.type,false);
                default:
                    return null;
            }
        } else {
            Node lvalueNode = stmt.childAt(0);
            TigerType rvalue = exprGetType(stmt.childAt(2), typeContext);
            TigerType id = typeContext.find(lvalueNode.childAt(0).symbol.strValue());

            if (rvalue == null) {
                return null;
            }

            if (lvalueNode.childAt(1).childAt(0).getTerminalType() == TokenType.EPSILON) {
                if (!rvalue.equals(id)) {
                    return null;
                }
            } else {
                TigerType offset = exprGetType(lvalueNode.childAt(1).childAt(1), typeContext);
                if (offset == null || offset.getType() != TigerTypeType.INT) {
                    return null;
                }
                if (!rvalue.equals(id.getTypeParams().get(0))) {
                    return null;
                }
            }
            return new MayReturn();
        }
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
                    if (indexType == null || indexType.getType() != TigerTypeType.INT) {
                        return null;
                    } else {
                        return idType.getTypeParams().get(0);
                    }
                } else {
                    List<TigerType> expectedFunctionParams = idType.getTypeParams();
                    List<TigerType> functionArgs = new ArrayList<>();
                    Node argsNode = curr.childAt(3);

                    if (argsNode.childAt(0).isNT()) {
                        Node neexprs = argsNode.childAt(0);
                        while (neexprs.children.size() > 1) {
                            TigerType expr = exprGetType(neexprs.childAt(0), typeContext);
                            if (expr == null) {
                                return null;
                            }
                            functionArgs.add(expr);
                            neexprs = neexprs.childAt(2);
                        }
                        TigerType expr = exprGetType(neexprs.childAt(0), typeContext);
                        if (expr == null) {
                            return null;
                        }
                        functionArgs.add(expr);
                    }

                    // expected function params include return type
                    if (expectedFunctionParams.size() != functionArgs.size() + 1) {
                        return null;
                    }
                    for (int i = 0; i < functionArgs.size(); i++) {
                        if (!expectedFunctionParams.get(i).equals(functionArgs.get(i))) {
                            return null;
                        }
                    }
                    return expectedFunctionParams.get(expectedFunctionParams.size() - 1);
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
                    if (leftSide.getType() != TigerTypeType.FLOAT && leftSide.getType() != TigerTypeType.INT
                            || rightSide.getType() != TigerTypeType.FLOAT && rightSide.getType() != TigerTypeType.INT) {
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
            TigerType nestedType = typeExprGetType(curr.childAt(5), typeAliasMap);
            if (nestedType == null) {
                return null;
            }
            params.add(nestedType);
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