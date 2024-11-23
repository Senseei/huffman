package com.senseei.huffman.interfaces;

import java.io.IOException;

public interface Compresser {
    void decompress(String file, String outputDir) throws IOException, ClassNotFoundException;
    void compress(String file, String outputDir) throws IOException;
}
