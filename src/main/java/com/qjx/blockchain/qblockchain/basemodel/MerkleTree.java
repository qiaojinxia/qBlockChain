package com.qjx.blockchain.qblockchain.basemodel;

import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Sha256;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Created by caomaoboy 2019-11-04
 **/
public class MerkleTree {


    public static class TreeNode{
        //二叉树的左孩子
        private TreeNode left;
        //二叉树的右孩子
        private TreeNode right;
        //二叉树所有孩子节点的数据
        private String data;
        //二叉树中孩子节点的数据对应的哈希值，此处采用SHA-256算法
        private String hash;
        //节点名称
        private String name;
        public TreeNode(){}
        public TreeNode getLeft() {
            return left;
        }

        public void setLeft(TreeNode left) {
            this.left = left;
        }

        public TreeNode getRight() {
            return right;
        }

        public void setRight(TreeNode right) {
            this.right = right;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TreeNode(String data){
            this.data =data;
            this.hash = Sha256.getSHA256(data);
            this.name = "[Node:"+data+"]";
        }

    }

    private List<TreeNode> list;
    private TreeNode root;
    public MerkleTree(List<String> contents){
        createMerkleTree(contents);
    }

    //初始化创建Merkle树
    private void createMerkleTree(List<String> contents) {
        //非空判断
        if (null == contents || contents.size() == 0)
            return;
        list = new ArrayList<>();
        //创建叶子节点
        List<TreeNode> leftList = createLeftList(contents);
        list.addAll(leftList);
        //创建父结点
        List<TreeNode> parents = createParentsList(leftList);
        list.addAll(parents);
        //循环创建各父节点直至根节点
        while (parents.size() > 1) {
            List<TreeNode> buff = createParentsList(parents);
            list.addAll(buff);
            parents = buff;
        }
        root = parents.get(0);
    }

    //构建子节点列表
    private List<TreeNode> createLeftList(List<String> contents) {
        List<TreeNode> leftList = new ArrayList<TreeNode>();
        //非空判断
        if (null == contents || contents.size() == 0)
            return leftList;
        for(String content:contents){
            TreeNode node = new TreeNode(content);
            leftList.add(node);
        }
        return leftList;
    }
    //遍历树
    public void traverseTreeNodes(){
        Collections.reverse(list);
        TreeNode root = list.get(0);
        traverseTreeNodes(root);

    }
    private void traverseTreeNodes(TreeNode node){
        System.out.println(node.getName());
        if(node.getLeft() != null){
            traverseTreeNodes(node.getLeft());
        }
        if(node.getRight() != null){
            traverseTreeNodes(node.getRight());
        }
    }

    public void setList(List<TreeNode> list) {
        this.list = list;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public List<TreeNode> getList(){
        if(list == null){
            return list;
        }
        Collections.reverse(list);
        return list;
    }

    //创建父结点列表
    private List<TreeNode> createParentsList(List<TreeNode> leftList) {
        List<TreeNode> parents = new ArrayList<TreeNode>();
        //非空校验
        if(leftList == null || leftList.size() == 0){
            return parents;
    }
        int lengeth = leftList.size();
        for(int i = 0;i<lengeth-1;i+=2){
            TreeNode parent = createParentNode(leftList.get(i),leftList.get(i+1));
            parents.add(parent);
        }
        //奇数个节点时 单独处理最后一个节点
        if(lengeth % 2 != 0){
            TreeNode parent = createParentNode(leftList.get(lengeth -1),null);
            parents.add(parent);
        }
        return parents;


}

    //创建父节点
    private TreeNode createParentNode(TreeNode left, TreeNode right) {
        TreeNode parent = new TreeNode();
        parent.setLeft(left);
        parent.setRight(right);
        //如果right为空 则父节点的hash值为left的hash值
        String hash =left.getHash();
        if(right != null){
            hash = Sha256.getSHA256(left.getHash() + right.getHash());
        }
        //hash字段和data字段同值
        parent.setData(hash);
        parent.setHash(hash);
        if(right != null){
            parent.setName("(" + left.getName() + "和" + right.getName() + " 的父节点 )");
        }else{
            parent.setName("继承节点("+left.getName() + ")成为父节点");
        }
        return parent;
    }

}
