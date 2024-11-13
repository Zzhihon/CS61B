package gitlet;
import static gitlet.MyUtils.createDir;



import java.io.File;
import java.io.Serializable;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.MyUtils.*;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  Key functions:
 *      init()
 *      Blob Management:
 *          generate single file SHA-1 hash
 *          store
 *      Commit Management:
 *          generate CommitID
 *          store
 *          get specific CommitID
 *      Checkout
 *          checkout specific CommitID
 *          and restore
 *
 *  Data models:
 *      dir: id.substring(0:2)
 *      file: id.substring(2)
 *
 *  @author TODO
 */

/**
 * Every time you enter an operation to be executed in Main, a new Repository instance will be created
 * Most of the logic of executing commands is encapsulated in this file
 * The interaction process should be handled from the perspective of file management and object-oriented
 */
public class Repository implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    private static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    private static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * .gitlet/objects/
     * Used to store new blob object and commit object
     * Take the first two digits of hashid as the dir name
     * Take hashid minus the first two digits as the file name
     * The file stores serialized objects, which are strings
     */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /**
     * .gitlet/index
     * Used to store serialized information of stagearea object
     * When performing the add operation, first check whether index exists
     * if True: indicates that there has been a submission before, and the stagearea instance has been created,
     * Retrieve this instance through readobject
     * if False: indicates that there has been no submission before, and a new one is needed
     */
    private static final File INDEX = join(GITLET_DIR, "index");

    /**
     * .gitlet refs/
     */
    private static final File REFS_DIR = join(GITLET_DIR, "refs");

    /**
     * .gitlet refs/heads
     * Files named after the branch name are stored here, and each file records HEAD's point to the latest CommitID in the branch
     */
    private static final File HEADS_DIR = join(REFS_DIR, "heads");

    /**
     * .gitlet/HEAD
     * here will store whole gitlet's HEAD which content store latest CommitID
     */
    private static final File HEAD = join(GITLET_DIR, "HEAD");

    /**
     * .gitlet/logs/refs/heads
     * here will store all the CommitID in each branch
     */

    //The default branch
    private static final String DEFAULT_BRANCH = "master";

    //current branch
    private String current_branch = (HEAD.exists()) ? getCurrentBranch() : "master";

    /**
     * currenFiles: include all the files under the CWD which are used to see what file is rm -rf
     */
    private final File[] currentFiles = CWD.listFiles(File::isFile);

    /**
     * get all branch
     */
    private final File[] AllBranchedFiles = HEADS_DIR.listFiles(File::isFile);

    /**
     * to check if index has already store Stagearea object，if true readobject，else new one
     */
    private final StagedArea stagearea = INDEX.exists() ? readObject(INDEX, StagedArea.class) : new StagedArea();

    /* TODO: fill in the rest of this class. */

    /**
     * when receive init
     * call this function:
     * initial .gitlet directory
     * create subdirectories commits, blobs, refs to store data
     * <p>
     * <p>
     * set up a master branch
     * set master as current branch
     * /**
     * Initialize a repository at the current working directory.
     *
     * <pre>
     * .gitlet
     * --- HEAD
     * --- objects
     * --- refs
     *     --- heads
     * </pre>
     */
    public static void init() {
        createDir(GITLET_DIR);
        createDir(OBJECTS_DIR);
        createDir(REFS_DIR);
        createDir(HEADS_DIR);

        Commit inital_commit = new Commit();

        inital_commit.savecommit();

        File file = join(HEADS_DIR, "master");
        String filepath = file.getPath();
        writeContents(file, inital_commit.getCommitID());
        writeContents(HEAD, "ref " + filepath);

        /**
         * this initialise master to refs/HEADS/
         * file named master which store the latest CommitID
         */

        //writeObject(head, (Serializable) inital_commit);

    }

    /**
     * add untracked&&modified to StageArea
     */
    public void add(String filename) {
        File file = getFilefromCWD(filename);
        stagearea.add(file);
        boolean flag = stagearea.get_is_modify_index();
        if (flag) stagearea.saveStageArea(INDEX);
    }

    private File getFilefromCWD(String filename) {
        Path path = Paths.get(filename);
        if (path.isAbsolute()) return new File(filename);
        return join(CWD, filename);
    }

    /**
     * update commit obj's `tracked`
     */
    public void commit(String msg) {

        Map<String, String> tracked = stagearea.commit();
        List<String> parentid = new ArrayList<>();
        if (HEAD.exists()) {
            Commit commit = getHeadCommit();
            parentid.add(commit.getCommitID());
        }
        Commit newcommit = new Commit(msg, tracked, parentid);
        newcommit.savecommit();
        stagearea.saveStageArea(INDEX);
        updateHEAD();
        updateHEADS(newcommit);
    }

    public void updateHEAD() {
        File file = join(HEADS_DIR, current_branch);
        String filepath = file.getPath();
        writeContents(HEAD, "ref " + filepath);
    }

    public void updateHEADS(Commit latestCommit) {
        File branch = join(HEADS_DIR, current_branch);
        writeContents(branch, latestCommit.getCommitID());
    }

    private static Commit getHeadCommit() {
        String path = ToString(readContents(HEAD)).substring(4);
        writeContents(join(CWD,"test"), path);
        File file = new File(path);
        String commitID =  ToString(readContents(file));
        return readObject(getobjFile(commitID), Commit.class);
    }

    /**
     * checkout -- [filename]
     * Takes the version of the file as it exists in the head commit and
     *     puts it in the working directory
     * then overwrite or create the file
     *
     * checkout [branchname]
     *      
     */
    public void checkout(String filename) {
        Commit headcommit = getHeadCommit();
        checkout(headcommit.getCommitID(),filename);
    }

    public void checkoutbranch(String branchname) {
        if (!is_branch_exist(branchname)) {exit("No such branch exists");}
        if (current_branch == branchname) {exit("No need to checkout the current branch.");}
        //if (untracked_is_overwrite()) {exit( "There is an untracked file in the way; delete it, or add and commit it first.");}
        File branch = join(HEADS_DIR, branchname);
        String path = branch.getPath();
        writeContents(HEAD, "ref " + path);
        current_branch = branchname;
    }

    public boolean untracked_is_overwrite() {

        return false;
    }

    public boolean is_branch_exist(String branchname) {

        for (File file : AllBranchedFiles) {
            String filename = file.getName();
            if (filename.equals(branchname)) {return true;}
        }
        return false;
    }

    public static void setbranch(String branchname) {
        File branch = join(HEADS_DIR, branchname);
        String commitid = getHeadCommit().getCommitID();
        writeContents(branch, commitid);
    }

    private String getCurrentBranch() {
        String path = ToString(readContents(HEAD)).substring(4);
        File file = new File(path);
        String branchname = file.getName();
        return branchname;
    }

    public void checkout(String commitid, String filename) {
        File file = getFilefromCWD(filename);
        String filePath = getFilefromCWD(filename).getPath();
        Commit commit_tar = readObject(getobjFile(commitid), Commit.class);
        String content = getBlobcontent(commit_tar, filePath);
        writeContents(file, content);
    }


    private String getBlobcontent(Commit commit, String filepath) {
        String blob_shaid = getBlobid(commit.tracked(), filepath);
        String dir_name = blob_shaid.substring(0,2);
        String sur_name = blob_shaid.substring(2);
        File target = join(OBJECTS_DIR,dir_name,sur_name);
        Blob tar_blob = readObject(target, Blob.class);
        return tar_blob.getContent();
    }


    public Map<String, String> getcurrentMap() {
        Map<String, String> currentMap = new HashMap<>();
        Set<String> default_files = new HashSet<>();
        default_files.add(getFilefromCWD("Makefile").getPath());
        default_files.add(getFilefromCWD("pom.xml").getPath());
        default_files.add(getFilefromCWD("proj2.iml").getPath());

        for (File file : currentFiles) {
            if (default_files.contains(file.getPath())) { continue; }
            String shaid = sha1(file.getPath(), readContents(file));
            currentMap.put(file.getPath(), shaid);
        }
        return currentMap;
    }


    public void log() {
        StringBuilder logBuilder = new StringBuilder();
        Commit currentCommit = getHeadCommit();
        while (true) {
            logBuilder.append(currentCommit.putlog()).append("\n");
            List<String> parentCommitIds = currentCommit.getParentID();
            if (parentCommitIds.size() == 0) {
                break;
            }
            String firstParentCommitId = parentCommitIds.get(0);
            currentCommit = Commit.fromFile(firstParentCommitId);
        }
        System.out.print(logBuilder);
    }

    /**
     * not in commit: removed from added
     * in the commit: removed from tracked and del from dir
     * @param filename
     */
    public void rm(String filename) {
        File file = getFilefromCWD(filename);
        if(stagearea.remove(file)) {
            stagearea.saveStageArea(INDEX);
        }else {
            exit("No reason to remove the file.");
        }
    }


    public Set<String> get_all_rm_rf() {
        Set<String> rm_rf = new HashSet<>();
        rm_rf.addAll(rm_rf_staged());
        rm_rf.addAll(rm_rf_tracked());
        return rm_rf;
    }

    public Set<String> rm_rf_tracked() {
        Map<String, String> tracked = stagearea.getTracked();
        Map<String, String> currentMap = getcurrentMap();
        return get_rm_rf(tracked, currentMap);
    }

    public Set<String> rm_rf_staged() {
        Map<String, String> added = stagearea.getAdded();
        Map<String, String> currentMap = getcurrentMap();
        return get_rm_rf(added, currentMap);
    }

    /**
     * === Branches ===
     * Displays what branches currently exist, and marks the current branch with a *.
     * *master
     * other-branch
     *
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     *
     * === Removed Files ===
     * goodbye.txt
     *
     *  tips: when delete from directory, the file won't put in removed.
     *        only then when imple add will put this file in removed
     *
     *        and if you don't imple add, the commit will still keep the file in tracked
     *
     * === Untracked Files ===
     * for files present in the working directory
     * but neither staged for addition nor tracked
     * this includes files that have been staged for removal, but then re-created the same name one
     */


    public void status() {

        StringBuilder statusbuilder = new StringBuilder();

        // branched
        StringBuilder branches = new StringBuilder();
        branches.append("=== Branches ===").append("\n");
        branches.append(current_branch).append("\n");

        statusbuilder.append(branches).append("\n");

        //staged files, those who in the added and is gona to put in tracked
        StringBuilder status_added = new StringBuilder();
        TreeSet<String> added_sort = new TreeSet<>();
        Map<String, String> added = stagearea.getAdded();
        status_added.append("=== Staged Files ===").append("\n");
        for ( String key : added.keySet()) {
            File file = new File(key);
            String filename =file.getName();
            added_sort.add(filename);
        }
        for (String filename : added_sort) {
            status_added.append(filename).append("\n");
        }
        statusbuilder.append(status_added).append("\n");

        //removed files, those who in the removed and is gona to del from tracked
        StringBuilder status_removed = new StringBuilder();
        Set<String> removed = stagearea.getRemoved();
        HashSet<String> removed_sort = new HashSet<>();
        statusbuilder.append("=== Removed Files ===").append("\n");
        for ( String path : removed) {
            File file = new File(path);
            String filename = file.getName();
            removed_sort.add(filename);
        }
        for (String filename : removed_sort) {
            status_removed.append(filename).append("\n");
        }
        statusbuilder.append(status_removed).append("\n");

        //modify no staged
        /**
         *
         * === Modifications Not Staged For Commit ===
         * a.txt: Tracked in the current commit, changed in the working directory, but not staged; or
         * b.txt: Staged for addition, but with different contents than in the working directory; or
         * c.txt: Staged for addition, but deleted in the working directory; or
         * d.txt: Not staged for removal, but tracked in the current commit and deleted from the working directory.
         *
         *          //when the file is delete in dir, then
         *          1. remove it from added and put it in removed
         *          2. put it in removed
         */
        StringBuilder unstaged = new StringBuilder();
        StringBuilder untracked = new StringBuilder();
        Map<String, String> currentFileMap = getcurrentMap();
        Map<String, String> tracked = stagearea.getTracked();
        TreeSet<String> unstaged_sorted = new TreeSet<>();
        TreeSet<String> untracked_sorted = new TreeSet<>();


        unstaged.append("=== Modifications Not Staged For Commit ===").append("\n");
        untracked.append("=== Untracked Files ===").append("\n");

        for (Map.Entry<String, String> entry : currentFileMap.entrySet()) {
            String filepath = entry.getKey();
            String cur_blobid = entry.getValue();
            File cur_file = new File(filepath);
            String filename = cur_file.getName();
            if (tracked.get(filepath) == null) {
                if (added.get(filepath) == null) {
                    untracked_sorted.add(filename);
                } else {
                    String added_blobid = getBlobid(added, filepath);
                    if (!cur_blobid.equals(added_blobid)) {
                        unstaged_sorted.add(filename);
                    }
                }
            }else {
                if (!cur_blobid.equals(getBlobid(tracked, filepath))) {
                    unstaged_sorted.add(filename);
                }
            }
        }
        for (String filepath : get_all_rm_rf()) {
            File file = new File(filepath);
            String filename = file.getName();
            if(removed.contains(filepath)) { continue; }

            unstaged_sorted.add(filename);
        }

        for (String filename : unstaged_sorted) {
            unstaged.append(filename).append("\n");
        }

        for (String filename : untracked_sorted) {
            untracked.append(filename).append("\n");
        }
        statusbuilder.append(unstaged).append("\n");
        statusbuilder.append(untracked).append("\n");

        System.out.print(statusbuilder);
        //untracked files
    }
}

