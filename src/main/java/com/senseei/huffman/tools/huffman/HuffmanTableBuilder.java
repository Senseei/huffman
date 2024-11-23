package com.senseei.huffman.tools.huffman;

import com.senseei.huffman.entities.HuffmanTreeNode;

public class HuffmanTableBuilder {
    public String[] loadTable(HuffmanTreeNode root) {
        String[] codeTable = new String[256];
        loadTable(root, codeTable, "");
        return codeTable;
    }

    private void loadTable(HuffmanTreeNode node, String[] codeTable, String code) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            codeTable[node.getCharacter()] = code;
            return;
        }

        loadTable(node.getLeft(), codeTable, code + "0");
        loadTable(node.getRight(), codeTable, code + "1");
    }
}