package org.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.w3c.dom.Text;

import java.io.File;

public class Main {

    public static File getDirectory(String name) {
        File directory = new File(name);
        if(!directory.exists()) {
            System.out.println("Making directory " + name + ".");
            directory.mkdir();
        }
        return directory;
    }

    public static void main(String[] args) {
        File transcriptsDirectory = getDirectory("transcripts");
        File embeddingsDirectory = getDirectory("embeddings");
        for(File yearTranscriptsDirectory : transcriptsDirectory.listFiles(File::isDirectory)) {
            System.out.println("Year: " + yearTranscriptsDirectory.getName());
            File yearEmbeddingsDirectory = getDirectory(embeddingsDirectory.getName() + "/" + yearTranscriptsDirectory.getName());
            for(File transcript : yearTranscriptsDirectory.listFiles(File::isFile)) {
                System.out.println("\tCase: " + transcript.getName());
                Document document = FileSystemDocumentLoader.loadDocument(transcript.getPath());
                InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
                EmbeddingStoreIngestor.ingest(document, embeddingStore);
                embeddingStore.serializeToFile(yearEmbeddingsDirectory.getPath() + "/" + transcript.getName().replace(".pdf", ".store"));
            }
        }
    }
}