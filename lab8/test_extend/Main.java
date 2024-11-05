package test_extend;


public class Main {
    public static void main(String[] args) {
        test1 t1 = new test1();
        test2 t2 = new test2(2);
        test3 t3 = new test3(3);
        System.out.println(t1.getNum());
        System.out.println(t2.getNum());
        System.out.println(t3.getNum());
    }
}