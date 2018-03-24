package com.tiger.parser;

import java.util.Map;
import java.util.HashMap;
import com.tiger.scanner.TokenType;
import com.tiger.scanner.TigerToken;

public class LLTable {

    private static final TigerProduction[] productions = {
        new TigerProduction(new TigerNT("program"), new TigerToken(TokenType.LET), new TigerNT("declseg"), new TigerToken(TokenType.IN), new TigerNT("stmts"), new TigerToken(TokenType.END)),
        new TigerProduction(new TigerNT("declseg"), new TigerNT("typedecls"), new TigerNT("vardecls"), new TigerNT("funcdecls")),
        new TigerProduction(new TigerNT("typedecls"), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("typedecls"), new TigerNT("typedecl"), new TigerNT("typedecls")),
        new TigerProduction(new TigerNT("typedecl"), new TigerNT("type"), new TigerToken(TokenType.IDENTIFIER), new TigerToken(TokenType.ASSIGN), new TigerNT("type"), new TigerToken(TokenType.SEMICOLON)),
        new TigerProduction(new TigerNT("type"), new TigerToken(TokenType.BOOLEAN)),
        new TigerProduction(new TigerNT("type"), new TigerToken(TokenType.INT)),
        new TigerProduction(new TigerNT("type"), new TigerToken(TokenType.FLOAT)),
        new TigerProduction(new TigerNT("type"), new TigerToken(TokenType.UNIT)),
        new TigerProduction(new TigerNT("type"), new TigerToken(TokenType.IDENTIFIER)),
        new TigerProduction(new TigerNT("type"), new TigerToken(TokenType.ARRAY), new TigerToken(TokenType.LSQBR), new TigerToken(TokenType.INTLIT), new TigerToken(TokenType.RSQBR), new TigerToken(TokenType.OF), new TigerNT("type")),
        new TigerProduction(new TigerNT("vardecls"), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("vardecls"), new TigerNT("vardecl"), new TigerNT("vardecls")),
        new TigerProduction(new TigerNT("vardecl"), new TigerToken(TokenType.VAR), new TigerNT("ids"), new TigerToken(TokenType.COLON), new TigerNT("type"), new TigerNT("optinit"), new TigerToken(TokenType.SEMICOLON)),
        new TigerProduction(new TigerNT("ids"), new TigerToken(TokenType.IDENTIFIER), new TigerNT("ids", NTType.LF)),
        new TigerProduction(new TigerNT("optinit"), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("optinit"), new TigerToken(TokenType.ASSIGN), new TigerNT("const")),
        new TigerProduction(new TigerNT("funcdecls"), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("funcdecls"), new TigerNT("funcdecl"), new TigerNT("funcdecls")),
        new TigerProduction(new TigerNT("funcdecl"), new TigerToken(TokenType.FUNC), new TigerToken(TokenType.IDENTIFIER), new TigerToken(TokenType.LPAREN), new TigerNT("params"), new TigerToken(TokenType.RPAREN), new TigerToken(TokenType.COLON), new TigerNT("type"), new TigerToken(TokenType.BEGIN), new TigerNT("stmts"), new TigerToken(TokenType.END), new TigerToken(TokenType.SEMICOLON)),
        new TigerProduction(new TigerNT("params"), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("params"), new TigerNT("neparams")),
        new TigerProduction(new TigerNT("neparams"), new TigerNT("param"), new TigerNT("neparams", NTType.LF)),
        new TigerProduction(new TigerNT("param"), new TigerToken(TokenType.IDENTIFIER), new TigerToken(TokenType.COLON), new TigerNT("type")),
        new TigerProduction(new TigerNT("stmts"), new TigerNT("fullstmt"), new TigerNT("stmts", NTType.LF)),
        new TigerProduction(new TigerNT("fullstmt"), new TigerNT("stmt"), new TigerToken(TokenType.SEMICOLON)),
        new TigerProduction(new TigerNT("stmt"), new TigerNT("lvalue"), new TigerToken(TokenType.ASSIGN), new TigerNT("expr")),
        new TigerProduction(new TigerNT("stmt"), new TigerToken(TokenType.WHILE), new TigerNT("expr"), new TigerToken(TokenType.DO), new TigerNT("stmts"), new TigerToken(TokenType.ENDDO)),
        new TigerProduction(new TigerNT("stmt"), new TigerToken(TokenType.FOR), new TigerToken(TokenType.IDENTIFIER), new TigerToken(TokenType.ASSIGN), new TigerNT("expr"), new TigerToken(TokenType.TO), new TigerNT("expr"), new TigerToken(TokenType.DO), new TigerNT("stmts"), new TigerToken(TokenType.ENDDO)),
        new TigerProduction(new TigerNT("stmt"), new TigerToken(TokenType.BREAK)),
        new TigerProduction(new TigerNT("stmt"), new TigerToken(TokenType.RETURN), new TigerNT("expr")),
        new TigerProduction(new TigerNT("stmt"), new TigerToken(TokenType.IF), new TigerNT("expr"), new TigerToken(TokenType.THEN), new TigerNT("stmts"), new TigerNT("stmt", NTType.LF)),
        new TigerProduction(new TigerNT("lvalue"), new TigerToken(TokenType.IDENTIFIER), new TigerNT("optoffset")),
        new TigerProduction(new TigerNT("optoffset"), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("optoffset"), new TigerToken(TokenType.LSQBR), new TigerNT("expr"), new TigerToken(TokenType.RSQBR)),
        new TigerProduction(new TigerNT("exprs"), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("exprs"), new TigerNT("neexprs")),
        new TigerProduction(new TigerNT("neexprs"), new TigerNT("expr"), new TigerNT("neexprs", NTType.LF)),
        new TigerProduction(new TigerNT("expr"), new TigerNT("clause"), new TigerNT("expr", NTType.LR)),
        new TigerProduction(new TigerNT("clause"), new TigerNT("pred"), new TigerNT("clause", NTType.LR)),
        new TigerProduction(new TigerNT("pred"), new TigerNT("aexpr"), new TigerNT("pred", NTType.LF)),
        new TigerProduction(new TigerNT("cmp"), new TigerToken(TokenType.EQUAL)),
        new TigerProduction(new TigerNT("cmp"), new TigerToken(TokenType.NOTEQUAL)),
        new TigerProduction(new TigerNT("cmp"), new TigerToken(TokenType.LEQ)),
        new TigerProduction(new TigerNT("cmp"), new TigerToken(TokenType.MEQ)),
        new TigerProduction(new TigerNT("cmp"), new TigerToken(TokenType.LESS)),
        new TigerProduction(new TigerNT("cmp"), new TigerToken(TokenType.MORE)),
        new TigerProduction(new TigerNT("aexpr"), new TigerNT("term"), new TigerNT("aexpr", NTType.LR)),
        new TigerProduction(new TigerNT("linop"), new TigerToken(TokenType.PLUS)),
        new TigerProduction(new TigerNT("linop"), new TigerToken(TokenType.MINUS)),
        new TigerProduction(new TigerNT("term"), new TigerNT("factor"), new TigerNT("term", NTType.LR)),
        new TigerProduction(new TigerNT("nonlinop"), new TigerToken(TokenType.STAR)),
        new TigerProduction(new TigerNT("nonlinop"), new TigerToken(TokenType.FWSLASH)),
        new TigerProduction(new TigerNT("factor"), new TigerNT("const")),
        new TigerProduction(new TigerNT("factor"), new TigerToken(TokenType.LPAREN), new TigerNT("expr"), new TigerToken(TokenType.RPAREN)),
        new TigerProduction(new TigerNT("factor"), new TigerToken(TokenType.IDENTIFIER), new TigerNT("factor", NTType.LF)),
        new TigerProduction(new TigerNT("const"), new TigerToken(TokenType.TRUE)),
        new TigerProduction(new TigerNT("const"), new TigerToken(TokenType.FALSE)),
        new TigerProduction(new TigerNT("const"), new TigerToken(TokenType.UNDERSCORE)),
        new TigerProduction(new TigerNT("const"), new TigerToken(TokenType.INTLIT)),
        new TigerProduction(new TigerNT("const"), new TigerToken(TokenType.FLOATLIT)),
        new TigerProduction(new TigerNT("stmts", NTType.LF), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("stmts", NTType.LF), new TigerNT("stmts")),
        new TigerProduction(new TigerNT("pred", NTType.LF), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("pred", NTType.LF), new TigerNT("cmp"), new TigerNT("aexpr")),
        new TigerProduction(new TigerNT("term", NTType.LR), new TigerNT("nonlinop"), new TigerNT("factor"), new TigerNT("term", NTType.LR)),
        new TigerProduction(new TigerNT("term", NTType.LR), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("ids", NTType.LF), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("ids", NTType.LF), new TigerToken(TokenType.COMMA), new TigerNT("ids")),
        new TigerProduction(new TigerNT("factor", NTType.LF), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("factor", NTType.LF), new TigerToken(TokenType.LSQBR), new TigerNT("expr"), new TigerToken(TokenType.RSQBR)),
        new TigerProduction(new TigerNT("factor", NTType.LF), new TigerToken(TokenType.LPAREN), new TigerNT("exprs"), new TigerToken(TokenType.RPAREN)),
        new TigerProduction(new TigerNT("expr", NTType.LR), new TigerToken(TokenType.OR), new TigerNT("clause"), new TigerNT("expr", NTType.LR)),
        new TigerProduction(new TigerNT("expr", NTType.LR), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("neexprs", NTType.LF), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("neexprs", NTType.LF), new TigerToken(TokenType.COMMA), new TigerNT("neexprs")),
        new TigerProduction(new TigerNT("neparams", NTType.LF), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("neparams", NTType.LF), new TigerToken(TokenType.COMMA), new TigerNT("neparams")),
        new TigerProduction(new TigerNT("clause", NTType.LR), new TigerToken(TokenType.AND), new TigerNT("pred"), new TigerNT("clause", NTType.LR)),
        new TigerProduction(new TigerNT("clause", NTType.LR), new TigerToken(TokenType. EPSILON)),
        new TigerProduction(new TigerNT("stmt", NTType.LF), new TigerToken(TokenType.ENDIF)),
        new TigerProduction(new TigerNT("stmt", NTType.LF), new TigerToken(TokenType.ELSE), new TigerNT("stmts"), new TigerToken(TokenType.ENDIF)),
        new TigerProduction(new TigerNT("aexpr", NTType.LR), new TigerNT("linop"), new TigerNT("term"), new TigerNT("aexpr", NTType.LR)),
        new TigerProduction(new TigerNT("aexpr", NTType.LR), new TigerToken(TokenType. EPSILON))
        };

    private static final Map<TigerNT, Map<TokenType, TigerProduction>> map = createMap();

    private static Map<TigerNT, Map<TokenType, TigerProduction>> createMap() {
        Map<TigerNT, Map<TokenType, TigerProduction>> map = new HashMap<>();
        map.put(new TigerNT("fullstmt"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.BREAK, productions[25]);
                put(TokenType.WHILE, productions[25]);
                put(TokenType.RETURN, productions[25]);
                put(TokenType.FOR, productions[25]);
                put(TokenType.IDENTIFIER, productions[25]);
                put(TokenType.IF, productions[25]);
            }
        });
        map.put(new TigerNT("stmts", NTType.LF), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.ENDDO, productions[61]);
                put(TokenType.ELSE, productions[61]);
                put(TokenType.END, productions[61]);
                put(TokenType.ENDIF, productions[61]);
                put(TokenType.BREAK, productions[62]);
                put(TokenType.WHILE, productions[62]);
                put(TokenType.RETURN, productions[62]);
                put(TokenType.FOR, productions[62]);
                put(TokenType.IDENTIFIER, productions[62]);
                put(TokenType.IF, productions[62]);
            }
        });
        map.put(new TigerNT("typedecl"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.BOOLEAN, productions[4]);
                put(TokenType.INT, productions[4]);
                put(TokenType.ARRAY, productions[4]);
                put(TokenType.FLOAT, productions[4]);
                put(TokenType.IDENTIFIER, productions[4]);
                put(TokenType.UNIT, productions[4]);
            }
        });
        map.put(new TigerNT("factor"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.INTLIT, productions[53]);
                put(TokenType.TRUE, productions[53]);
                put(TokenType.FALSE, productions[53]);
                put(TokenType.FLOATLIT, productions[53]);
                put(TokenType.UNDERSCORE, productions[53]);
                put(TokenType.LPAREN, productions[54]);
                put(TokenType.IDENTIFIER, productions[55]);
            }
        });
        map.put(new TigerNT("neparams"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.IDENTIFIER, productions[22]);
            }
        });
        map.put(new TigerNT("linop"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.PLUS, productions[48]);
                put(TokenType.MINUS, productions[49]);
            }
        });
        map.put(new TigerNT("pred", NTType.LF), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.DO, productions[63]);
                put(TokenType.AND, productions[63]);
                put(TokenType.RPAREN, productions[63]);
                put(TokenType.COMMA, productions[63]);
                put(TokenType.TO, productions[63]);
                put(TokenType.THEN, productions[63]);
                put(TokenType.SEMICOLON, productions[63]);
                put(TokenType.RSQBR, productions[63]);
                put(TokenType.OR, productions[63]);
                put(TokenType.MEQ, productions[64]);
                put(TokenType.NOTEQUAL, productions[64]);
                put(TokenType.LEQ, productions[64]);
                put(TokenType.EQUAL, productions[64]);
                put(TokenType.LESS, productions[64]);
                put(TokenType.MORE, productions[64]);
            }
        });
        map.put(new TigerNT("term", NTType.LR), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.STAR, productions[65]);
                put(TokenType.FWSLASH, productions[65]);
                put(TokenType.DO, productions[66]);
                put(TokenType.MEQ, productions[66]);
                put(TokenType.THEN, productions[66]);
                put(TokenType.NOTEQUAL, productions[66]);
                put(TokenType.LEQ, productions[66]);
                put(TokenType.RSQBR, productions[66]);
                put(TokenType.AND, productions[66]);
                put(TokenType.RPAREN, productions[66]);
                put(TokenType.PLUS, productions[66]);
                put(TokenType.MINUS, productions[66]);
                put(TokenType.COMMA, productions[66]);
                put(TokenType.LESS, productions[66]);
                put(TokenType.TO, productions[66]);
                put(TokenType.SEMICOLON, productions[66]);
                put(TokenType.EQUAL, productions[66]);
                put(TokenType.OR, productions[66]);
                put(TokenType.MORE, productions[66]);
            }
        });
        map.put(new TigerNT("neexprs"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FALSE, productions[37]);
                put(TokenType.INTLIT, productions[37]);
                put(TokenType.LPAREN, productions[37]);
                put(TokenType.TRUE, productions[37]);
                put(TokenType.FLOATLIT, productions[37]);
                put(TokenType.UNDERSCORE, productions[37]);
                put(TokenType.IDENTIFIER, productions[37]);
            }
        });
        map.put(new TigerNT("ids", NTType.LF), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.COLON, productions[67]);
                put(TokenType.COMMA, productions[68]);
            }
        });
        map.put(new TigerNT("aexpr"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FALSE, productions[47]);
                put(TokenType.INTLIT, productions[47]);
                put(TokenType.LPAREN, productions[47]);
                put(TokenType.TRUE, productions[47]);
                put(TokenType.FLOATLIT, productions[47]);
                put(TokenType.UNDERSCORE, productions[47]);
                put(TokenType.IDENTIFIER, productions[47]);
            }
        });
        map.put(new TigerNT("const"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.TRUE, productions[56]);
                put(TokenType.FALSE, productions[57]);
                put(TokenType.UNDERSCORE, productions[58]);
                put(TokenType.INTLIT, productions[59]);
                put(TokenType.FLOATLIT, productions[60]);
            }
        });
        map.put(new TigerNT("expr", NTType.LR), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.OR, productions[72]);
                put(TokenType.DO, productions[73]);
                put(TokenType.THEN, productions[73]);
                put(TokenType.RPAREN, productions[73]);
                put(TokenType.COMMA, productions[73]);
                put(TokenType.TO, productions[73]);
                put(TokenType.RSQBR, productions[73]);
                put(TokenType.SEMICOLON, productions[73]);
            }
        });
        map.put(new TigerNT("stmts"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.BREAK, productions[24]);
                put(TokenType.WHILE, productions[24]);
                put(TokenType.RETURN, productions[24]);
                put(TokenType.FOR, productions[24]);
                put(TokenType.IDENTIFIER, productions[24]);
                put(TokenType.IF, productions[24]);
            }
        });
        map.put(new TigerNT("lvalue"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.IDENTIFIER, productions[32]);
            }
        });
        map.put(new TigerNT("vardecl"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.VAR, productions[13]);
            }
        });
        map.put(new TigerNT("param"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.IDENTIFIER, productions[23]);
            }
        });
        map.put(new TigerNT("program"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.LET, productions[0]);
            }
        });
        map.put(new TigerNT("params"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.RPAREN, productions[20]);
                put(TokenType.IDENTIFIER, productions[21]);
            }
        });
        map.put(new TigerNT("clause"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FALSE, productions[39]);
                put(TokenType.INTLIT, productions[39]);
                put(TokenType.LPAREN, productions[39]);
                put(TokenType.TRUE, productions[39]);
                put(TokenType.FLOATLIT, productions[39]);
                put(TokenType.UNDERSCORE, productions[39]);
                put(TokenType.IDENTIFIER, productions[39]);
            }
        });
        map.put(new TigerNT("optinit"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.SEMICOLON, productions[15]);
                put(TokenType.ASSIGN, productions[16]);
            }
        });
        map.put(new TigerNT("type"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.BOOLEAN, productions[5]);
                put(TokenType.INT, productions[6]);
                put(TokenType.FLOAT, productions[7]);
                put(TokenType.UNIT, productions[8]);
                put(TokenType.IDENTIFIER, productions[9]);
                put(TokenType.ARRAY, productions[10]);
            }
        });
        map.put(new TigerNT("nonlinop"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.STAR, productions[51]);
                put(TokenType.FWSLASH, productions[52]);
            }
        });
        map.put(new TigerNT("neexprs", NTType.LF), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.RPAREN, productions[74]);
                put(TokenType.COMMA, productions[75]);
            }
        });
        map.put(new TigerNT("factor", NTType.LF), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.DO, productions[69]);
                put(TokenType.MEQ, productions[69]);
                put(TokenType.THEN, productions[69]);
                put(TokenType.NOTEQUAL, productions[69]);
                put(TokenType.LEQ, productions[69]);
                put(TokenType.RSQBR, productions[69]);
                put(TokenType.AND, productions[69]);
                put(TokenType.RPAREN, productions[69]);
                put(TokenType.PLUS, productions[69]);
                put(TokenType.STAR, productions[69]);
                put(TokenType.MINUS, productions[69]);
                put(TokenType.COMMA, productions[69]);
                put(TokenType.FWSLASH, productions[69]);
                put(TokenType.OR, productions[69]);
                put(TokenType.TO, productions[69]);
                put(TokenType.SEMICOLON, productions[69]);
                put(TokenType.EQUAL, productions[69]);
                put(TokenType.LESS, productions[69]);
                put(TokenType.MORE, productions[69]);
                put(TokenType.LSQBR, productions[70]);
                put(TokenType.LPAREN, productions[71]);
            }
        });
        map.put(new TigerNT("neparams", NTType.LF), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.RPAREN, productions[76]);
                put(TokenType.COMMA, productions[77]);
            }
        });
        map.put(new TigerNT("stmt"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.IDENTIFIER, productions[26]);
                put(TokenType.WHILE, productions[27]);
                put(TokenType.FOR, productions[28]);
                put(TokenType.BREAK, productions[29]);
                put(TokenType.RETURN, productions[30]);
                put(TokenType.IF, productions[31]);
            }
        });
        map.put(new TigerNT("clause", NTType.LR), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.AND, productions[78]);
                put(TokenType.DO, productions[79]);
                put(TokenType.THEN, productions[79]);
                put(TokenType.RPAREN, productions[79]);
                put(TokenType.COMMA, productions[79]);
                put(TokenType.TO, productions[79]);
                put(TokenType.SEMICOLON, productions[79]);
                put(TokenType.RSQBR, productions[79]);
                put(TokenType.OR, productions[79]);
            }
        });
        map.put(new TigerNT("stmt", NTType.LF), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.ENDIF, productions[80]);
                put(TokenType.ELSE, productions[81]);
            }
        });
        map.put(new TigerNT("funcdecls"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.IN, productions[17]);
                put(TokenType.FUNC, productions[18]);
            }
        });
        map.put(new TigerNT("vardecls"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FUNC, productions[11]);
                put(TokenType.IN, productions[11]);
                put(TokenType.VAR, productions[12]);
            }
        });
        map.put(new TigerNT("typedecls"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.VAR, productions[2]);
                put(TokenType.FUNC, productions[2]);
                put(TokenType.IN, productions[2]);
                put(TokenType.BOOLEAN, productions[3]);
                put(TokenType.INT, productions[3]);
                put(TokenType.ARRAY, productions[3]);
                put(TokenType.FLOAT, productions[3]);
                put(TokenType.IDENTIFIER, productions[3]);
                put(TokenType.UNIT, productions[3]);
            }
        });
        map.put(new TigerNT("term"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FALSE, productions[50]);
                put(TokenType.INTLIT, productions[50]);
                put(TokenType.LPAREN, productions[50]);
                put(TokenType.TRUE, productions[50]);
                put(TokenType.FLOATLIT, productions[50]);
                put(TokenType.UNDERSCORE, productions[50]);
                put(TokenType.IDENTIFIER, productions[50]);
            }
        });
        map.put(new TigerNT("declseg"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.VAR, productions[1]);
                put(TokenType.BOOLEAN, productions[1]);
                put(TokenType.FUNC, productions[1]);
                put(TokenType.IN, productions[1]);
                put(TokenType.INT, productions[1]);
                put(TokenType.FLOAT, productions[1]);
                put(TokenType.ARRAY, productions[1]);
                put(TokenType.UNIT, productions[1]);
                put(TokenType.IDENTIFIER, productions[1]);
            }
        });
        map.put(new TigerNT("aexpr", NTType.LR), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.PLUS, productions[82]);
                put(TokenType.MINUS, productions[82]);
                put(TokenType.DO, productions[83]);
                put(TokenType.MEQ, productions[83]);
                put(TokenType.THEN, productions[83]);
                put(TokenType.NOTEQUAL, productions[83]);
                put(TokenType.LEQ, productions[83]);
                put(TokenType.RSQBR, productions[83]);
                put(TokenType.AND, productions[83]);
                put(TokenType.RPAREN, productions[83]);
                put(TokenType.COMMA, productions[83]);
                put(TokenType.LESS, productions[83]);
                put(TokenType.TO, productions[83]);
                put(TokenType.SEMICOLON, productions[83]);
                put(TokenType.EQUAL, productions[83]);
                put(TokenType.OR, productions[83]);
                put(TokenType.MORE, productions[83]);
            }
        });
        map.put(new TigerNT("optoffset"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.ASSIGN, productions[33]);
                put(TokenType.LSQBR, productions[34]);
            }
        });
        map.put(new TigerNT("ids"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.IDENTIFIER, productions[14]);
            }
        });
        map.put(new TigerNT("expr"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FALSE, productions[38]);
                put(TokenType.INTLIT, productions[38]);
                put(TokenType.LPAREN, productions[38]);
                put(TokenType.TRUE, productions[38]);
                put(TokenType.FLOATLIT, productions[38]);
                put(TokenType.UNDERSCORE, productions[38]);
                put(TokenType.IDENTIFIER, productions[38]);
            }
        });
        map.put(new TigerNT("exprs"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.RPAREN, productions[35]);
                put(TokenType.FALSE, productions[36]);
                put(TokenType.INTLIT, productions[36]);
                put(TokenType.LPAREN, productions[36]);
                put(TokenType.TRUE, productions[36]);
                put(TokenType.FLOATLIT, productions[36]);
                put(TokenType.UNDERSCORE, productions[36]);
                put(TokenType.IDENTIFIER, productions[36]);
            }
        });
        map.put(new TigerNT("pred"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FALSE, productions[40]);
                put(TokenType.INTLIT, productions[40]);
                put(TokenType.LPAREN, productions[40]);
                put(TokenType.TRUE, productions[40]);
                put(TokenType.FLOATLIT, productions[40]);
                put(TokenType.UNDERSCORE, productions[40]);
                put(TokenType.IDENTIFIER, productions[40]);
            }
        });
        map.put(new TigerNT("funcdecl"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.FUNC, productions[19]);
            }
        });
        map.put(new TigerNT("cmp"), new HashMap<TokenType, TigerProduction>() {
            {
                put(TokenType.EQUAL, productions[41]);
                put(TokenType.NOTEQUAL, productions[42]);
                put(TokenType.LEQ, productions[43]);
                put(TokenType.MEQ, productions[44]);
                put(TokenType.LESS, productions[45]);
                put(TokenType.MORE, productions[46]);
            }
        });
        return map;
    }

    public static final TigerNT startSymbol = new TigerNT("program");
    public static final TigerToken EOF = new TigerToken(TokenType.EOF);

    public static TigerProduction getProduction(TigerNT NT, TigerToken token) {
        Map<TokenType, TigerProduction> byNT = map.get(NT);
        if (byNT == null) {
			return null;
		}

		return byNT.get(token.getType());
	}
}
