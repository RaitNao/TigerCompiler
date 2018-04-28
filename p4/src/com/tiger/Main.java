package com.tiger;

import com.tiger.controlflow.CFGAnalyzer;
import com.tiger.parser.TigerParser;
import com.tiger.scanner.TigerScanner;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;
import com.tiger.syntax.TigerAST;
import com.tiger.type.TypeChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
        } else {
            if (args.length == 1 && Arrays.asList(args).contains("--help")) {
                usage();
            }
            try {
                if (Arrays.asList(args).contains("--tokens")) {
                    runScanner(args[0]);
                }

                Reader reader = new FileReader(new File(args[0]));

                TigerAST tree = new TigerAST();
                TigerParser.parse(reader, tree);

                tree.revertLeftFactoring();
                tree.revertLeftRecursion();

                if (Arrays.asList(args).contains("--type-check")) {
                    TypeChecker checker = new TypeChecker(tree.getRoot());
                    boolean isWellTyped = checker.isWellTyped();
                    if (!isWellTyped) {
                        System.err.println("Type Check Error in " + args[0]);
                        System.exit(1);
                    }
                }

                CFGAnalyzer analyzer = new CFGAnalyzer(tree);
                analyzer.printEliminations();

                if (Arrays.asList(args).contains("--ast")) {
                    System.out.print(tree);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private static void usage() {
        System.err.println("Specify .tgr file\n" +
                "[--tokens] to print tokens and any lexer errors\n" +
                "[--ast] to print out AST and any parser errors\n" +
                "[--type-check] to type check and output any type errors");
        System.exit(1);
    }
    private static void runScanner(String filename) throws FileNotFoundException {
        Reader reader = new FileReader(new File(filename));
        TigerScanner sc = new TigerScanner(reader);
        boolean firstRun = true;
        for (TigerToken[] token : sc) {
            if (token[0].getType() == TokenType.EOF)
                break;

            if (firstRun) {
                firstRun = false;
            } else {
                System.out.print(" ");
            }
            System.out.print(token[0]);
        }
    }
}
