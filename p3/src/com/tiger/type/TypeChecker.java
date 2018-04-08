package com.tiger.type;

import com.tiger.scanner.TokenType;
import com.tiger.syntax.TigerAST;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker {
    private TigerAST.Node root;
    // Symbol Table for overall context + scoping
    private SymbolTable typeContext;
    // Typedef Symbol Table
    private SymbolTable typeAliasMap;

    public TypeChecker(TigerAST.Node root) {
        this.root = root;
        typeContext = new SymbolTable();
        typeAliasMap = new SymbolTable();
    }


    private class MayReturn {
        // Type that a statement "Must" or "May" return
        TigerType type;
        // Implements the "Must"/"May" classification of statement + function type returns
        boolean isMustReturn;

        MayReturn(TigerType type, boolean isMustReturn) {
            this.type = type;
            this.isMustReturn = isMustReturn;
        }

        /*
        Used by:
        1. Statements that don't return anything
        2. Program itself (doesn't return anything)
         */
        MayReturn() {
        }
    }

    public boolean isWellTyped() {

        // create "typedefs" for primitives to themselves for easier lookup
        typeAliasMap.push("int", new TigerType(TigerTypeType.INT));
        typeAliasMap.push("float", new TigerType(TigerTypeType.FLOAT));
        typeAliasMap.push("unit", new TigerType(TigerTypeType.UNIT));
        typeAliasMap.push("boolean", new TigerType(TigerTypeType.BOOLEAN));

        // program → let declseg in stmts end (1)
        TigerAST.Node declseg = root.childAt(1);

        // declseg → typedecls vardecls funcdecls (2)
        TigerAST.Node typedecls = declseg.childAt(0);
        TigerAST.Node vardecls = declseg.childAt(1);
        TigerAST.Node funcdecls = declseg.childAt(2);

        /*
        typedecls → ϵ (3)
        typedecls → typedecl typedecls (4)
        typedecl  → type id := type ; (5)
         */
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

        /*
        vardecls → ϵ (12)
        vardecls → vardecl vardecls (13)
        vardecl  → var ids : type optinit ; (14)
         */
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

            /*
            ids → id (15)
            ids → id , ids (16)
             */
            while (ids.getExpectedNumCh() > 1) {
                typeContext.push(ids.childAt(0).getSymbolStr(), type);
                ids = ids.childAt(2);
            }
            typeContext.push(ids.childAt(0).getSymbolStr(), type);

            vardecls = vardecls.childAt(1);
        }

        /*
        funcdecls → ϵ (19)
        funcdecls → funcdecl funcdecls (20)
        funcdecl  → func id ( params ) : type begin stmts end ; (21)
         */
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

            /*
            params → ϵ (22)
            params → neparams (23)
            neparams → param (24)
            neparams → param , neparams (25)
            param → id : type (26)
             */
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
            // push func parameters and their types
            for (TigerAST.Node currSingleParam: singleParamList) {
                typeContext.push(currSingleParam.childAt(0).getSymbolStr(),
                        typeExprGetType(currSingleParam.childAt(2)));
            }

            MayReturn funcType = areStmtsWellTyped(currNode.childAt(8));

            if (funcType == null || !funcType.type.equals(returnType)) {
                return false;
            }

            // pop the func parameters and their types
            for (TigerAST.Node currSingleParam: singleParamList) {
                typeContext.pop();
            }

            funcdecls = funcdecls.childAt(1);
        }

        // Get well typedness of "stmts" in "program"
        MayReturn programType = areStmtsWellTyped(root.childAt(3));

        // Type of "program" has to be none (null)
        return programType != null && programType.type == null;
    }

    private MayReturn areStmtsWellTyped(TigerAST.Node curr) {
        // If a last non-empty "May" or "Must" return type is different than a new one's
        // "Stmts" is not well-typed
        TigerType lastType = null;

        /*
        stmts → fullstmt (27)
        stmts → fullstmt stmts (28)
        fullstmt → stmt ; (29)
         */
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

            // If we get a "Must" return, then we don't need to analyze later statements
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
                    // stmt → return expr (36)
                    return new MayReturn(exprGetType(stmt.childAt(1)), true);
                case BREAK:
                    // stmt → break (35)
                    return new MayReturn();
                case WHILE:
                case IF:
                    /*
                    stmt → if expr then stmts endif (31)
                    stmt → if expr then stmts else stmts endif (32)
                    stmt → while expr do stmts enddo (33)
                     */

                    // check if "expr" returns Boolean type
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

                        /*
                        If "if" "stmts" "May"/"Must" returns type T, then "else" "stmts" also has to "May"/"Must" return T
                        Or if one "stmts" returns none, type of "stmt" is "Maybe" T, where T is type returned by the
                        other "stmts"
                         */
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
                    // stmt → for id := expr to expr do stmts enddo (34)
                    // "id" and both "expr" must be type Integer
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
            // stmt → lvalue := expr (30)
            // both sides need to be of same type
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
        /*
        expr → clause (44)
        expr → expr | clause (45)
        clause → pred (46)
        clause → clause & pred (47)
        pred → aexpr (48)
        pred → aexpr cmp aexpr (49)
        aexpr → term (56)
        aexpr → aexpr linop term (57)
        term → factor (60)
        term → term nonlinop factor (61)
         */
        if (curr.getSymbolStr().equals("factor")) {

            /*
            factor → const (64)
            factor → id (65)
            factor → id [ expr ] (66)
            factor → id ( exprs ) (67)
            factor → ( expr ) (68)
             */
            if (curr.getExpectedNumCh() == 1) {
                if (curr.childAt(0).getSymbolStr().equals("const")) {
                    return getTypeConst(curr.childAt(0));
                } else {
                    return typeContext.find(curr.childAt(0).getSymbolStr());
                }

            } else if (curr.childAt(0).getTerminalType() == TokenType.IDENTIFIER) {
                TigerType idType = typeContext.find(curr.childAt(0).getSymbolStr());

                if (curr.childAt(3).getSymbolStr().equals("expr")) {
                    // Array indexing => "id" must be Array type and "expr" has to be Integer
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
                    // Function call
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
                    // In arithmetic operations only Integer and Float types are permitted
                    // If any of operands is Float => promotion is done if the other operand is Integer
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
                    // No well-typedness check in comparison logic
                    return new TigerType(TigerTypeType.BOOLEAN);
                case "|":
                case "&":
                    // Operands of & and | must be Boolean
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
            // type → array [ intlit ] of type (11)
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
