package org.jugph;

import com.google.gson.Gson;
import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class RAGWithJson {

    public static void main(String[] args) {
        String filePath = "D:\\code\\JUGPHLangchain4jTalk\\src\\main\\resources\\sample.json";
        try {
            // Read the content of the file into a String
            String json = new String(Files.readAllBytes(Paths.get(filePath)));

            Gson gson = new Gson();
            SegmentsDTO segmentsDTO = gson.fromJson(json, SegmentsDTO.class);
            var azulIndexSegments = segmentsDTO.segments();
            List<TextSegment> textSegments = new ArrayList<>();

            for(var azulIndexSegment : azulIndexSegments) {
                Map<String, String> metadataMap = new HashMap<>();
                metadataMap.put("OBJECT_ID", azulIndexSegment.objectID());
                metadataMap.put("LINK", azulIndexSegment.link());
                textSegments.add(TextSegment.from(azulIndexSegment.content(),
                        Metadata.from(metadataMap)));
            }

            var embeddingModel = OpenAiEmbeddingModel
                    .builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("text-embedding-ada-002")
                    .build();

            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
            List<Embedding> embeddings = embeddingModel.embedAll(textSegments).content();
            embeddingStore.addAll(embeddings, textSegments);

            // Specify the question you want to ask the model
            String question = "What is Azul Zulu?";

            // Embed the question
            Embedding questionEmbedding = embeddingModel.embed(question).content();

            // Find relevant embeddings in embedding store by semantic similarity
            int maxResults = 2;
            List<EmbeddingMatch<TextSegment>> relevantEmbeddings
                    = embeddingStore.findRelevant(questionEmbedding, maxResults);

            // Create a prompt for the model that includes question and relevant embeddings
            PromptTemplate promptTemplate = PromptTemplate.from(
                    "Answer the following question to the best of your ability, also show the appropriate " +
                            "link in your answer.:\n"
                            + "\n"
                            + "Question:\n"
                            + "{{question}}\n"
                            + "\n"
                            + "Base your answer on the following information:\n"
                            + "{{information}}");

            String information = relevantEmbeddings.stream()
                    .map(match -> match.embedded().text() + ". Link: " + match.embedded().metadata("LINK"))
                    .collect(joining("\n\n\n"));

            Map<String, Object> variables = new HashMap<>();
            variables.put("question", question);
            variables.put("information", information);

            Prompt prompt = promptTemplate.apply(variables);

//            System.out.println(prompt.text());

//
            // Send the prompt to the OpenAI chat model
            ChatLanguageModel chatModel = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .timeout(Duration.ofSeconds(120))
                    .build();
            AiMessage aiMessage = chatModel.generate(prompt.toUserMessage()).content();

            // See an answer from the model
            String answer = aiMessage.text();
            System.out.println(answer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
