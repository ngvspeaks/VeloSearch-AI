package com.veloindex.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Service
public class VideoProcessingService {

    private static final Logger log = LoggerFactory.getLogger(VideoProcessingService.class);
    private Path tempDir;

    /**
     * Extracts frames from a video file located in /src/main/resources/input.
     * Extracts 1 frame every 2 seconds.
     *
     * @param videoFileName The name of the video file in the input directory.
     * @return The path to the temporary directory containing the jpg frames.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the process is interrupted.
     */
    public Path extractFrames(String videoFileName) throws IOException, InterruptedException {
        // Create temp directory for each extraction session or reuse
        if (tempDir == null || !Files.exists(tempDir)) {
            tempDir = Files.createTempDirectory("veloindex-frames-");
            log.info("Created temporary directory for frames: {}", tempDir);
        }

        // Locate input file in src/main/resources/input
        Path inputPath = Paths.get("src", "main", "resources", "input", videoFileName);
        if (!Files.exists(inputPath)) {
            // Check absolute path as well in case CWD is different
            inputPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "input", videoFileName);
        }

        if (!Files.exists(inputPath)) {
            throw new IOException("Input video file not found at: " + inputPath.toAbsolutePath());
        }

        String outputPathPattern = tempDir.resolve("frame-%04d.jpg").toString();

        log.info("Executing FFmpeg to extract frames from {}...", inputPath.getFileName());

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", inputPath.toAbsolutePath().toString(),
                "-vf", "fps=1/2",
                "-y", // Overwrite output files
                outputPathPattern);

        // Redirect error stream to catch FFmpeg output in logs if needed
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Wait for extraction to complete
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("FFmpeg process failed with exit code " + exitCode);
        }

        log.info("Frames extracted successfully to {}", tempDir.toAbsolutePath());
        return tempDir;
    }

    /**
     * Cleans up the temporary directory and all extracted frames.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void cleanupFrames() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            log.info("Cleaning up temporary directory: {}", tempDir.toAbsolutePath());
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            tempDir = null;
        }
    }
}
