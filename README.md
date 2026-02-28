# VeloIndex 🚀

**VeloIndex** is a multimodal video search engine that allows you to search through video content using natural language and jump directly to specified moments.

![VeloIndex Demo](https://raw.githubusercontent.com/your-username/velo-index/main/docs/demo.gif) *(Replace with your actual demo GIF)*

## 🌟 Key Features
- **Multimodal AI Analysis**: Uses **Ollama (Llama 3.2 Vision)** to analyze and describe individual video frames.
- **Semantic Search**: Powered by **Spring AI** and **PGVector (PostgreSQL)** to perform high-dimensional vector searches on video content.
- **Automatic Seeking**: A custom web interface that instantly jumps the video player to the most relevant timestamp based on your search query.
- **Local & Private**: All AI processing (embeddings and vision) runs locally via Ollama.

## 🛠️ Tech Stack
- **Backend**: Java 23, Spring Boot 3.4
- **AI Framework**: Spring AI
- **LLM/Vision**: Ollama (Llama 3.2 & Llama 3.2 Vision)
- **Database**: PostgreSQL with PGVector
- **Processing**: FFmpeg for frame extraction
- **Frontend**: Thymeleaf, Vanilla CSS, HTML5 Video

## 🚀 Getting Started

### Prerequisites
1. **Ollama**: Install and pull the required models:
   ```bash
   ollama pull llama3.2
   ollama pull llama3.2-vision
   ```
2. **FFmpeg**: Ensure `ffmpeg` is installed and available in your system PATH.
3. **Docker**: For running PostgreSQL with PGVector.

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/velo-index.git
   cd velo-index
   ```
2. Start the database:
   ```bash
   docker compose up -d
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## 📝 How it Works
1. **Extraction**: The `VideoProcessingService` uses FFmpeg to extract frames from the input video every 2 seconds.
2. **Indexing**: `IndexingService` sends these frames to **Llama 3.2 Vision** to generate text descriptions (e.g., "A bunny peeks out from its hole").
3. **Vectorization**: Descriptions are converted into vector embeddings and stored in **PGVector**.
4. **Search**: When you search (e.g., "show me the butterflies"), the query is embedded and compared against the stored vectors.
5. **Seek**: The UI receives the top matching timestamps and uses JavaScript to jump the video player to that exact time.

## ⚖️ License & Attribution
- This project is open-source.
- **Video Credit**: The demo uses "Big Buck Bunny", (c) copyright 2008, Blender Foundation | [www.bigbuckbunny.org](http://www.bigbuckbunny.org) released under CC BY 3.0.

---
Built with ❤️ by [Your Name]
