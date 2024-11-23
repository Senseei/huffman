package com.senseei.huffman.entities;

import java.io.Serializable;

public class HuffmanTree implements Serializable {
    private static final long serialVersionUID = 1L;
    private HuffmanTreeNode root;
    private String originalExtension;

    public HuffmanTree(HuffmanTreeNode root) {
        this.root = root;
    }

    public HuffmanTreeNode getRoot() {
        return root;
    }

    public void setRoot(HuffmanTreeNode root) {
        this.root = root;
    }

    public String getOriginalExtension() {
        return originalExtension;
    }

    public void setOriginalExtension(String originalExtension) {
        this.originalExtension = originalExtension;
    }

    public int getDepth() {
        return getDepth(root);
    }

    private int getDepth(HuffmanTreeNode node) {
        if (node == null) {
            return 0;
        }

        return 1 + Math.max(getDepth(node.getLeft()), getDepth(node.getRight()));
    }

    public int getTotalNodes() {
        return getTotalNodes(root);
    }

    private int getTotalNodes(HuffmanTreeNode node) {
        if (node == null) {
            return 0;
        }

        return 1 + getTotalNodes(node.getLeft()) + getTotalNodes(node.getRight());
    }

    @Override
    public String toString() {
        return printTree(root, 0);
    }

    private String printTree(HuffmanTreeNode node, int level) {
        if (node == null) {
            return "";
        }

        String result = "";
        result += printTree(node.getRight(), level + 1);
        for (int i = 0; i < level; i++) {
            result += "  ";
        }
        result += node.toString() + "\n";
        result += printTree(node.getLeft(), level + 1);

        return result;
    }
}
