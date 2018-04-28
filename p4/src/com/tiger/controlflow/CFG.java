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
    // end is a sentinel node
    private CFGNode end;
    private List<CFGNode> statementList;
    private List<CFGNode> funcCalls;

    private Set<TigerToken> criticals = new HashSet<>();

    public CFG(TigerAST.Node statements, Set<TigerToken> globals) {
        deadList = new ArrayList<>();
        statementList = new ArrayList<>();
        funcCalls = new ArrayList<>();
        end = new CFGNode();
        end.isUseful = true;
        start = buildSubCFG(statements, end, false, false,null);
        criticals.addAll(globals);
    }

    public void printEliminations() {
        printDeadCode();
        computeAllAvail();
        printCommonSubexpressionElimination();
    }

    private CFGNode buildSubCFG(TigerAST.Node node, CFGNode nextStmt, boolean isNextBackEdge,
                                boolean isNextAfterLoopBackEdge, CFGNode nextAfterLoopStmt) {
        switch (node.getSymbolStr()) {
            case "stmts":
                CFGNode fullstmt;
                if (node.getExpectedNumCh() == 2) {
                    CFGNode stmts = buildSubCFG(node.childAt(1), nextStmt, isNextBackEdge, isNextAfterLoopBackEdge, nextAfterLoopStmt);
                    fullstmt = buildSubCFG(node.childAt(0), stmts, false, isNextAfterLoopBackEdge, nextAfterLoopStmt);
                } else {
                    fullstmt = buildSubCFG(node.childAt(0), nextStmt, isNextBackEdge, isNextAfterLoopBackEdge, nextAfterLoopStmt);
                }
                return fullstmt;
            case "fullstmt":
                return buildSubCFG(node.childAt(0), nextStmt, isNextBackEdge, isNextAfterLoopBackEdge, nextAfterLoopStmt);
            case "stmt":
                switch (node.childAt(0).getSymbolStr()) {
                    case "if": {
                        CFGNode expr = new CFGNode(node.childAt(1));
                        CFGNode ifStmts = buildSubCFG(node.childAt(3), nextStmt, isNextBackEdge, isNextAfterLoopBackEdge, nextAfterLoopStmt);
                        expr.connectNode(ifStmts);
                        if (node.getExpectedNumCh() == 7) {
                            CFGNode elseStmts = buildSubCFG(node.childAt(5), nextStmt, isNextBackEdge, isNextAfterLoopBackEdge, nextAfterLoopStmt);
                            expr.altConnectNode(elseStmts);
                        } else {
                            expr.altConnectNode(nextStmt);
                        }
                        return expr;
                    }
                    case "while": {
                        CFGNode expr = new CFGNode(node.childAt(1));
                        CFGNode stmts = buildSubCFG(node.childAt(3), expr, true, isNextBackEdge, nextStmt);

                        expr.connectNode(stmts);

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

                        TigerAST decrementAst = new TigerAST();
                        List<TigerToken[]> decrementList = new ArrayList<>();
                        decrementList.addAll(node.childAt(1).getTokens());
                        decrementList.addAll(node.childAt(2).getTokens());
                        decrementList.addAll(node.childAt(1).getTokens());
                        decrementList.add(new TigerToken[]{
                                new TigerToken(TokenType.MINUS)
                        });
                        decrementList.add(new TigerToken[]{
                                new TigerToken(TokenType.INTLIT, "1")
                        });
                        decrementList.add(new TigerToken[] {
                                new TigerToken(TokenType.EOF)
                        });

                        TigerParser.parse(decrementList.iterator(), decrementAst, new TigerNT("stmt"));

                        TigerAST exprAst = new TigerAST();
                        List<TigerToken[]> exprList = new ArrayList<>();
                        exprList.addAll(node.childAt(1).getTokens());
                        exprList.add(new TigerToken[]{
                                new TigerToken(TokenType.LEQ)
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
                        CFGNode decrement = new CFGNode(decrementAst.getRoot());
                        assign.connectNode(decrement);
                        CFGNode expr = new CFGNode(exprAst.getRoot());
                        decrement.connectNode(expr);

                        CFGNode increment = new CFGNode(incrementAst.getRoot());
                        expr.connectNode(increment);

                        CFGNode stmts = buildSubCFG(node.childAt(7), expr, true, isNextBackEdge, nextStmt);
                        increment.connectNode(stmts);
                        expr.altConnectNode(nextStmt);

                        return assign;
                    }
                    case "break": {
                        CFGNode breakNode = new CFGNode(node);
                        breakNode.isUseful = true;
                        if (nextAfterLoopStmt == null) {
                            breakNode.connectNode(nextStmt);
                        } else {
                            breakNode.connectNode(nextAfterLoopStmt);
                            breakNode.nextIsBackEdge = isNextAfterLoopBackEdge;
                            this.deadList.add(nextStmt);
                        }

                        return breakNode;
                    }
                    case "return": {
                        CFGNode returnCFG = new CFGNode(node);
                        returnCFG.isUseful = true;
                        this.deadList.add(nextStmt);
                        returnCFG.connectNode(this.end);

                        criticals.addAll(returnCFG.getRhsOperands());

                        return returnCFG;
                    }
                    default: {
                        TigerToken leftParan = new TigerToken(TokenType.LPAREN);
                        TigerToken rightParan = new TigerToken(TokenType.RPAREN);
                        CFGNode assign = new CFGNode(node);
                        assign.connectNode(nextStmt);
                        assign.nextIsBackEdge = isNextBackEdge;
                        if (assign.DEexpr != null
                                && assign.DEexpr.contains(leftParan) && assign.DEexpr.contains(rightParan)) {
                            this.funcCalls.add(assign);
                        }
                        return assign;
                    }
                }
            default:
                throw new IllegalArgumentException("Passing something other than stmts, fullstmt or stmt");
        }
    }

    private void computeAllAvail() {
        boolean updated;
        start.availComputed = true;
        do {
            updated = false;
            for (CFGNode node: statementList) {
                updated |= node.computeAvail();
            }
        } while (updated);
    }

    private void printCommonSubexpressionElimination() {
        for (CFGNode node: statementList) {
            if (node.statement != null && node.statement.isNT()) {
                switch (node.statement.getSymbolStr()) {
                    case "stmt":
                        switch (node.statement.getExpectedNumCh()) {
                            case 3:
                                if (node.availSet.contains(node.statement.childAt(2).getSingleTokens())
                                        && node.isUseful) {
                                    if (!(node.statement.childAt(2).getSingleTokens().size() == 1 &&
                                            node.statement.childAt(2).getSingleTokens().get(0).getType() == TokenType.INTLIT)) {
                                        printStatement(node);
                                        System.err.println(";\t\t/* CSE */");
                                    }
                                }
                                break;
                            case 2:
                                if (node.availSet.contains(node.statement.childAt(1).getSingleTokens())
                                        && node.isUseful) {
                                    printStatement(node);
                                    System.err.println(";\t\t/* CSE */");
                                }
                                break;
                        }
                        break;
                }
            }
        }
    }

    private void printStatement(CFGNode node) {
        System.err.print("    ");
        node.statement.getSingleTokens().forEach(x -> {
            if (x.strValue() != null) {
                System.err.print(x.strValue() + " ");
            }
        });
    }

    private void printDeadCode() {
        for (TigerToken token: criticals) {
            recursiveFindDefs(token, end);
        }

        for (CFGNode el: funcCalls) {
            List<TigerToken> tokens = el.getRhsOperands();
            for (TigerToken token: tokens) {
                recursiveFindDefs(token, el);
                el.isUseful = true;
            }
        }

        for (CFGNode el: this.deadList) {
            CFGNode curr = el;
            if (curr.previousBlocks.size() != 0) {
                continue;
            }
            while (curr != null && curr.statement != null && curr.previousBlocks.size() < 2) {
                curr.isUseful = false;
                curr = curr.nextBlock;
            }
        }

        for (CFGNode node: statementList) {
            if (node.altNextBlock == null && !node.isUseful
                    && node.statement != null) {

                if (node.DEexpr != null) {
                    printStatement(node);
                    System.err.println(";\t\t/* Dead */");
                }
            } else {
                node.isUseful = true;
            }
        }
    }

    private void recursiveFindDefs(TigerToken token, CFGNode node) {
        Set<CFGNode> defs = new HashSet<>();
        node.previousBlocks.forEach(x -> defs.addAll(findDefs(token, x)));
        defs.remove(node);
        defs.forEach(x -> {
            if (!x.isUseful) {
                x.isUseful = true;
                x.getRhsOperands().forEach(t -> recursiveFindDefs(t, x));
            }
        });
    }

    private HashSet<CFGNode> findDefs(TigerToken var, CFGNode curr) {
        HashSet<CFGNode> definitions = new HashSet<>();
        CFGNode prev = curr.altNextBlock;
        boolean traversed = false;
        while (curr.previousBlocks.size() <= 1) {
            if (curr.statement != null && curr.statement.getSymbolStr().equals("stmt")
                    && curr.statement.getExpectedNumCh() == 3
                    && curr.statement.childAt(0).getSingleTokens().get(0).equals(var)) {
                definitions.add(curr);
                return definitions;
            } else {
                prev = curr;
                if (curr.previousBlocks.size() == 0) {
                    break;
                }
                if (curr.statement != null && curr.statement.getSymbolStr().equals("stmt")
                        && curr.statement.getExpectedNumCh() == 3
                        && curr.statement.childAt(0).getSingleTokens().get(0).equals(var)) {
                    traversed = true;
                }
                curr = (CFGNode) curr.previousBlocks.toArray()[0];
            }
        }

        if (curr.previousBlocks.size() > 1) {
            if (!traversed) {
                if (curr.statement != null && curr.statement.getSymbolStr().equals("stmt")
                        && curr.statement.getExpectedNumCh() == 3
                        && curr.statement.childAt(0).getSingleTokens().get(0).equals(var)) {
                    definitions.add(curr);
                    return definitions;
                }
            }
            for (CFGNode parent: curr.previousBlocks) {
                if (parent.nextIsBackEdge && parent.nextBlock == curr) {
                    if (prev == curr.altNextBlock) {
                        definitions.addAll(findDefs(var, parent));
                    }
                } else {
                    definitions.addAll(findDefs(var, parent));
                }
            }
        }
        return definitions;
    }

    private class CFGNode {
        private TigerAST.Node statement;
        private CFGNode nextBlock;
        private CFGNode altNextBlock;
        private Set<CFGNode> previousBlocks;
        private boolean nextIsBackEdge;
        private boolean availComputed;
        private boolean isUseful;

        private HashSet<List<TigerToken>> availSet;
        private List<TigerToken> DEexpr;

        private CFGNode() {
            nextIsBackEdge = false;
            availComputed = false;
            previousBlocks = new HashSet<>();
            availSet = new HashSet<>();
            CFG.this.statementList.add(0, this);
        }

        private CFGNode(TigerAST.Node statement) {
            this();
            this.statement = statement;
            if (statement.isNT() && statement.getSymbolStr().equals("stmt") && statement.getExpectedNumCh() == 3) {
                this.DEexpr = statement.childAt(2).getSingleTokens();
            }
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

        private boolean isKilled(List<TigerToken> tokenList) {
            if (statement!= null && tokenList != null
                    && statement.isNT() && statement.getSymbolStr().equals("stmt") && statement.getExpectedNumCh() == 3) {
                for (TigerToken el : tokenList) {
                    if (el != null && el.equals(statement.childAt(0).getSingleTokens().get(0))) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean computeAvail() {
            TigerToken leftParan = new TigerToken(TokenType.LPAREN);
            TigerToken rightParan = new TigerToken(TokenType.RPAREN);

            Set<List<TigerToken>> oldAvail = this.availSet;
            this.availSet = new HashSet<>();
            Set<List<TigerToken>> localAvail = new HashSet<>();
            boolean isInitialized = false;
            for (CFGNode parent: this.previousBlocks) {
                if (parent.isUseful) {
                    // CSE is run after Dead Code elimination, so all
                    // function calls, statements after return and break, etc. are set with correct usefulness
                    if (parent.DEexpr != null
                            && !(parent.DEexpr.contains(leftParan) && parent.DEexpr.contains(rightParan))) {
                        localAvail.add(parent.DEexpr);
                    }
                } else {
                    this.availComputed = true;
                }
                for (List<TigerToken> parentAvailEl : parent.availSet) {
                    if (!parent.isKilled(parentAvailEl)) {
                        localAvail.add(parentAvailEl);
                    }
                }
                if (!isInitialized && parent.availComputed) {
                    this.availSet.addAll(localAvail);
                    isInitialized = true;
                    this.availComputed = true;
                } else if (isInitialized) {
                    if (parent.availComputed) {
                        this.availSet.retainAll(localAvail);
                    }
                }
                localAvail.clear();

            }

            if (oldAvail.size() == this.availSet.size() && oldAvail.containsAll(this.availSet)) {
                return false;
            }
            return true;
        }

        private List<TigerToken> getRhsOperands() {
            switch (this.statement.getSymbolStr()) {
                case "expr":
                    return new ArrayList<>(); // ????? correct ?????
                case "stmt": {
                    switch (this.statement.childAt(0).getSymbolStr()) {
                        case "break":
                            return new ArrayList<>();
                        case "return":
                        default: {
                            List<TigerToken> result = new ArrayList<>();
                            TigerToken prevToken = null;
                            for (TigerToken token: this.statement.getSingleTokens()) {
                                if (prevToken != null) { // skip first token
                                    if (token.getType() == TokenType.IDENTIFIER) {
                                        result.add(token);
                                    } else if (token.getType() == TokenType.LPAREN) {
                                        if (prevToken.getType() == TokenType.IDENTIFIER) {
                                            result.remove(prevToken); // function call...
                                        }
                                    }
                                }

                                prevToken = token;
                            }
                            return result;
                        }
                    }
                }
                default:
                    throw new IllegalArgumentException("invalid statement!");
            }
        }
    }
}