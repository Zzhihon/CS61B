package gitlet;

import java.io.File;
import java.util.ResourceBundle;

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
                String filename = args[1];
                // TODO: handle the `add [filename]` command
                new Repository().add(filename);
                break;

            case "commit":
                String msg = args[1];
                new Repository().commit(msg);
                break;

            case "checkout":
                switch (args.length) {
                    case 3:
                        filename = args[2];
                        new Repository().checkout(filename);
                        break;

                    case 4:
                        String commitid = args[1];
                        filename = args[3];
                        new Repository().checkout(commitid, filename);
                        break;
                }
                break;

            case "log":
                new Repository().log();
                break;

            case "rm" :
                filename = args[1];
                new Repository().rm(filename);
        }
    }
}
