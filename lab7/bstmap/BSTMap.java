package bstmap;

import java.util.Iterator;
import java.util.Set;



public class BSTMap<K extends Comparable<K>,V extends Comparable<V>> implements Map61B<K,V>{
    int size = 0;
    private BSTNode root;
    int comparison;

    public int size() {return size;}

    public void put(K key, V val) {
        root = insertRecursive(root, key, val);
    }

    private BSTNode insertRecursive(BSTNode node, K key, V value) {
        if(node != null) {
            comparison = key.compareTo(node.key);//要插入节点的key值-父节点key值
        }
        if(node == null) {
            size += 1;
            return new BSTNode(key,value);
        }
        if(comparison < 0) {
            node.left = insertRecursive(node.left, key, value);
        }
        else if (comparison > 0) {
            node.right = insertRecursive(node.right, key, value);
        }
        else if(comparison == 0) {
            node.val = value;
            return node;
        }

        return node;
    }

    public V get(K key) {
        BSTNode target_node = root.searchRecursive(key);
        if(target_node == null){
            return null;
        }
        return target_node.val;
    }

    private BSTNode node;
    private class  BSTNode {
        K key;
        V val;
        BSTNode left;
        BSTNode right;

        BSTNode(K k, V v) {
            key = k;
            val = v;
            left = null;
            right = null;
        }

        BSTNode searchRecursive(K k) {
            if(k.equals(key) && k != null) {
                return this;
            }
            if(left == null && right == null) {
                return null;
            }
            BSTNode left_check = left.searchRecursive(k);
            if(left_check == null && right != null) {
                return right.searchRecursive(k);
            }
            else {return left_check;}
        }
    }


    public void clear(){
        size = 0;
        root = null;
    }

    public boolean containsKey(K key){
        if (root == null) {
            return false;
        }
        return root.searchRecursive(key) != null;
    }

    @Override
    public V remove(K key) {throw new UnsupportedOperationException();}

    @Override
    public V remove(K key, V value) {throw new UnsupportedOperationException();}

    @Override
    public Set<K> keySet() {throw new UnsupportedOperationException();}

    @Override
    public Iterator<K> iterator() {throw new UnsupportedOperationException();}

}
