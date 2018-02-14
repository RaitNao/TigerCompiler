package com.tiger.scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class Main {

    public static void main(String[] args) {
	// write your code here
        if (args.length == 0) {
            System.err.println("Specify .tgr file");
            System.exit(1);
        }

        try {
            Reader reader = new FileReader(new File(args[0]));
            TigerScanner sc = new TigerScanner(reader);
            for (TigerToken[] token: sc) {
                System.out.print(token[0]);
                if (sc.iterator().hasNext()) {
                    System.out.print(" ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
