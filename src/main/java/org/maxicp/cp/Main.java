package org.maxicp.cp;

import org.maxicp.util.Procedure;

public class Main {
    public static void main(String[] args) {
        final int a = 2;
        wait(5,() -> {
            System.out.println("Hello");

        });
    }

    public static void wait(int t, Procedure p) {
        //
    }
}
