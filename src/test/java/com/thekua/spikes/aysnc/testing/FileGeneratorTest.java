package com.thekua.spikes.aysnc.testing;

import com.thekua.spikes.aysnc.testing.FileGenerator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class FileGeneratorTest {

    private static final String RESULT_FILE = "target/test/resultFile.txt";
    private static final String STEP_1_LOG = "target/test/step1.log";
    private static final String STEP_2_LOG = "target/test/step2.log";
    private static final String STEP_3_LOG = "target/test/step3.log";

    private static final List<String> FILES_TO_CLEAN_UP = Arrays.asList(STEP_1_LOG, STEP_2_LOG, STEP_3_LOG, RESULT_FILE);


    @Before
    public void setUp() {
        for (String fileToCleanUp : FILES_TO_CLEAN_UP) {
            File file = new File(fileToCleanUp);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    @Test
    public void shouldWaitForAFileToBeCreated() throws Exception {
        // Given I have an aysnc process to run
        String expectedFile = RESULT_FILE;

        List<FileGenerator> fileGenerators = Arrays.asList(
                new FileGenerator(STEP_1_LOG, 1, "Step 1 is complete"),
                new FileGenerator(STEP_2_LOG, 3, "Step 2 is complete"),
                new FileGenerator(STEP_3_LOG, 4, "Step 3 is complete"),
                new FileGenerator(expectedFile, 7, "Process is now complete")
        );

        // when it is busy doing its work
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (final FileGenerator fileGenerator : fileGenerators) {
            executorService.execute(new Runnable() {
                public void run() {
                    fileGenerator.generate();
                }
            });
        }

        // then I get some log outputs
        await().atMost(2, SECONDS).until(testFileFound(STEP_1_LOG));
        await().until(testFileFound(STEP_2_LOG));
        await().until(testFileFound(STEP_3_LOG));

        // and I should have my final result with the output I expect
        await().atMost(10, SECONDS).until(testFileFound(expectedFile));
        String fileContents = readFile(expectedFile);
        assertThat(fileContents, startsWith("Process"));

        // Cleanup
        executorService.shutdown();
    }

    private String readFile(String expectedFile) throws IOException {
        return new String(Files.readAllBytes(Paths.get(expectedFile)));

    }


    private Callable<Boolean> testFileFound(final String file) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return new File(file).exists();
            }
        };
    }
}