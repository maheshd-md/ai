package com.example.spring_ai_demo.config;

import java.io.File;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.annotation.PostConstruct;

@Configuration
public class Config {

	@Value("${spring.ai.vectorstore.qdrant.api-key}")
	String qdrantAPIKey;
	
	@Value("${spring.ai.vectorstore.qdrant.host}")
	String qdrantHost;
	
	@Value("${spring.ai.vectorstore.qdrant.port}")
	String qdrantPort;
	
	@Value("${spring.ai.vectorstore.collection-name}")
	String collectionName;
	
//	@Value("classpath:/INDvsPAK_highlights.txt")
//	private Resource resource;

	@Bean
	public QdrantClient qdrantClient() {
	    QdrantGrpcClient qdrantGrpcClient = QdrantGrpcClient.newBuilder(qdrantHost).withApiKey(qdrantAPIKey).build();
	    return new QdrantClient(qdrantGrpcClient);
	}
	
	@Bean
	public QdrantVectorStore vectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
		return QdrantVectorStore.builder(qdrantClient, embeddingModel).collectionName(collectionName).build();
	}

//	@Bean
//	public SimpleVectorStore getVectorStore(EmbeddingModel embeddingModel) {
//		SimpleVectorStore vectoreStore = SimpleVectorStore.builder(embeddingModel).build();
//		File vectorStoreFile = new File("src/main/resources/vector_store_IndVsPak.json");
//		if (vectorStoreFile.exists()) {
//			System.out.println("Loaded vector store file");
//		} else {
//			TextReader reader = new TextReader(resource);
//			reader.getCustomMetadata().put("filename", "INDvsPAK_highlights.txt");
//			List<Document> docs = reader.get();
//			TextSplitter splitter = new TokenTextSplitter();
//			List<Document> splitDocs = splitter.apply(docs);
//			for (Document document : splitDocs) {
//				vectoreStore.add(List.of(document));
//				vectoreStore.save(vectorStoreFile);
//			}
//		}
//		return vectoreStore;
//	}
}
