package deque;

import org.junit.Test;
import java.math.*;


import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;


public class ArrayDequeTest {

    @Test
    public void Testadd() {
          ArrayDeque<Integer> ary = new ArrayDeque<Integer>();
        for(int i = 0; i < 10000; i ++) {
            ary.addLast(i);
        }

        for(int i = 0; i < 8000; i ++) {
            ary.removeLast();
        }

        System.out.println(ary.size());

    }


}
