package com.senseei.huffman;

import java.io.IOException;

import com.senseei.huffman.interfaces.Compresser;
import com.senseei.huffman.tools.Huffman;

public class Main {
    private static final String OUTPUT_DIR = "output_files";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Main <compress|decompress> <input file>");
            return;
        }

        String action = args[0];
        String inputFile = args[1];
        Compresser compresser = new Huffman();

        switch (action.toLowerCase()) {
            case "compress":
                if (inputFile.endsWith(".huff")) {
                    System.out.println("Error: File already compressed...");
                }
                
                compress(inputFile, OUTPUT_DIR, compresser);
                break;
            case "decompress":

                if (!inputFile.endsWith(".huff")) {
                    System.out.println("Error: Input file must have a .huff extension for decompression.");
                }

                decompress(inputFile, OUTPUT_DIR, compresser);
                break;
            default:
                System.out.println("Usage: java Main <compress|decompress> <input file>");
                break;
        }
    }

    private static void compress(String inputFile, String outputDir, Compresser compresser) {
        try {
            compresser.compress(inputFile, outputDir);
            System.out.println("File compressed successfully.");
        } catch (IOException e) {
            System.out.println("Error during compression: " + e.getStackTrace());
        }
    }

    private static void decompress(String inputFile, String outputDir, Compresser compresser) {
        try {
            compresser.decompress(inputFile, outputDir);
            System.out.println("File decompressed successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during decompression: " + e.getStackTrace());
        }
    }
}