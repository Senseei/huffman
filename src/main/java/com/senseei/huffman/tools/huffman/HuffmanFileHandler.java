package com.senseei.huffman.tools.huffman;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.senseei.huffman.entities.HuffmanTreeNode;
import com.senseei.huffman.tools.ProgressBar;

public class HuffmanFileHandler {
    public HuffmanTreeNode[] getBytesFrequencies(String file) throws IOException {
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