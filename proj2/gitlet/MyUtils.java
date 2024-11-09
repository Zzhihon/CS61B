package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Utils.*;


public class MyUtils {
    /**
     * create dir
     */
    public static void createDir(File dir) {
        if(!dir.mkdir()) {
            exit("Directory %s cannot create", dir.getPath());
        }
    }



    public static void exit(String msg, Object... args) {
        message(msg,args);
        System.exit(0);
    }

    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(-1);
    }

    public static File getobjFile(String sha1) {
        String dirName = sha1.substring(0,2);
        String surName = sha1.substring(2);
        return join(Repository.OBJECTS_DIR, dirName, surName);
    }

    public static void clearFile(File file) {
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
        } catch (IOException e) {
            System.err.println("Failed to clear file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveObjectFile(File file, Serializable obj) {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdir();
        }
        writeObject(file, obj);
    }

}
