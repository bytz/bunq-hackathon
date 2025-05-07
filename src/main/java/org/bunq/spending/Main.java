package org.bunq.spending;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bunq.sdk.context.ApiContext;
import com.bunq.sdk.context.ApiEnvironmentType;
import com.bunq.sdk.context.BunqContext;
import com.bunq.sdk.model.generated.endpoint.InsightEventApiObject;
import com.bunq.sdk.model.generated.endpoint.PaymentApiObject;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class Main {
    public static void main(String[] args) {
        ApiContext apiContext = ApiContext.create(
            ApiEnvironmentType.SANDBOX,
            System.getenv("BUNQ_SANDBOX_API_KEY"),
            System.getenv("USER")
        );
        apiContext.save("bunq-config.conf");
        BunqContext.loadApiContext(apiContext); //load the API context to use in your app

        //PaymentApiObject.create(new AmountObject("1","EUR"), new PointerObject("EMAIL", "sugardaddy@bunq.com","Sugar Daddy"), "coffee");

        List<Document> documents = new ArrayList<>();
        documents.add(Document.from("I paid 1 EUR for coffee"));
        InsightEventApiObject.list(
            Map.of("time_start", Instant.now().minus(31, ChronoUnit.DAYS).toString(), "time_end", Instant.now().toString())
            ).getValue().forEach(insightEventApiObject -> {
                PaymentApiObject payment = insightEventApiObject.getObject().getPayment();
                documents.add(Document.from(
                    String.format("I paid %s %s for %s ", payment.getAmount().getValue() ,payment.getAmount().getCurrency(), payment.getDescription())
                ));
            });

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
            .apiKey("demo")
            .modelName(OpenAiChatModelName.GPT_4_O_MINI)
            .build();
        Assistant assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatModel) // it should use OpenAI LLM
            .contentRetriever(createContentRetriever(documents)) // it should have access to our documents
            .build();
        System.out.println(assistant.answer("What am I spending my money on?"));

        apiContext.closeSession();
        System.exit(0);
    }

    private static ContentRetriever createContentRetriever(List<Document> documents) {

        // Here, we create an empty in-memory store for our documents and their embeddings.
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // Here, we are ingesting our documents into the store.
        // Under the hood, a lot of "magic" is happening, but we can ignore it for now.
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        // Lastly, let's create a content retriever from an embedding store.
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    private interface Assistant {
        String answer(String query);
    }
}
