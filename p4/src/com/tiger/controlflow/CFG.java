package com.tiger.controlflow;

import com.tiger.parser.TigerNT;
import com.tiger.parser.TigerParser;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;
import com.tiger.syntax.TigerAST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFG {
    private List<CFGNode> deadList;
    private CFGNode start;
    private CFGNode end;

    public CFG(TigerAST.Node statements) {
        deadList = new ArrayList<>();
        end = new CFGNode();
        start = buildSubCFG(statements, end, null, end);
        System.out.println("lul");
    }

    private CFGNode buildSubCFG(TigerAST.Node node, CFGNode nextStmt, CFGNode nextAfterLoopStmt, CFGNode endStmt) {
        switch (node.getSymbolStr()) {
            case "stmts":
                CFGNode fullstmt;
                if (node.getExpectedNumCh() == 2) {
                    CFGNode stmts = buildSubCFG(node.childAt(1), nextStmt, nextAfterLoopStmt, endStmt);
                    fullstmt = buildSubCFG(node.childAt(0), stmts, nextAfterLoopStmt, endStmt);
                } else {
                    fullstmt = buildSubCFG(node.childAt(0), nextStmt, nextAfterLoopStmt, endStmt);
                }
                return fullstmt;
            case "fullstmt":
                return buildSubCFG(node.childAt(0), nextStmt, nextAfterLoopStmt, endStmt);
            case "stmt":
                switch (node.childAt(0).getSymbolStr()) {
                    case "if": {
                        CFGNode expr = new CFGNode(node.childAt(1));
                        CFGNode ifStmts = buildSubCFG(node.childAt(3), nextStmt, nextAfterLoopStmt, endStmt);
                        expr.nextBlock = ifStmts;
                        if (node.getExpectedNumCh() == 7) {
                            CFGNode elseStmts = buildSubCFG(node.childAt(5), nextStmt, nextAfterLoopStmt, endStmt);
                            expr.altConnectNode(elseStmts);
                        } else {
                            expr.altConnectNode(nextStmt);
                        }
                        return expr;
                    }
                    case "while": {
                        CFGNode expr = new CFGNode(node.childAt(1));
                        CFGNode stmts = buildSubCFG(node.childAt(3), expr, nextStmt, endStmt);

                        expr.nextBlock = stmts;

                        expr.altConnectNode(nextStmt);
                        return expr;
                    }
                    case "for": {
                        TigerAST assignAst = new TigerAST();
                        List<TigerToken[]> assignList = new ArrayList<>();
                        assignList.addAll(node.childAt(1).getTokens());
                        assignList.addAll(node.childAt(2).getTokens());
                        assignList.addAll(node.childAt(3).getTokens());
                        assignList.add(new TigerToken[] {
                                new TigerToken(TokenType.EOF)
                        });

                        TigerParser.parse(assignList.iterator(), assignAst, new TigerNT("stmt"));

                        TigerAST exprAst = new TigerAST();
                        List<TigerToken[]> exprList = new ArrayList<>();
                        exprList.addAll(node.childAt(1).getTokens());
                        exprList.add(new TigerToken[]{
                                new TigerToken(TokenType.LESS)
                        });
                        exprList.addAll(node.childAt(5).getTokens());
                        exprList.add(new TigerToken[] {
                                new TigerToken(TokenType.EOF)
                        });

                        TigerParser.parse(exprList.iterator(), exprAst, new TigerNT("expr"));

                        TigerAST incrementAst = new TigerAST();
                        List<TigerToken[]> incrementList = new ArrayList<>();
                        incrementList.addAll(node.childAt(1).getTokens());
                        incrementList.add(new TigerToken[]{
                                new TigerToken(TokenType.ASSIGN)
                        });
                        incrementList.addAll(node.childAt(1).getTokens());
                        incrementList.add(new TigerToken[]{
                                new TigerToken(TokenType.PLUS)
                        });
                        incrementList.add(new TigerToken[]{
                                new TigerToken(TokenType.INTLIT, "1")
                        });
                        incrementList.add(new TigerToken[] {
                                new TigerToken(TokenType.EOF)
                        });

                        TigerParser.parse(incrementList.iterator(), incrementAst, new TigerNT("stmt"));

                        CFGNode assign = new CFGNode(assignAst.getRoot());
                        CFGNode expr = new CFGNode(exprAst.getRoot());
                        assign.connectNode(expr);

                        CFGNode increment = new CFGNode(incrementAst.getRoot());

                        CFGNode stmts = buildSubCFG(node.childAt(7), increment, nextStmt, endStmt);

                        increment.connectNode(expr);

                        expr.nextBlock = stmts;
                        expr.altConnectNode(nextStmt);

                        return assign;
                    }
                    case "break": {
                        CFGNode breakNode = new CFGNode(node);
                        if (nextAfterLoopStmt == null) {
                            breakNode.connectNode(nextStmt);
                        } else {
                            breakNode.connectNode(nextAfterLoopStmt);
                        }

                        return breakNode;
                    }
                    case "return": {
                        CFGNode returnCFG = new CFGNode(node);
                        this.deadList.add(nextStmt);
                        returnCFG.connectNode(endStmt);

                        return returnCFG;
                    }
                    default: {
                        CFGNode assign = new CFGNode(node);
                        assign.connectNode(nextStmt);

                        return assign;
                    }
                }
            default:
                throw new IllegalArgumentException("Passing something other than stmts, fullstmt or stmt");
        }
    }

    private class CFGNode {
        private TigerAST.Node statement;
        private CFGNode nextBlock;
        private CFGNode altNextBlock;
        private Set<CFGNode> previousBlocks;

        private CFGNode() {
            previousBlocks = new HashSet<>();
        }

        private CFGNode(TigerAST.Node statement) {
            this();
            this.statement = statement;
        }

        private void addParent(CFGNode parent) {
            previousBlocks.add(parent);
        }

        private void connectNode(CFGNode next) {
            this.nextBlock = next;
            next.addParent(this);
        }

        private void altConnectNode(CFGNode next) {
            this.altNextBlock = next;
            next.addParent(this);
        }
    }

}