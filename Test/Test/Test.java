package Test;
public class Test {
    public static void main(String[] args) {
        Count myCount = new Count();
        int times = 0;

        for (int i = 0; i < 100; i ++) {
            increment(myCount, times);
            if (i == 10) {
                return;
            }
        }

        System.out.println(myCount.count);
        System.out.println(times);

    }


    public static void increment(Count c, int times) {
        c.count++;
        times++;
        System.out.println(times);

    }
}
