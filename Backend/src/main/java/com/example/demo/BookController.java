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
  /**
   * Basic assistant interface
   */
  private static interface Assistant {
    String chat(String userMessage);
  }

  private final String apiKey = Dotenv.configure().load().get("OPENAI_API_KEY");

  private final Map<String, Map<String, Assistant>> assistants = new HashMap<>();

  public BookController() {
    System.out.println(apiKey);
  }

  /**
   * Route to get years
   * @return the list of years for which cases are supported
   */
  @GetMapping("/getYears")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public List<Integer> getYears() {
    //Get directories within embeddings directory
    File[] embeddingsDirectory = new File("embeddings").listFiles(File::isDirectory);
    List<Integer> years = new ArrayList<>();
    //Get names of all subdirectories
    for(File yearDirectory : embeddingsDirectory) {
      years.add(Integer.parseInt(yearDirectory.getName()));
    }
    return years;
  }

  /**
   * Route to get cases for a given year
   * @param year the year whose cases are to be returned
   * @return A list of case names for a given year
   */
  @GetMapping("/getCases")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public List<String> getCases(@RequestParam int year) {
    //Get directory for given year
    File[] yearDirectory = new File("embeddings/" + year).listFiles();
    List<String> caseNames = new ArrayList<>();
    //Get all case names for the given year
    for(File embedding : yearDirectory) {
      String name = embedding.getName();
      caseNames.add(name.replace(".store",""));
    }
    return caseNames;
  }

  /**
   * Route to chat with a given case
   * @param userName name of user chatting
   * @param year year of the case
   * @param caseName name of the case
   * @param userMessage message to ask agent
   * @return response of agent
   */
  @GetMapping("/chat")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public String chat(@RequestParam String userName, @RequestParam int year, @RequestParam String caseName, @RequestParam String userMessage) {
    try {
      //Get assistants associated with the user
      Map<String, Assistant> userAssistants = assistants.get(userName);
      if (userAssistants == null) {
        //Make empty hashmap of assistants and associate it with the user
        assistants.put(userName, new HashMap<>());
        userAssistants = assistants.get(userName);
      }
      //Get assistant associated with the user and case
      Assistant caseAssistant = userAssistants.get(caseName);
      if (caseAssistant == null) {
        //Load pre-processed embedding store for the case
        InMemoryEmbeddingStore<TextSegment> embedding = InMemoryEmbeddingStore.fromFile("embeddings/" + year + "/" + caseName + ".store");
        //Create an openai chat model
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();
        //Create an assistant
        Assistant assistant = AiServices.builder(Assistant.class)
                //Link the openai chat model
                .chatLanguageModel(chatModel)
                //Add chat memory
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                //Add content retriever (rag with case information)
                .contentRetriever(EmbeddingStoreContentRetriever.from(embedding))
                //Add system message
                .systemMessageProvider(chatMemoryId -> "You are a constitutional law professor explaining supreme court cases. Respond concisely, correctly, and accurately.")
                .build();
        //Associate assistant with user and case
        userAssistants.put(caseName, assistant);
        caseAssistant = userAssistants.get(caseName);
      }
      //Prompt assistant with userMessage and return response
      return caseAssistant.chat(userMessage);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return "Error getting file.";
    }
  }
}