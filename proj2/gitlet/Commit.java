package gitlet;

// TODO: any imports you need here

import javax.xml.crypto.Data;
import java.io.File;
import java.io.Serializable;
import java.security.SecureRandomParameters;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.MyUtils.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  Key functions:
 *      create/update a snapshot from stagearea
 *      generate CommitID with metadata
 *      sha-1
 *
 *      retriev snapshot by specific CommitID
 *
 * Key points:
 *      find the corresponding Commit object through CommitID, and then get `tracked`
 *
 *
 *  When we do a commit, the files which are changed will be
 *
 *  @author TODO
 */
public class Commit implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit.
     *  * Data model:
     *  *      parent: String (store parent's hash-1)
     *  *      snapshot: Map<string,string>
     *  *          key: File:filepath -> content -> blob object
     *  *          key: id
     *  *      id: String
     *  *      file: File
     *  *          content: serialize Commit object
     *  *          name: getid
     * */
    private final String message;
    private final Date date;
    private final Map<String, String> tracked;
    private final String CommitID;
    private final List<String> parentID;
    private final File file;

    /**
     * initial a commit
     *   create a initial commit:
     *      log message: "initial commit"
     *      timestamp: 0;
     *      parent node: null;
     *   computer this CommitID
     *   store
     */
    public Commit() {
        message = "";
        date = new Date(0);
        tracked = new HashMap<>();
        CommitID = "0000000000000000000000000000000000000000";
        parentID = null;
        file = null;
    }

    public Commit(String msg, Map<String, String> tr, List<String> parentid) {
        message = msg;
        date = new Date();
        tracked = tr;
        parentID = parentid;
        CommitID = generateCommitID();
        file = getobjFile(CommitID);
    }

    public Date getDate() {
        return date;
    }

    /**
     * Get the timestamp.
     *
     * @return Date and time
     */
    public String getTimestamp() {
        // Thu Jan 1 00:00:00 1970 +0000
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.CHINESE);
        return dateFormat.format(date);
    }

    public List<String> CommitParentID() {
        return parentID;
    }

    public String CommitID() {
        return CommitID;
    }

    public String message() {
        return message;
    }

    public Map<String, String> tracked() {
        return tracked;
    }


    /* TODO: fill in the rest of this class. */


    private String generateCommitID() {
        return sha1(getTimestamp(),message, parentID.toString(), tracked.toString());
    }

    public void savecommit() {
        saveObjectFile(file, this);
    }

    public void putlog() {
        System.out.println("===");
        System.out.println("commit " + CommitID);
        System.out.println("Date " + date);
        System.out.println(message);
        System.out.println("");
    }

}
