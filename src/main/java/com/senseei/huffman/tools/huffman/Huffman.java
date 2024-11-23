package com.senseei.huffman.tools.huffman;

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
import com.senseei.huffman.tools.ProgressBar;

public class Huffman implements Compresser {
    private HuffmanTreeBuilder treeBuilder = new HuffmanTreeBuilder();
    private HuffmanTableBuilder tableBuilder = new HuffmanTableBuilder();
    private HuffmanFileHandler fileHandler = new HuffmanFileHandler();

    public void decompress(String file, String outputDir) throws IOException, ClassNotFoundException {
        HuffmanTree tree;

        long totalBytes = Files.size(Paths.get(file));
        ProgressBar progressBar = new ProgressBar(totalBytes, "Decompressing...");

        try(FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            tree = (HuffmanTree) ois.readObject();
            
            String extension = tree.getOriginalExtension();
            String fileName = Paths.get(file).getFileName().toString();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            
            Path outputPath = Paths.get(outputDir, fileName, fileName + "." + extension);
            Files.createDirectories(outputPath.getParent());

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

                HuffmanTreeNode current = tree.getRoot();
    
                byte buffer[] = new byte[1024]; // Increase buffer size to 1024 bytes
                int validBits = 8;
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    for (int j = 0; j < bytesRead; j++) {
                        progressBar.increment();
                        char b = (char) (buffer[j] & 0xFF);

                        if (fis.available() == 1 && j == bytesRead - 1) {
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

        HuffmanTreeNode[] frequencies = fileHandler.getBytesFrequencies(file);
        
        Queue<HuffmanTreeNode> queue = new PriorityQueue<>((a, b) -> a.getFrequency() - b.getFrequency());
        
        for (HuffmanTreeNode node : frequencies) {
            if (node != null) {
                queue.add(node);
            }
        }
        
        HuffmanTree tree = treeBuilder.buildTree(queue);
        
        String codeTable[] = tableBuilder.loadTable(tree.getRoot());
        
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
}