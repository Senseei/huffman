package com.senseei.huffman.tools.huffman;

import java.util.Queue;
import com.senseei.huffman.entities.HuffmanTree;
import com.senseei.huffman.entities.HuffmanTreeNode;

public class HuffmanTreeBuilder {
    public HuffmanTree buildTree(Queue<HuffmanTreeNode> queue) {
        if (queue.size() == 1) {
            HuffmanTreeNode node = queue.poll();
            HuffmanTreeNode parent = new HuffmanTreeNode(node.getFrequency(), node, new HuffmanTreeNode(0));
            return new HuffmanTree(parent);
        }

        while (queue.size() > 1) {
            HuffmanTreeNode left = queue.poll();
            HuffmanTreeNode right = queue.poll();
            HuffmanTreeNode parent = new HuffmanTreeNode(left.getFrequency() + right.getFrequency(), left, right);
            queue.add(parent);
        }

        return new HuffmanTree(queue.poll());
    }
}