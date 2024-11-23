package com.senseei.huffman.tools.huffman;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.senseei.huffman.entities.HuffmanTreeNode;
import com.senseei.huffman.tools.ProgressBar;
import com.senseei.huffman.utils.Constants;

public class HuffmanFileHandler {
    public HuffmanTreeNode[] getBytesFrequencies(String file) throws IOException {
        long totalBytes = Files.size(Paths.get(file));
        ProgressBar progressBar = new ProgressBar(totalBytes, "Counting bytes frequencies...");

        HuffmanTreeNode[] frequencies = new HuffmanTreeNode[256];
        byte buffer[] = new byte[Constants.READ_BUFFER_SIZE];

        int bytesRead;
        try (FileInputStream fis = new FileInputStream(file)) {
            while ((bytesRead = fis.read(buffer)) != -1) {
                progressBar.increment(bytesRead);
                for (int i = 0; i < bytesRead; i++) {
                    char b = (char) (buffer[i] & 0xFF);
                
                    if (frequencies[b] == null){
                        frequencies[b] = new HuffmanTreeNode(1, b);
                    } else {
                        frequencies[b].incrementFrequency();
                    }
                }
            }
        }
        
        return frequencies;
    }
}