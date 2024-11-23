package com.senseei.huffman.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.Queue;

import com.senseei.huffman.entities.HuffmanTree;
import com.senseei.huffman.entities.HuffmanTreeNode;
import com.senseei.huffman.interfaces.Compresser;

public class Huffman implements Compresser {

    public void decompress(String file, String outputDir) throws IOException, ClassNotFoundException {
        HuffmanTree tree;

        long totalBytes = Files.size(Paths.get(file));
        ProgressBar progressBar = new ProgressBar(totalBytes, "Decompressing...");

        try(FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            tree = (HuffmanTree) ois.readObject();
            
            String extension = tree.getOriginalExtension();
            String fileName = file.substring(0, file.lastIndexOf('.'));

            Path outputPath = Paths.get(outputDir, fileName + "." + extension);
            Files.createDirectories(outputPath.getParent());

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

                HuffmanTreeNode current = tree.getRoot();
    
                byte buffer[] = new byte[1];
                
                int validBits = 8;
                while (fis.read(buffer) != -1) {
                    progressBar.increment();
                    char b = (char) (buffer[0] & 0xFF);
    
                    if (fis.available() == 1) {
                        validBits = fis.read();
                    }
                    
                    for (int i = 7; i >= 8 - validBits; i--) {
                        if (current.isLeaf()) {
                            fos.write(current.getCharacter());
                            current = tree.getRoot();
                        }
                        
                        int bit = (b >> i) & 1;
                        current = bit == 0 ? current.getLeft() : current.getRight();
                    }
                }
    
                if (current != null && current.isLeaf()) {
                    fos.write(current.getCharacter());
                }
            }
        }
    }
    
    public void compress(String file, String outputDir) throws IOException {
        long totalBytes = Files.size(Paths.get(file));
        ProgressBar progressBar = new ProgressBar(totalBytes, "Compressing...");

        HuffmanTreeNode[] frequencies = getBytesFrequencies(file);
        
        Queue<HuffmanTreeNode> queue = new PriorityQueue<>((a, b) -> a.getFrequency() - b.getFrequency());
        
        for (HuffmanTreeNode node : frequencies) {
            if (node != null) {
                queue.add(node);
            }
        }
        
        HuffmanTree tree = buildTree(queue);
        
        String codeTable[] = loadTable(tree.getRoot());
        
        int extIndex = file.lastIndexOf('.');
        String fileName = extIndex == -1 ? file : file.substring(0, extIndex);
        String extension = extIndex == -1 ? "" : file.substring(extIndex + 1);

        tree.setOriginalExtension(extension);

        Path outputPath = Paths.get(outputDir, fileName + ".huff");
        Files.createDirectories(outputPath.getParent());

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            FileInputStream fis = new FileInputStream(file)) {

            oos.writeObject(tree);

            byte buffer[] = new byte[1];
            int currentByte = 0;
            int bitPosition = 7;

            while (fis.read(buffer) != -1) {
                progressBar.increment();
                char b = (char) (buffer[0] & 0xFF);
                String code = codeTable[b];

                for (int i = 0; i < code.length(); i++) {
                    int bit = code.charAt(i) - '0';
                    currentByte |= bit << bitPosition;
                    bitPosition--;

                    if (bitPosition < 0) {
                        fos.write(currentByte);
                        currentByte = 0;
                        bitPosition = 7;
                    }
                }
            }

            if (bitPosition < 7) {
                fos.write(currentByte);
            }
            int validBits = 7 - bitPosition == 0 ? 8 : 7 - bitPosition;
            fos.write(validBits);
        }
    }

    private HuffmanTree buildTree(Queue<HuffmanTreeNode> queue) {
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

    private String[] loadTable(HuffmanTreeNode root) {
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
    
    private HuffmanTreeNode[] getBytesFrequencies(String file) throws IOException {
        long totalBytes = Files.size(Paths.get(file));
        ProgressBar progressBar = new ProgressBar(totalBytes, "Counting bytes frequencies...");

        HuffmanTreeNode[] frequencies = new HuffmanTreeNode[256];
        byte buffer[] = new byte[1];

        try (FileInputStream fis = new FileInputStream(file)) {
            while (fis.read(buffer) != -1) {
                progressBar.increment();
                char b = (char) (buffer[0] & 0xFF);
                
                if (frequencies[b] == null){
                    frequencies[b] = new HuffmanTreeNode(1, b);
                    continue;
                }
                    
                frequencies[b].incrementFrequency();
            }
        }
        
        return frequencies;
    } 

}
