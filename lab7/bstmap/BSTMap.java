package bstmap;

import java.util.*;


public class BSTMap<K extends Comparable<K>,V extends Comparable<V>> implements Map61B<K,V>{
    private int size = 0;
    private BSTNode root;
    private int comparison;
    private BSTNode left_check = null;

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
        if (root == null) {
            return null;
        }
        left_check = null;
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
            if(left != null) {
                left_check = left.searchRecursive(k);
            }
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

    public Iterator<K> iterator() {return new BSTMapIter();}

    private class BSTMapIter implements Iterator<K> {
        private BSTNode cur;
        private List<BSTNode> BSTnodeslist = BSTtree_inorder(root);
        public BSTMapIter() {cur = root;}
        private int cnt = 1;

        @Override
        public boolean hasNext() {
            return cur != null;
        }

        public K next() {

            K ret = cur.key;
            if(cnt == BSTnodeslist.size()) {cur = null;}
            else cur = BSTnodeslist.get(cnt);
            cnt += 1;
            return ret;
        }
    }

    public void printInOrder() {
        List<BSTNode> BSTnodesList = BSTtree_inorder(root);
        for(BSTNode node:BSTnodesList ) {
            System.out.println(node.key);
        }
    }

    public List<BSTNode> BSTtree_inorder(BSTNode node) {
        List<BSTNode> result = new ArrayList<>();
        inorderRecursive(root,result);
        return result;
    }

    private void inorderRecursive(BSTNode node, List<BSTNode> res) {
        if(node != null) {
            inorderRecursive(node.left, res);
            res.add(node);
            inorderRecursive(node.right,res);
        }
    }

    @Override
    public Set<K> keySet() {
        List<BSTNode> BSTnodesList = BSTtree_inorder(root);
        Set<K> BSTnodesSet = new HashSet<>();
        for (BSTNode node : BSTnodesList) {
            BSTnodesSet.add(node.key);
        }
        return BSTnodesSet;
    }

    @Override
    public V remove(K key) {
        V val = get(key);
        root = removeRecursive(root,key);
        return val;
    }

    private BSTNode removeRecursive(BSTNode node, K key) {
        if(node != null) {
            comparison = key.compareTo(node.key);//要插入节点的key值-父节点key值
        }
        if(node == null) {
            return null;
        }
        if(comparison < 0) {
            node.left = removeRecursive(node.left, key);
        } else if(comparison > 0) {
            node.right = removeRecursive(node.right, key);
        }else {
            size -= 1;
            if (node.left == null) {
                return node.right;
            }
            else if (node.right == null) {
                return node.left;
            }

            node.key = findMinVal(node.right);
            //可以node.right理解为右子树的根节点，因为这一步进行的操作就是更新右子树
            node.right = removeRecursive(node.right, node.key);
        }
        return node;
    }

    private K findMinVal(BSTNode node) {
        if(node.left == null) {
            return node.key;
        }
        return findMinVal(node.left);

    }

    @Override
    public V remove(K key, V value) {throw new UnsupportedOperationException();}






}