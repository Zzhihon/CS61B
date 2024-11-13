package capers;

import javax.swing.*;


public class Hello {

    public static String str = "ref C:\Users\Radein\Desktop\cs61b\proj2\.gitlet\refs\heads\master";
    public static String escapedPath = str.replace("\\", "\\\\");


    public static void main(String[] args) {
        System.out.println(escapedPath.substring(4));
    }
}
