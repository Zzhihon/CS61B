package test_extend;

public class test1 {
    private static int num = 1;
    public test1(){
        this(num);
    }

    public test1(int a) {
        this.num = a;
    }

    public int getNum() {
        return num;
    }
}