package gitlet;

import java.io.File;
import java.util.ResourceBundle;

import static gitlet.MyUtils.exit;
import static gitlet.MyUtils.exitWithError;
import static gitlet.Utils.join;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            exitWithError("Must have at least one argument");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                /**
                 */
                Repository.init();

                break;
            case "add":
                Repository.checkWorkingDir();
                String filename = args[1];
                // TODO: handle the `add [filename]` command
                new Repository().add(filename);
                break;

            case "commit":
                Repository.checkWorkingDir();
                if (args.length == 1) {exit("Please enter a commit message.");}
                String msg = args[1];
                new Repository().commit(msg);
                break;

            case "checkout":
                Repository.checkWorkingDir();
                String branchname;
                switch (args.length) {
                    case 3:
                        String operand = args[1];
                        if (!operand.equals("--")) {exit("Incorrect operands.");}
                        filename = args[2];
                        new Repository().checkout(filename);
                        break;

                    case 4:
                        operand = args[2];
                        if (!operand.equals("--")) {exit("Incorrect operands.");}
                        String commitid = args[1];
                        filename = args[3];
                        new Repository().checkout(commitid, filename);
                        break;

                    case 2:
                        branchname = args[1];
                        new Repository().checkoutbranch(branchname);
                        break;
                }
                break;

            case "log":
                Repository.checkWorkingDir();

                new Repository().log();
                break;

            case "rm":
                Repository.checkWorkingDir();

                filename = args[1];
                new Repository().rm(filename);
                break;

            case "status":
                Repository.checkWorkingDir();

                new Repository().status();
                break;

            case "branch":
                Repository.checkWorkingDir();

                branchname = args[1];
                new Repository().setbranch(branchname);
                break;

            case "rm-branch":
                Repository.checkWorkingDir();

                branchname = args[1];
                new Repository().rm_branch(branchname);
                break;

            case "global-log":
                Repository.checkWorkingDir();

                new Repository().globalLog();
                break;

            case "reset":
                Repository.checkWorkingDir();

                String commitid = args[1];
                new Repository().reset(commitid);
                break;

            case "find":
                Repository.checkWorkingDir();

                msg = args[1];
                new Repository().find(msg);
                break;

            default:
                {exit("No command with that name exists.");}
                break;


        }
    }
}
