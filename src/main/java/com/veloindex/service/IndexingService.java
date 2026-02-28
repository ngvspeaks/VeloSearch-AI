package com.veloindex.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);
    private static final Pattern FRAME_NUMBER_PATTERN = Pattern.compile("frame-(\\d+)\\.jpg");

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public IndexingService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    /**
     * Indexes all frames in the provided directory.
     *
     * @param framesDir The directory containing extracted frames.
     * @throws IOException If an I/O error occurs.
     */
    public void indexFrames(Path framesDir) throws IOException {
        log.info("Starting indexing of frames in directory: {}", framesDir);

        try (Stream<Path> paths = Files.walk(framesDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jpg"))
                    .forEach(this::processAndIndexFrame);
        }

        log.info("Indexing completed for directory: {}", framesDir);
    }

    private void processAndIndexFrame(Path framePath) {
        log.info("Processing frame: {}", framePath.getFileName());

        try {
            // 1. Prepare the multimodal message
            Resource imageResource = new FileSystemResource(framePath);
            Media media = new Media(MimeTypeUtils.IMAGE_JPEG, imageResource);

            String userPrompt = "Describe the action in this cricket frame specifically. Is it a six, wicket, or boundary? Return a short 1-sentence description.";
            UserMessage userMessage = new UserMessage(userPrompt, List.of(media));

            // 2. Call Ollama with llama3.2-vision
            ChatResponse response = chatModel.call(new Prompt(List.of(userMessage)));
            String description = response.getResult().getOutput().getContent();

            log.info("Description for {}: {}", framePath.getFileName(), description);

            // 3. Extract timestamp/frame index from filename
            long timestampSeconds = extractTimestamp(framePath.getFileName().toString());

            // 4. Save to Vector Store (embeddings are generated automatically if not
            // provided)
            Document doc = new Document(description, Map.of(
                    "timestamp", timestampSeconds,
                    "filename", framePath.getFileName().toString()));

            vectorStore.add(List.of(doc));
            log.info("Indexed frame {} at {}s", framePath.getFileName(), timestampSeconds);

        } catch (Exception e) {
            log.error("Failed to process frame {}: {}", framePath.getFileName(), e.getMessage(), e);
        }
    }

    private long extractTimestamp(String fileName) {
        Matcher matcher = FRAME_NUMBER_PATTERN.matcher(fileName);
        if (matcher.find()) {
            // Assuming 1 frame every 2 seconds as per VideoProcessingService requirement
            int frameIndex = Integer.parseInt(matcher.group(1));
            return (long) frameIndex * 2;
        }
        return 0;
    }
}
