package com.tiger.type;

import com.tiger.scanner.TokenType;
import com.tiger.syntax.TigerAST;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker {
    private TigerAST.Node root;
    private SymbolTable typeContext;
    private SymbolTable typeAliasMap;

    public TypeChecker(TigerAST.Node root) {
        this.root = root;
        typeContext = new SymbolTable();
        typeAliasMap = new SymbolTable();
    }


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

        typeAliasMap.push("int", new TigerType(TigerTypeType.INT));
        typeAliasMap.push("float", new TigerType(TigerTypeType.FLOAT));
        typeAliasMap.push("unit", new TigerType(TigerTypeType.UNIT));
        typeAliasMap.push("boolean", new TigerType(TigerTypeType.BOOLEAN));

        TigerAST.Node declseg = root.childAt(1);

        TigerAST.Node typedecls = declseg.childAt(0);
        TigerAST.Node vardecls = declseg.childAt(1);
        TigerAST.Node funcdecls = declseg.childAt(2);

        while (typedecls.childAt(0).isNT()) {
            TigerAST.Node currNode = typedecls.childAt(0);
            String typeName = currNode.childAt(1).getSymbolStr();

            TigerAST.Node typeDef = currNode.childAt(3);
            TigerType typeDefType = typeExprGetType(typeDef);
            if (typeDefType == null) {
                return false;
            }
            typeAliasMap.push(typeName, typeDefType);

            typedecls = typedecls.childAt(1);
        }

        while (vardecls.childAt(0).isNT()) {
            TigerAST.Node currNode = vardecls.childAt(0);
            TigerAST.Node typeDef = currNode.childAt(3);
            TigerAST.Node optInit = currNode.childAt(4);

            TigerType type = typeExprGetType(typeDef);
            if (type == null) {
                return false;
            }
            if (optInit.getExpectedNumCh() > 1) {
                TigerType optInitType = getTypeConst(optInit.childAt(1));
                if (optInitType == null || !optInitType.equals(type)) {
                    return false;
                }
            }
            TigerAST.Node ids = currNode.childAt(1);

            while (ids.getExpectedNumCh() > 1) {
                typeContext.push(ids.childAt(0).getSymbolStr(), type);
                ids = ids.childAt(2);
            }
            typeContext.push(ids.childAt(0).getSymbolStr(), type);

            vardecls = vardecls.childAt(1);
        }

        while (funcdecls.childAt(0).isNT()) {
            TigerAST.Node currNode = funcdecls.childAt(0);
            String typeName = currNode.childAt(1).getSymbolStr();

            TigerAST.Node params = currNode.childAt(3);
            TigerType returnType = typeExprGetType(currNode.childAt(6));

            if (returnType == null) {
                return false;
            }

            List<TigerType> typeParams = new ArrayList<>();
            List<TigerAST.Node> singleParamList = new ArrayList<>();

            if (params.childAt(0).isNT()) {
                TigerAST.Node singleParamNode;

                TigerAST.Node neparams = params.childAt(0);
                while (neparams.getExpectedNumCh() > 1) {
                    singleParamNode = neparams.childAt(0);
                    singleParamList.add(singleParamNode);
                    TigerType singleParamType = typeExprGetType(singleParamNode.childAt(2));
                    if (singleParamType == null) {
                        return false;
                    }
                    typeParams.add(singleParamType);
                    neparams = neparams.childAt(2);
                }
                singleParamNode = neparams.childAt(0);
                singleParamList.add(singleParamNode);
                TigerType singleParamType = typeExprGetType(singleParamNode.childAt(2));
                if (singleParamType == null) {
                    return false;
                }
                typeParams.add(singleParamType);
            }
            typeParams.add(returnType);

            // push func type
            typeContext.push(typeName, new TigerType(TigerTypeType.FUNCTION, typeParams));
            // push func parameter types
            for (TigerAST.Node currSingleParam: singleParamList) {
                typeContext.push(currSingleParam.childAt(0).getSymbolStr(),
                        typeExprGetType(currSingleParam.childAt(2)));
            }

            MayReturn funcType = areStmtsWellTyped(currNode.childAt(8));

            if (funcType == null || !funcType.type.equals(returnType)) {
                return false;
            }

            for (TigerAST.Node currSingleParam: singleParamList) {
                typeContext.pop();
            }

            funcdecls = funcdecls.childAt(1);
        }

        MayReturn programType = areStmtsWellTyped(root.childAt(3));
        return programType != null && programType.type == null;
    }

    private MayReturn areStmtsWellTyped(TigerAST.Node curr) {
        TigerType lastType = null;

        while (curr.getExpectedNumCh() > 1) {
            TigerAST.Node stmt = curr.childAt(0).childAt(0);
            MayReturn returnType = processStmt(stmt);

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
        TigerAST.Node stmt = curr.childAt(0).childAt(0);
        return processStmt(stmt);
    }

    private MayReturn processStmt(TigerAST.Node stmt) {
        if (!stmt.childAt(0).isNT()) {
            switch (stmt.childAt(0).getTerminalType()) {
                case RETURN:
                    return new MayReturn(exprGetType(stmt.childAt(1)), true);
                case BREAK:
                    return new MayReturn();
                case WHILE:
                case IF:
                    // check if expression returns Boolean
                    TigerType condition = exprGetType(stmt.childAt(1));
                    if (condition == null || condition.getType() != TigerTypeType.BOOLEAN) {
                        return null;
                    }

                    MayReturn ifType = areStmtsWellTyped(stmt.childAt(3));
                    if (ifType == null) {
                        return null;
                    }
                    if (stmt.getExpectedNumCh() == 5) {
                        return new MayReturn(ifType.type, false);
                    } else {
                        MayReturn elseType = areStmtsWellTyped(stmt.childAt(5));
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
                    TigerType id = typeContext.find(stmt.childAt(1).getSymbolStr());
                    TigerType startCount = exprGetType(stmt.childAt(3));
                    TigerType endCount = exprGetType(stmt.childAt(5));

                    if (id == null || startCount == null || endCount == null) {
                        return null;
                    }

                    if (!(id.getType() == TigerTypeType.INT
                            && startCount.getType() == TigerTypeType.INT
                            && endCount.getType() == TigerTypeType.INT)) {
                        return null;
                    }
                    MayReturn forType = areStmtsWellTyped(stmt.childAt(7));
                    if (forType == null) {
                        return null;
                    }
                    return new MayReturn(forType.type,false);
                default:
                    return null;
            }
        } else {
            TigerAST.Node lvalueNode = stmt.childAt(0);
            TigerType rvalue = exprGetType(stmt.childAt(2));
            TigerType id = typeContext.find(lvalueNode.childAt(0).getSymbolStr());

            if (rvalue == null) {
                return null;
            }

            if (lvalueNode.childAt(1).childAt(0).getTerminalType() == TokenType.EPSILON) {
                if (!rvalue.equals(id)) {
                    return null;
                }
            } else {
                TigerType offset = exprGetType(lvalueNode.childAt(1).childAt(1));
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

    private TigerType exprGetType(TigerAST.Node curr) {
        if (curr.getSymbolStr().equals("factor")) {
            if (curr.getExpectedNumCh() == 1) {
                if (curr.childAt(0).getSymbolStr().equals("const")) {
                    return getTypeConst(curr.childAt(0));
                } else {
                    return typeContext.find(curr.childAt(0).getSymbolStr());
                }

            } else if (curr.childAt(0).getTerminalType() == TokenType.IDENTIFIER) {
                TigerType idType = typeContext.find(curr.childAt(0).getSymbolStr());

                if (curr.childAt(3).getSymbolStr().equals("expr")) {
                    if (idType.getType() != TigerTypeType.ARRAY) {
                        return null;
                    }

                    TigerType indexType = exprGetType(curr.childAt(3));
                    if (indexType == null || indexType.getType() != TigerTypeType.INT) {
                        return null;
                    } else {
                        return idType.getTypeParams().get(0);
                    }
                } else {
                    List<TigerType> expectedFunctionParams = idType.getTypeParams();
                    List<TigerType> functionArgs = new ArrayList<>();
                    TigerAST.Node argsNode = curr.childAt(3);

                    if (argsNode.childAt(0).isNT()) {
                        TigerAST.Node neexprs = argsNode.childAt(0);
                        while (neexprs.getExpectedNumCh() > 1) {
                            TigerType expr = exprGetType(neexprs.childAt(0));
                            if (expr == null) {
                                return null;
                            }
                            functionArgs.add(expr);
                            neexprs = neexprs.childAt(2);
                        }
                        TigerType expr = exprGetType(neexprs.childAt(0));
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
                return exprGetType(curr.childAt(1));
            }
        } else if (curr.getExpectedNumCh() == 1) {
            return exprGetType(curr.childAt(0));
        } else {
            TigerType leftSide = exprGetType(curr.childAt(0));
            if (leftSide == null) {
                return null;
            }
            TigerType rightSide = exprGetType(curr.childAt(2));
            if (rightSide == null) {
                return null;
            }

            TigerAST.Node op = curr.childAt(1);
            if (op.isNT()) {
                op = op.childAt(0);
            }

            switch (op.getSymbolStr()) {
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

    private TigerType typeExprGetType(TigerAST.Node curr) {
        if (curr.getExpectedNumCh() == 1) {
            TigerAST.Node primitiveNode = curr.childAt(0);
            return typeAliasMap.find(primitiveNode.getSymbolStr());
        } else {
            List<TigerType> params = new ArrayList<>(1);
            TigerType nestedType = typeExprGetType(curr.childAt(5));
            if (nestedType == null) {
                return null;
            }
            params.add(nestedType);
            return new TigerType(TigerTypeType.ARRAY, params);
        }
    }

    private TigerType getTypeConst(TigerAST.Node curr) {
        TigerAST.Node valueNode = curr.childAt(0);
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

}
