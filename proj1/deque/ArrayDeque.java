package deque;

import java.io.ObjectStreamException;
import java.util.Iterator;

import java.math.BigDecimal;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    public int length() {
        return items.length;
    }

    private void resize(int capacity) {
        T[] newitems = (T[]) new Object[capacity];
        System.arraycopy(items, 0, newitems, 0, size);
        items = newitems;
    }

    @Override
    public void addFirst(T stuff) {
        if(size == items.length) {
            resize(size * 2);
        }
        items[size] = stuff;
        size += 1;
    }

    @Override
    public void addLast(T stuff) {
        if(size == items.length) {
            resize(size * 2);
        }
        items[size] = stuff;
        size += 1;
    }

    @Override
    public T removeFirst() {
        if (items[0] != null) {
            T item = items[0];
            if(size-1 / items.length < 0.25) resize(size/4 + 1);


            T[] newitems = (T[]) new Object[items.length];
            System.arraycopy(items, 1, newitems, 0, size);
            items = newitems;
            size -= 1;
            return item;
        }
        else return null;
    }

    @Override
    public T removeLast() {
        if (items[0] != null) {
            T item = items[size-1];
            BigDecimal sub = new BigDecimal(0.25);
            BigDecimal x = new BigDecimal(size-1);
            BigDecimal y = new BigDecimal(items.length);
            BigDecimal ratio = x.divide(y,2,BigDecimal.ROUND_HALF_UP);
            int res = ratio.compareTo(sub);
            if(res == -1 || res == 0 ) resize(items.length/3 + 1);

            T[] newitems = (T[]) new Object[items.length];
            System.arraycopy(items, 0, newitems, 0, size-1);
            items = newitems;
            size -= 1;
            return item;
        }
        else return null;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int wizPos;

        private ArrayDequeIterator() {
            wizPos = 0;
        }

        public boolean hasNext() {
            return wizPos < size;
        }

        public T next() {
            T item = get(wizPos);
            wizPos += 1;
            return item;
        }

    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<T> oa = (Deque<T>) o;
        if (oa.size() != this.size()) {
            return false;
        }
        for (int i = 0; i < size; i ++) {
            if (!(oa.get(i).equals(this.get(i)))){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (items[0] == null) return true;
        else return false;
    }

    @Override
    public T get(int index) {
        return items[index];
    }


    @Override
    public void printDeque() {
        for(int i = 0; i < size; i ++) {
            System.out.print(get(i) + " ");

        }
    }
}
