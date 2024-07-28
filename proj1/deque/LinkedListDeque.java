package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node {
        public T item;
        public Node next;
        public Node prev;

        public Node(T stuff, Node n, Node m) {
            item = stuff;
            next = n;
            prev = m;
        }

    }

    private Node sentinel;
    private int size;

    /** create an empty list */
    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        size = 0;
    }

    public LinkedListDeque(T stuff) {
        sentinel = new Node(null, null, null);
        sentinel.next = new Node(stuff, null, sentinel);
        sentinel.prev = new Node(stuff, null, sentinel);
        size = 1;

    }

    @Override
    /** addfirst method */
    public void addFirst(T stuff) {
        Node j = new Node(stuff, sentinel.next, sentinel);
        if(size != 0) {
            sentinel.next.prev = j;
        }

        sentinel.next = j;

        if(size == 0) {
            sentinel.prev = j;
        }
        size = size + 1;
    }

    @Override
    public void addLast(T stuff) {
        Node j = new Node(stuff, sentinel, sentinel.prev);
        if (size != 0) {
            sentinel.prev.next = j;
            sentinel.prev = j;
        }
        if (size == 0) {
            sentinel.next = j;
            sentinel.prev = j;
        }
        size = size + 1;
    }

    @Override
    public boolean isEmpty() {
        if(sentinel.next == null) {
            return true;
        }
        return false;
    }

    @Override
    public T removeFirst() {
        if (sentinel.next != null) {
            T item = sentinel.next.item;
            sentinel.next = getFirstNode().next;
            if (size == 1) {
                sentinel.prev = null;
            }
            return item;
        }

        return null;
    }

    @Override
    public T removeLast() {
        if(sentinel.prev != null && size != 0) {
            T item = sentinel.prev.item;
            getLastNode().prev.next = sentinel;
            sentinel.prev = getLastNode().prev;
            size = size - 1;
            return item;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    public Node getFirstNode() {
        return sentinel.next;
    }

    public Node getLastNode() {
        if(size != 0) {
            return sentinel.prev;
        }
        return sentinel;
    }

    @Override
    public T get(int index) {
        int i = 0;
        Node p = sentinel.next;
        if(p != null) {
            while(i < index) {
                p = p.next;
                if(p == null) return null;
                i += 1;
            }
            return p.item;
        }
        return null;
    }

    public T getLast() {
        return sentinel.prev.item;
    }

    @Override
    public void printDeque() {
        Node p = sentinel.next;
        while( p != null) {
            System.out.print(p.item + " ");
            p = p.next;
            if(p == sentinel){
                break;
            }
        }
    }

    public T recursivehelper(Node p, int index, int cnt) {
        if(p == null) {
            return null;
        }

        if(cnt == index) {
            return p.item;
        }

        else return recursivehelper(p.next, index, cnt + 1);
    }

    public T getRecursive(int index) {
        Node p = sentinel.next;

        int cnt = 0;
        return recursivehelper(p, index, cnt);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<?> lld = (LinkedListDeque<?>) o;
        if (lld.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (lld.get(i) != get(i)) {
                return false;
            }
        }
        return true;
    }



    private class LinkedListDequeIterator implements Iterator<T> {
        Node p = sentinel;

        LinkedListDequeIterator() {
            p = sentinel.next;
        }

        public boolean hasNext() {
            return p == sentinel;
        }

        public T next() {
            T item = p.item;
            p = p.next;
            return item;
        }
    }

}
