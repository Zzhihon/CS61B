package gitlet;
import static gitlet.MyUtils.createDir;



import java.io.File;
import java.io.Serializable;

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

    /**
     * get all commitfile
     */
    private final File[] AllCommitDirs = OBJECTS_DIR.listFiles(File::isDirectory);

    /**
     * get unstaged in status
     * get untracked in status
     */
    private TreeSet<String> global_untracked_sorted = new TreeSet<>();
    private TreeSet<String> global_unstaged_sorted = new TreeSet<>();
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
        if (GITLET_DIR.exists()) { exit("A Gitlet version-control system already exists in the current directory.");}
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
     * Exit if the repository at the current working directory is not initialized.
     */
    public static void checkWorkingDir() {
        if (!(GITLET_DIR.exists() && GITLET_DIR.isDirectory())) {
            exit("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * add untracked&&modified to StageArea
     */
    public void add(String filename) {
        File file = getFilefromCWD(filename);
        getstatus();
        if (!file.exists() && !global_unstaged_sorted.contains(filename) ) {exit("File does not exist.");}
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
        if (msg.equals("")) {exit("Please enter a commit message.");}
        if (stagearea.getAdded().isEmpty() && stagearea.getRemoved().isEmpty()) {exit("No changes added to the commit.");}
        Map<String, String> tracked = stagearea.commit();
        List<String> parentid = new ArrayList<>();
        if (HEAD.exists()) {
            //the parent commitid should be in current branch
            Commit commit = getcurrentbranchHeadCommit(current_branch);
            parentid.add(commit.getCommitID());
        }
        Commit newcommit = new Commit(msg, tracked, parentid);
        newcommit.savecommit();
        stagearea.saveStageArea(INDEX);
        updateHEAD();
        updateHEADS(newcommit);
    }

    public void find(String msg) {
        StringBuilder msgTocommit = new StringBuilder();
        boolean flag = false;
        Set<Commit> AllCommits = getAllCommit(AllCommitDirs);
        for (Commit commit : AllCommits) {
            if (msg.equals(commit.getMessage())) {
                msgTocommit.append(commit.getCommitID()).append("\n");
                flag = true;
            }
        }
        if (!flag) {exit("Found no commit with that message.");}
        System.out.print(msgTocommit);
    }

    public Commit getcurrentbranchHeadCommit(String branchname) {
        File file = join(HEADS_DIR, branchname);
        Commit commit = getCommitObj(ToString(readContents(file)));
        return commit;
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

    //get commit from HEAD
    private static Commit getHeadCommit() {
        String path = ToString(readContents(HEAD)).substring(4);
        File file = new File(path);
        String commitID =  ToString(readContents(file));
        return getCommitObj(commitID);
    }



    public void globalLog() {
        StringBuilder logBuilder = new StringBuilder();
        // As the project spec goes, the runtime should be O(N) where N is the number of commits ever made.
        // But here I choose to log the commits in the order of created date, which has a runtime of O(NlogN).
        Set<Commit> AllCommits = getAllCommit(AllCommitDirs);
        for (Commit commit : AllCommits) {
            logBuilder.append(commit.putlog()).append("\n");
        }
        System.out.print(logBuilder);
    }


    /**
     * If a working file is untracked in the current branch and would be overwritten by the checkout,
     * print "There is an untracked file in the way; delete it, or add and commit it first". and exit
     *
     * when the file untracked in the current branch, when the check-branch has the same name one and
     * this file is tracked. It will error that.
     *
     * @param branchname
     */

    public void checkoutbranch(String branchname) {
        if (!is_branch_exist(branchname)) {exit("No such branch exists");}
        if (current_branch.equals(branchname)) {exit("No need to checkout the current branch.");}
        //if (untracked_is_overwrite()) {exit( "There is an untracked file in the way; delete it, or add and commit it first.");}
        File branch = join(HEADS_DIR, branchname);
        String path = branch.getPath();
        Commit des_commit = readObject(getobjFile(ToString(readContents(branch))), Commit.class);
        switchbranch(des_commit);
        writeContents(HEAD, "ref " + path);
        current_branch = branchname;
    }

    public void switchbranch(Commit des_commit) {
        Commit cur_commit = getHeadCommit();
        Map<String, String> des_tracked = des_commit.tracked();
        Map<String, String> cur_tracked = cur_commit.tracked();
        Map<String, String> added = stagearea.getAdded();
        getstatus();
        //int cnt = 20;
        for (String path : des_tracked.keySet()) {
            File file = new File(path);
            String filename = file.getName();
            if (global_untracked_sorted.contains(filename)){exit("There is an untracked file in the way; delete it, or add and commit it first.");}
        }

        for( String path : cur_tracked.keySet()) {

            File file = new File(path);

            if(des_tracked.get(path) == null) {
                file.delete();
                stagearea.add(file);
                stagearea.getTracked().remove(path);
                //writeContents(join(CWD, String.valueOf(cnt)), "success delete");
            }
        }

        for (String path : des_tracked.keySet()) {
            File file = new File(path);
            String des_blobid = getBlobid(des_tracked, path);
            Blob blob = readObject(getobjFile(des_blobid), Blob.class);
            writeContents(file, blob.getContent());
            stagearea.getTracked().put(path, des_blobid);
        }
        stagearea.clear();

        stagearea.saveStageArea(INDEX);

        for (Map.Entry<String, String> entry : added.entrySet() ) {
            String path = entry.getKey();
            File file = new File(path);
            String staged_blobid = entry.getValue();
            if (des_tracked.equals(path)) {
                String des_blobid = des_tracked.get(path);
                if (des_blobid.equals(staged_blobid)) {
                    if ( !cur_tracked.equals(path)) {file.delete();}
                    else {  }
                }
            }else if(cur_tracked.containsKey(path)) {error("There is an untracked file in the way; delete it, or add and commit it first.");}
            else { }
        }


    }

    public void reset(String commitid) {
        if (!getobjFile(commitid).exists()) {exit("No commit with that id exists.");}
        Commit commit = getCommitObj(commitid);
        switchbranch(commit);
        File file = join(HEADS_DIR, current_branch);
        writeContents(file, commitid);

    }

    public boolean is_branch_exist(String branchname) {

        for (File file : AllBranchedFiles) {
            String filename = file.getName();
            if (filename.equals(branchname)) {return true;}
        }
        return false;
    }

    public void setbranch(String branchname) {
        if (is_branch_exist(branchname)) {exit("A branch with that name already exists.");}
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

    private String getEntireCommitID(String commitid) {
        Set<Commit> AllCommits = getAllCommit(AllCommitDirs);
        for (Commit commit : AllCommits) {
            if (commitid.equals(commit.getCommitID().substring(0,8)));
                return commit.getCommitID();
        }
        return null;
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
        checkout(headcommit.getCommitID(), filename);
    }

    public void checkout(String commitid, String filename) {
        if (commitid.length() == 8) { commitid = getEntireCommitID(commitid);}
        if (commitid == null) {exit("No commit with that id exists.");}
        if (!getobjFile(commitid).exists()) {exit("No commit with that id exists.");}
        File file = getFilefromCWD(filename);
        String filePath = getFilefromCWD(filename).getPath();
        Commit commit_tar = getCommitObj(commitid);
        String content = getBlobcontent(commit_tar, filePath);
        writeContents(file, content);
    }

    private String getBlobcontent(Commit commit, String filepath) {
        String blob_shaid = getBlobid(commit.tracked(), filepath);
        if (blob_shaid == null) { exit("File does not exist in that commit.");}
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
     * delete the branch pointer, but not delete the commit of any one
     * @param branchname
     */
    public void rm_branch(String branchname) {
        if (!is_branch_exist(branchname)) {exit("A branch with that name does not exist.");}
        if (current_branch.equals(branchname)) {exit("Cannot remove the current branch.");}
        File file = new File(HEADS_DIR, branchname);
        file.delete();

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
        TreeSet<String> allBranches = new TreeSet<>();
        branches.append("=== Branches ===").append("\n");
        for (File file : AllBranchedFiles) {
            String filename = file.getName();
            allBranches.add(filename);
        }

        for (String branch : allBranches) {
            if (branch.equals(current_branch)) {
                branches.append("*" + current_branch).append("\n");
            }else {branches.append(branch).append("\n");}
        }
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



        unstaged.append("=== Modifications Not Staged For Commit ===").append("\n");
        untracked.append("=== Untracked Files ===").append("\n");
        getstatus();
        TreeSet<String> unstaged_sorted = global_unstaged_sorted;
        TreeSet<String> untracked_sorted = global_untracked_sorted;

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

    private void getstatus() {
        Map<String, String> added = stagearea.getAdded();
        Map<String, String> currentFileMap = getcurrentMap();
        Map<String, String> tracked = stagearea.getTracked();
        Set<String> removed = stagearea.getRemoved();
        getstatus(currentFileMap, tracked, added, removed);
    }

    private void getstatus(Map<String, String> currentFileMap, Map<String, String> tracked, Map<String, String> added, Set<String> removed ) {
        TreeSet<String> unstaged_sorted = new TreeSet<>();
        TreeSet<String> untracked_sorted = new TreeSet<>();

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


        global_unstaged_sorted = unstaged_sorted;
        global_untracked_sorted = untracked_sorted;
    }
}

