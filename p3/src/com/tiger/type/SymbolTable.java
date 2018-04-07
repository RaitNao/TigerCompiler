package com.tiger.type;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private Stack<Map.Entry<String, TigerType>> scopeStack;

    public SymbolTable() {
        scopeStack = new Stack<>();
    }
    public void push(String x, TigerType type) {
        scopeStack.push(new AbstractMap.SimpleEntry<>(x, type));
    }

    public TigerType find(String x) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map.Entry<String, TigerType> currEntry = scopeStack.get(i);
            if (currEntry.getKey().equals(x)) {
                return currEntry.getValue();
            }
        }
        return null;
    }

    public void pop() {
        scopeStack.pop();
    }

}
