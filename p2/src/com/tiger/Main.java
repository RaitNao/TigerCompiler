package com.tiger;

import com.tiger.parser.TigerParser;
import com.tiger.scanner.TigerScanner;
import com.tiger.scanner.TigerToken;
import com.tiger.scanner.TokenType;
import com.tiger.syntax.TigerAST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Specify .tgr file");
            System.exit(1);
        } else if (args.length == 1) {
            System.err.println("Specify compiler output");
            System.exit(1);
        } else {
            try {
                if (Arrays.asList(args).contains("--tokens")) {
                    runScanner(args[0]);
                }

                if (Arrays.asList(args).contains("--ast")) {
                    runParser(args[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


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

    private static void runParser(String filename) throws FileNotFoundException {
        Reader reader = new FileReader(new File(filename));

        TigerAST tree = new TigerAST();
        TigerParser.parse(reader, tree);

        tree.revertLeftFactoring();
        tree.revertLeftRecursion();

        System.out.print(tree);
    }
}
