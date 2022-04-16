package com.lh;

import java.net.ServerSocket;

public class Test {
    public static boolean checkPort(Integer port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static void main(String[] args) {
        for (int i = 0; i < 1000000; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(checkPort(18010)){
                System.out.println(i+":"+true);
            }else {
                System.out.println("false");
            }
        }
    }
}
