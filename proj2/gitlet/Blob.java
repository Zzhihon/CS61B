package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

/**
 *  Key features:
 *      generate sha1 id for file
 *      get file from file sha1 id
 *
 *  Data model:
 *      sourcefile: file
 *      filepath: String
 *      content: String
 *      blobid: String (Utils func: sha1(filepath,content))
 */

public class Blob implements Serializable {
    private final File sourcefile;
    private final String filepath;
    private final byte[] content;
    private final String blobid;
    private final File objfile;

    public Blob(File file) {
        sourcefile = file;
        String path = sourcefile.getPath();
        filepath = path;
        content = readContents(file);
        blobid = sha1(path, content);
        objfile = getobjFile(blobid);


    }
    public String getFilepath() {
        return filepath;
    }

    public String getContent() {
        return new String(content);
    }

    public String getid() {
        return blobid;
    }

    public void save() {
        saveObjectFile(objfile, this);
    }



}
