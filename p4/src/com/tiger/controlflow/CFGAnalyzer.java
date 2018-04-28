package com.tiger.controlflow;

import com.tiger.scanner.TigerToken;
import com.tiger.syntax.TigerAST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFGAnalyzer {
    private Set<TigerToken> globals;
    private CFG mainStmts;
    private List<CFG> funcStmtsList;

    public CFGAnalyzer(TigerAST tree) {
        TigerAST.Node root = tree.getRoot();
        this.globals = getVardecls(root.childAt(1).childAt(1));
        this.mainStmts = new CFG(root.childAt(3), globals);
        this.funcStmtsList = new ArrayList<>();
        populateFuncLists(root.childAt(1).childAt(2));
    }

    private void populateFuncLists(TigerAST.Node node) {
        if (node.getExpectedNumCh() == 2) {
            Set<TigerToken> paramSet;
            Set<TigerToken> differenceSet = new HashSet<>(globals);
            if (node.childAt(0).childAt(3).childAt(0).isNT()) {
                paramSet = getParams(node.childAt(0).childAt(3).childAt(0));
            } else {
                paramSet = new HashSet<>();
            }
            differenceSet.removeAll(paramSet);
            funcStmtsList.add(new CFG(node.childAt(0).childAt(8), differenceSet));
            populateFuncLists(node.childAt(1));
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

    private Set<TigerToken> getParams(TigerAST.Node node) {
        Set<TigerToken> tokenSet = new HashSet<>();
        tokenSet.add(node.childAt(0).childAt(0).getSingleTokens().get(0));

        if (node.getExpectedNumCh() == 3) {
            tokenSet.addAll(getParams(node.childAt(2)));
        }
        return tokenSet;
    }

    public void printEliminations() {
        mainStmts.printEliminations();
        for (CFG funcStmts: this.funcStmtsList) {
            funcStmts.printEliminations();
        }
    }
}
