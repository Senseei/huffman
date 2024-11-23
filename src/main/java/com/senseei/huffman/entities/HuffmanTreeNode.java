package com.senseei.huffman.entities;

import java.io.Serializable;

public class HuffmanTreeNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private int frequency;
    private char character;
    private HuffmanTreeNode left;
    private HuffmanTreeNode right;

    public HuffmanTreeNode(int frequency) {
        this.frequency = frequency;
    }

    public HuffmanTreeNode(int frequency, char character) {
        this.frequency = frequency;
        this.character = character;
    }

    public HuffmanTreeNode(int frequency, HuffmanTreeNode left, HuffmanTreeNode right) {
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    public HuffmanTreeNode(int frequency, char character, HuffmanTreeNode left, HuffmanTreeNode right) {
        this.frequency = frequency;
        this.character = character;
        this.left = left;
        this.right = right;
    }

    public int getFrequency() {
        return frequency;
    }

    public char getCharacter() {
        return character;
    }

    public HuffmanTreeNode getLeft() {
        return left;
    }

    public HuffmanTreeNode getRight() {
        return right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public void incrementFrequency() {
        frequency++;
    }

    @Override
    public String toString() {
        return "(" + character + "|" + frequency + ")";
    }
}
