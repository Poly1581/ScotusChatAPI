package com.example.demo;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.github.cdimascio.dotenv.Dotenv;
import io.grpc.internal.JsonUtil;
import org.codelibs.jhighlight.fastutil.Hash;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;

import dev.langchain4j.model.openai.OpenAiChatModel;

@RestController
public class BookController {

  private static interface Assistant {
    String chat(String userMessage);
  }

  private final String apiKey = Dotenv.configure().load().get("OPENAI_API_KEY");

  private final BookRepository bookRepository;

  private final Map<String, Map<String, Assistant>> assistants = new HashMap<>();

  public BookController(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
    System.out.println(apiKey);
  }

  @GetMapping("/getYears")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public List<Integer> getYears() {
    File[] directories = new File("embeddings").listFiles(File::isDirectory);
    List<Integer> years = new ArrayList<>();
    for(File directory : directories) {
      years.add(Integer.parseInt(directory.getName()));
    }
    return years;
  }

  @GetMapping("/getCases")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public List<String> getCases(@RequestParam int year) {
    System.out.println("embeddings/" + year);
    File[] embeddings = new File("embeddings/" + year).listFiles();
    List<String> caseNames = new ArrayList<>();
    for(File embedding : embeddings) {
      String name = embedding.getName();
      caseNames.add(name.substring(0,name.indexOf(".store")));
    }
    return caseNames;
  }

  @GetMapping("/chat")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public String chat(@RequestParam String userName, @RequestParam int year, @RequestParam String caseName, @RequestParam String userMessage) {
    try {
      Map<String, Assistant> userAssistants = assistants.get(userName);
      if (userAssistants == null) {
        assistants.put(userName, new HashMap<>());
        userAssistants = assistants.get(userName);
      }
      Assistant caseAssistant = userAssistants.get(caseName);
      if (caseAssistant == null) {
        InMemoryEmbeddingStore<TextSegment> embedding = InMemoryEmbeddingStore.fromFile("embeddings/" + year + "/" + caseName + ".store");
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embedding))
                .build();
        userAssistants.put(caseName, assistant);
        caseAssistant = userAssistants.get(caseName);
      }
      return caseAssistant.chat(userMessage);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return "Error getting file.";
    }
  }
}