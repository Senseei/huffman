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
import com.senseei.huffman.utils.Constants;

public class Huffman implements Compresser {
    private HuffmanTreeBuilder treeBuilder = new HuffmanTreeBuilder();
    private HuffmanTableBuilder tableBuilder = new HuffmanTableBuilder();
    private HuffmanFileHandler fileHandler = new HuffmanFileHandler();

    public void decompress(String file, String outputDir) throws IOException, ClassNotFoundException {
        HuffmanTree tree;
    
        long totalBytes = Files.size(Paths.get(file));
        ProgressBar progressBar = new ProgressBar(totalBytes, "Decompressing...");
    
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
    
            tree = (HuffmanTree) ois.readObject();
    
            String extension = tree.getOriginalExtension();
            String fileName = Paths.get(file).getFileName().toString();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    
            Path outputPath = Paths.get(outputDir, fileName, fileName + "." + extension);
            Files.createDirectories(outputPath.getParent());
    
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
    
                HuffmanTreeNode current = tree.getRoot();
    
                byte buffer[] = new byte[Constants.READ_BUFFER_SIZE];
                byte outputBuffer[] = new byte[Constants.WRITE_BUFFER_SIZE];
                int outputBufferPos = 0;
                int validBits = 8;
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    progressBar.increment(bytesRead);
                    for (int j = 0; j < bytesRead; j++) {
                        char b = (char) (buffer[j] & 0xFF);
    
                        if (fis.available() == 1 && j == bytesRead - 1) {
                            validBits = fis.read();
                        }
    
                        for (int i = 7; i >= 8 - validBits; i--) {
                            if (current.isLeaf()) {
                                outputBuffer[outputBufferPos++] = (byte) current.getCharacter();
                                if (outputBufferPos == outputBuffer.length) {
                                    fos.write(outputBuffer, 0, outputBufferPos);
                                    outputBufferPos = 0;
                                }
                                current = tree.getRoot();
                            }
    
                            int bit = (b >> i) & 1;
                            current = bit == 0 ? current.getLeft() : current.getRight();
                        }
                    }
                }
    
                if (current != null && current.isLeaf()) {
                    outputBuffer[outputBufferPos++] = (byte) current.getCharacter();
                }

                if (outputBufferPos > 0) {
                    fos.write(outputBuffer, 0, outputBufferPos);
                }
                progressBar.finish();
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
        
            byte buffer[] = new byte[Constants.READ_BUFFER_SIZE];
            byte outputBuffer[] = new byte[Constants.WRITE_BUFFER_SIZE];
            int outputBufferPos = 0;
            int currentByte = 0;
            int bitPosition = 7;
        
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                progressBar.increment(bytesRead);
                for (int i = 0; i < bytesRead; i++) {
                    char b = (char) (buffer[i] & 0xFF);
                    String code = codeTable[b];
        
                    for (int j = 0; j < code.length(); j++) {
                        int bit = code.charAt(j) - '0';
                        currentByte |= bit << bitPosition;
                        bitPosition--;
        
                        if (bitPosition < 0) {
                            outputBuffer[outputBufferPos++] = (byte) currentByte;
                            if (outputBufferPos == outputBuffer.length) {
                                fos.write(outputBuffer, 0, outputBufferPos);
                                outputBufferPos = 0;
                            }
                            currentByte = 0;
                            bitPosition = 7;
                        }
                    }
                }
            }
        
            if (bitPosition < 7) {
                outputBuffer[outputBufferPos++] = (byte) currentByte;
            }
            if (outputBufferPos > 0) {
                fos.write(outputBuffer, 0, outputBufferPos);
            }
            int validBits = 7 - bitPosition == 0 ? 8 : 7 - bitPosition;
            fos.write(validBits);
        }
    }
}