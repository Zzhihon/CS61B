package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

/**
 *  Key features:
 *      manage the satgearea, set tracked and untracked
 *
 *  Key functions:
 *      add:
 *      remove: set untracked
 *
 * Key points:
 *      File file = join('DIR',"filename")
 *      That is to say, as long as we know the path of the file, we can get the file object
 *      Then get the content through the Utils interface
 *
 *      When a file is updated to tracked, a new blob object of this file should be stored in /objects
 *      The file name comes from the hash BlobID, which is generated by the file (path) and content
 *
 *  Data models:
 *      added: Map<string, string>
 *          key: File:filepath
 *          value: SHA1 id
 *      tracked: Map<string, string>
 *          key: File:filepath -> content -> blob object
 *          value: SHA1 id
 *      removed: List[]<File>
 *
 */

public class StagedArea implements Serializable{
    /**
     * tracked indicates a tracked file. When commit is executed, commit stores this tracked file. All corresponding files can be found through tracked file.
     * added indicates a newly created or modified file (some files here belong to tracked file and some belong to unctracked file (equivalent to removed file))
     * removed indicates a file removed from tracked file, which means it will be ignored when committing.
     * is_modify_index Whenever an add operation is executed on a file, it is determined whether the operation do changes
        the index before the operation. If so, the stagearea object is written to the index.
     */


    /**
     * Consider what is the difference of tracked in Commit obj and StageArea obj
     * Commit' `tracked` is entirely copy from the StageArea's `tracked`
     * only when commit is implement, then the `tracked` will be update
     * So, we can say that the `tracked` in StageArea is the same one in the latest commit of whole gitlet
     *
     */
    private final Map<String, String> tracked = new HashMap<>();

    /**
     * when a file implement add, what laws can be used to do that
     *      whether a file should be put in the `added` is determined by the comparison with the one in tracked
     *          if is new, then put
     *          if is changed then put
     *          else not put
     *
     */
    private final Map<String, String> added = new HashMap<>();
    private final Set<String> removed = new HashSet<>();
    private final String StageAreaID = "0";
    private boolean is_modify_index = false;

    public Map<String, String> getTracked() {
        return tracked;
    }

    public Map<String, String> getAdded() {
        return added;
    }

    public boolean get_is_modify_index() {
        return is_modify_index;
    }

    public void saveIndex(File file, StagedArea stagearea) {
        writeObject(file, (Serializable) stagearea);
    }

    /**
     * Processing the core logic of add
     * File copying to stagearea
     * a. Rewrite the file (modified, including file name or content)
     * b. Newly created file
     * c. If the file you want to add is already in the staging area, but the content of the file you want to add is
          the same as the file stored in the latest commit have not changed,
          Then executing the add operation will no make sense
     *
     * Tips: only the file differ from the one in commit can be added into stagearea
     */
    public void add(File file) {
        String filepath = file.getPath(); // in user's directory
        Blob dir_blob = new Blob(file); //file in user's directory
        String blobid = dir_blob.getid(); //blobid is defined by it's path(filename) and content
        String tracked_blobid = getBlobid(tracked, filepath); //file in `tracked`


        if(tracked_blobid == null) {
            //a. file name changed, so the track not have to del the previous version of blob(use `removed`)
            //   so just put this blob in the `added`
            //b. not exist this file(no implet del)
            add_put(filepath, blobid, dir_blob);
        }
        else {
            String blob_id = getBlobid(added, filepath);
            if (removed.contains(filepath)) {
                // not exist this file(after implet del)
                removed.remove(filepath);
                add_put(filepath, blobid, dir_blob);
            }else if(!tracked_blobid.equals(blobid)) {
                //file name not changed, only content change,
                    //then put in the `added`
                add_put(filepath, blobid, dir_blob);
            } else {
                //nothing change, but we need to check if it is the third type situation
                if (blob_id != null) {
                    //correspond to the third type situation
                    added.remove(filepath);
                }
            }
        }
    }

    private void add_put(String filepath, String id, Blob blob) {
        added.put(filepath, id);
        blob.save();
        is_modify_index = true;
    }

    public void saveStageArea(File index) {
        writeObject(index, this);
    }

    /**
     * Process the core logic of commit
     * Update tracked:
     *      Add all key-value pairs of added to tracked
     *      Remove all key-value pairs of removed from tracked
     * Clear added
     * Clear removed
     *
     */
    public Map<String, String> commit() {
        tracked.putAll(added);
        for (String filePath : removed) {
            tracked.remove(filePath);
        }
        clear();
        return tracked;
    }

    private void clear() {
        added.clear();
        removed.clear();
    }

    public boolean remove(File file) {
        String filePath = file.getPath();

        String addedBlobId = added.remove(filePath);
        if (addedBlobId != null) {
            return true;
        }

        if (tracked.get(filePath) != null) {
            if (file.exists()) {
                rm(file);
            }
            return removed.add(filePath);
        }
        return false;
    }

    public void removed(String path) {
        removed.add(path);
    }
}
