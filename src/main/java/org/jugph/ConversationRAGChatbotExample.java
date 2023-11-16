package org.jugph;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.DocumentType;
import dev.langchain4j.data.document.UrlDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;

import java.util.Arrays;

import static java.time.Duration.ofSeconds;

public class ConversationRAGChatbotExample {

    public static void main(String[] args) {

        var document = UrlDocumentLoader
                .load("https://github.com/JUGPH/.github/tree/main/profile",
                        DocumentType.HTML);

        var textExtractor = new HtmlTextExtractor();
        var transformedDocument = textExtractor.transform(document);

        EmbeddingStore<TextSegment> embeddingStore = WeaviateEmbeddingStore.builder()
                .apiKey(System.getenv("WEAVIATE_API_KEY"))
                .scheme("https")
                .host("jugph-sandbox-cluster-hbgr1t5s.weaviate.network")
                .avoidDups(true)
                .consistencyLevel("ALL")
                .build();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel
                .builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("text-embedding-ada-002")
                .build();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(1000, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(transformedDocument);

        var model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .timeout(ofSeconds(120))
                .build();

        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(model)
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
//                 .chatMemory(MessageWindowChatMemory.withMaxMessages(12))
                 .promptTemplate(PromptTemplate.from(
                         "Answer the following question to the best of your ability: {{question}}\n\n" +
                         "You can base your answer on the following information:\n{{information}}. Limit your response to 2 lines max."))
                .build();

        var userMessages = Arrays.asList(
                "Hi, I'm Jansen. Can you give me tips to become a more efficient java developer?",
                "By the way, who are the community leaders of the Java User Group Philippines?",
                "Also, can you tell me their sponsors?",
                "Is Jetbrains also a sponsor?");
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
