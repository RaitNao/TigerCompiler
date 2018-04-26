package com.tiger.controlflow;

import com.tiger.scanner.TigerToken;
import com.tiger.syntax.TigerAST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFGAnalyzer {
    private TigerAST.Node root;
    private Set<TigerToken> globals;
    private CFG mainStmts;
    private List<CFG> funcStmtsList;

    public CFGAnalyzer(TigerAST tree) {
        this.root = tree.getRoot();
        this.globals = getVardecls(root.childAt(1).childAt(1));
        this.mainStmts = new CFG(root.childAt(3), globals);
        this.funcStmtsList = new ArrayList<>();
        populateFuncStmts(this.root.childAt(1).childAt(2));
    }

    private void populateFuncStmts(TigerAST.Node node) {
        if (node.getExpectedNumCh() == 2) {
            funcStmtsList.add(new CFG(node.childAt(0).childAt(8), globals));
            populateFuncStmts(node.childAt(1));
        }
    }

    private Set<TigerToken> getVardecls(TigerAST.Node node) {
        Set<TigerToken> tokenSet = new HashSet<>();
        if (node.getExpectedNumCh() == 2) {
            tokenSet.addAll(getVardecls(node.childAt(1)));
            tokenSet.addAll(getIds(node.childAt(0).childAt(1)));
        }
        return tokenSet;
    }

    private Set<TigerToken> getIds(TigerAST.Node node) {
        Set<TigerToken> tokenSet = new HashSet<>();
        if (node.getExpectedNumCh() == 2) {
            tokenSet.addAll(getVardecls(node.childAt(2)));
        }
        tokenSet.add(node.childAt(0).getSingleTokens().get(0));
        return tokenSet;
    }

    public void printCommonSubexpressionElimination() {
        mainStmts.printCommonSubexpressionElimination();
        for (CFG funcStmts: this.funcStmtsList) {
            funcStmts.printCommonSubexpressionElimination();
        }
    }

    public void printDeadCode() {
        mainStmts.printDeadCode();
        for (CFG funcStmts: this.funcStmtsList) {
            funcStmts.printDeadCode();
        }
    }
}
