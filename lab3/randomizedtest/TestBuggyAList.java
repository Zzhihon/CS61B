package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> simple = new AListNoResizing<Integer>();
        BuggyAList<Integer> buggy = new BuggyAList<Integer>();

        for(int i = 4; i < 7; i ++) {
            simple.addLast(i);
            buggy.addLast(i);
        }

        int s1 = simple.removeLast();
        int b1 = buggy.removeLast();
        assertTrue(s1 == b1);

        int s2 = simple.removeLast();
        int b2 = buggy.removeLast();
        assertTrue(s2 == b2);

        int s3 = simple.removeLast();
        int b3 = buggy.removeLast();
        assertTrue(s3 == b3);

    }

    @Test
    public void randomizeTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<Integer>();
        int N = 5000;

        for(int i = 0; i < N; i ++){

            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                //L.addLast(randVal);
                buggy.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                //int size = L.size();
                int size = buggy.size();
                System.out.println("size: " + size);
            }

            else if (operationNumber == 2) {
                //getlast
                if(buggy.size() != 0) {
                    System.out.println(buggy.getLast());
                }
            }

            else {
                if(buggy.size() != 0) {
                    System.out.println(buggy.removeLast());
                }
            }
        }
    }
}
