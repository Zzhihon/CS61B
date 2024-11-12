package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public static String getBlobid(Map<String, String> map, String filepath) {

        String blob_shaid = null;

        for(Map.Entry<String, String> entry : map.entrySet()) {
            //下面这个判断这里必须要用equals，用==会报错！
            if(entry.getKey().equals(filepath)) {
                blob_shaid = entry.getValue();
            }
        }

        return blob_shaid;
    }

    public static void rm(File file) {
        String filepath = file.getPath();
        Path path = Paths.get(filepath);
        try {
            Files.delete(path);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> get_rm_rf(Map<String, String> map, Map<String, String> currentMap) {
        Set<String> rm_rf = new HashSet<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (currentMap.get(entry.getKey()) == null) {
                //find the file that is rm -rf
                rm_rf.add(entry.getKey());
            } else {
                //the rm-rf file is in removed, and then create the same one in dir(untracked)
                //do nothing is fine
            }
        }
        return rm_rf;
    }


}
