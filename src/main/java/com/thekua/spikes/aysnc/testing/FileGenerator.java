package com.thekua.spikes.aysnc.testing;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A class that simluates a process that produces an output file after a certain amount of time
 */
public class FileGenerator {
    private final String fileName;
    private int timeToGenerate;
    private final String contents;

    public FileGenerator(String fileName, int timeToGenerate, String contents) {
        this.fileName = fileName;
        this.timeToGenerate = timeToGenerate;
        this.contents = contents;
    }

    public void generate() {
        try {
            // simulate the process that makes the file to sort
            Thread.sleep(timeToGenerate * 1000);

            Files.createDirectories(Paths.get(fileName).getParent());
            PrintWriter printWriter = new PrintWriter(fileName);
            printWriter.print(contents);
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
