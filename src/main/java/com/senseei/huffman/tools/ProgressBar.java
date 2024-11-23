package com.senseei.huffman.tools;

public class ProgressBar {
    private final long total;
    private long current;
    private int lastPrintedPercent;
    private String context;

    public ProgressBar(int total, String context) {
        this.total = total;
        this.current = 0;
        this.lastPrintedPercent = -1;
        this.context = context;
    }

    public ProgressBar(long total, String context) {
        this.total = total;
        this.current = 0;
        this.lastPrintedPercent = -1;
        this.context = context;
    }

    public void increment() {
        current++;
        int percent = (int) ((current * 100) / total);
        if (percent != lastPrintedPercent) {
            lastPrintedPercent = percent;
            print(percent);
        }
    }

    private void print(int percent) {
        StringBuilder progressBar = new StringBuilder("\r[");
        int progress = percent / 2;
        for (int i = 0; i < 50; i++) {
            if (i < progress) {
                progressBar.append("=");
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append("] ").append(percent).append("%");
        progressBar.append(" - ").append(context);
        System.out.print(progressBar.toString());

        if (percent == 100) {
            System.out.println();
        }
    }
}