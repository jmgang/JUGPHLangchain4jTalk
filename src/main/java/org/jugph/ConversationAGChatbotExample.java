package org.jugph;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;

import java.util.Arrays;
import java.util.Collections;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static java.time.Duration.ofSeconds;

public class ConversationAGChatbotExample {

    public static void main(String[] args) {

        var document = UrlDocumentLoader
                .load("https://www.eac.edu.ph/research-and-development-office/", new TextDocumentParser());

        var textExtractor = new HtmlTextExtractor();
        var transformedDocument = textExtractor.transform(document);

        //System.out.println(transformedDocument);

        Prompt systemMessagePrompt = PromptTemplate.from("Answer the following questions to the best of your ability.\n\n" +
                "You can base your answer on the following information:\n{{information}}. Limit your response to 2 lines max.")
                .apply(Collections.singletonMap("information", transformedDocument));

        var model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .timeout(ofSeconds(120))
                .build();

        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(5);
        memory.add(systemMessage(systemMessagePrompt.text()));

        ConversationalChain chain = ConversationalChain.builder()
                .chatLanguageModel(model)
                .chatMemory(memory)
                .build();

        var userMessages = Arrays.asList(
                "What does the Research and Development Office of EAC-Cavite Campus aim to do?",
                "Name at least 3 forged linkages of the institution",
                "What are some services offered by the RDO of EAC-Cavite?",
                "What is the specific address of the Cavite campus of EAC?");
        var output = new StringBuilder();

        System.out.println("============================================");
        for (String userMessage : userMessages) {
            output.append("\n[USER]: ")
                    .append(userMessage)
                    .append("\n[LLM]: ")
                    .append(chain.execute(userMessage))
                    .append("\n");
        }

        System.out.println(output);
    }
}
