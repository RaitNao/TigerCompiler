package com.tiger.scanner;

import com.tiger.syntax.TigerAST;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Specify .tgr file");
            System.exit(1);
        } else {
            try {
                Reader reader = new FileReader(new File(args[0]));

                if (Arrays.asList(args).contains("--tokens")) {
                    runScanner(reader);
                }

                if (Arrays.asList(args).contains("--ast")) {
                    runParser(reader);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private static void runScanner(Reader reader) throws ParseException {
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

    private static void runParser(Reader reader) throws ParseException {
        TigerAST tree = new TigerAST();
        String output = tree.parse(reader);
        System.out.print(output);
    }
}
