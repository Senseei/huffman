package com.senseei.huffman.tools;

import java.time.Instant;
import java.time.Duration;

public class ProgressBar {
    private long total;
    private long current;
    private int lastPrintedPercent;
    private String context;
    private Instant startTime;

    public ProgressBar(long total, String context) {
        this.total = total;
        this.context = context;
        this.current = 0;
        this.lastPrintedPercent = -1;
        this.startTime = Instant.now();
    }

    public void increment(long bytes) {
        current += bytes;
        int percent = (int) ((current * 100) / total);
        if (percent != lastPrintedPercent) {
            lastPrintedPercent = percent;
            print(percent);
        }
    }

    public void finish() {
        print(100);
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

        Duration elapsed = Duration.between(startTime, Instant.now());
        double seconds = elapsed.toMillis() / 1000.0;
        double bytesPerSecond = current / seconds;
        progressBar.append(" - ").append(formatBytes(bytesPerSecond)).append("/sec");

        System.out.print(progressBar.toString());

        if (percent == 100) {
            System.out.println();
        }
    }

    private String formatBytes(double bytes) {
        if (bytes < 1024) {
            return String.format("%.2f B", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024 * 1024 * 1024));
        }
    }
}