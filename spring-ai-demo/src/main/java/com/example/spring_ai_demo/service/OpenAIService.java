package com.example.spring_ai_demo.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;

import io.qdrant.client.QdrantClient;

@Service
public class OpenAIService {

	@Autowired
	private ChatModel chatModel;

	private ChatClient chatClient;

	@Autowired
	private VectorStore vectorStore;

	@Value("classpath:/INDvsPAK_highlights.txt")
	private Resource resource;
	
	private static final int MAX_TOKENS = 2048;

	@Autowired
	private QdrantClient qdrantClient;

	@Autowired
	public OpenAIService(ChatClient.Builder builder, VectorStore vectorStore) {
		this.chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
				new QuestionAnswerAdvisor(vectorStore)).build();
	}

//	@PostConstruct
//	public void loadVectorStore() throws InterruptedException, ExecutionException {
//
//		int count = qdrantClient.countAsync("vector_store").get().intValue();
//		System.out.println("Count of documents in vector_store collection: " + count);
////		List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("India").topK(5).build());
////		if(results == null || results.isEmpty()) {			
//		if (0 == count) {
//			TextReader reader = new TextReader(resource);
//			reader.getCustomMetadata().put("filename", "INDvsPAK_highlights.txt");
//			List<Document> documents = reader.get();
//			System.out.println(documents);
//			vectorStore.add(documents);
//		}
//
//	}

	public String generate(String message) {

		ChatResponse response = chatModel.call(new Prompt(message, OpenAiChatOptions.builder()
//                              .model("o1-mini")
				.temperature(0.5).build()));

		return response.getResult().getOutput().getText();
	}

	public String generateWithRAG(String message) {

		ChatClientRequestSpec requestSpecs = chatClient.prompt(new Prompt(message));
		return requestSpecs.call().content();
	}

	public Map<String, String> generateWithMemory(String sessionId, String message) {

		if (sessionId == null || sessionId.isEmpty()) {
			sessionId = UUID.randomUUID().toString();
		}

		final String session = sessionId;
		ChatClientRequestSpec requestSpecs = chatClient.prompt(new Prompt(message)).advisors(a -> {
			a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, session);
			a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100);
		});

		Map<String, String> response = new HashMap<>();
		response.put("sessionId", sessionId);
		response.put("content", requestSpecs.call().content());
		return response;
	}

	public ResponseEntity<String> uploadFile(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (isFileAlreadyUploaded(filename)) {
                return ResponseEntity.status(409).body("File already uploaded: " + filename);
            }

            if (filename.endsWith(".zip")) {
                handleZipFile(file);
            } else {
                handleFile(file, filename);
            }

            return ResponseEntity.ok("File uploaded successfully: " + filename);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    private void handleZipFile(MultipartFile file) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String filename = zipEntry.getName();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                String content = reader.lines().collect(Collectors.joining("\n"));
                List<String> chunks = chunkContent(content, MAX_TOKENS);
                for (String chunk : chunks) {
                    System.out.println("Uploading chunk: " + chunk);
                    Resource resource = stringToResource(chunk);
                    TextReader textReader = new TextReader(resource);
                    textReader.getCustomMetadata().put("filename", filename);
                    List<Document> documents = textReader.get();
                    vectorStore.add(documents);
                }
            }
        }
    }

    private void handleFile(MultipartFile file, String filename) throws Exception {
        String content;
        if (filename.endsWith(".docx")) {
            content = readDocxFile(file);
        } else if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
            content = readXlsFile(file);
        } else if (filename.endsWith(".xml")) {
            content = readXmlFile(file);
        } else if (filename.endsWith(".pdf")) {
            content = readPdfFile(file);
        } else if (filename.endsWith(".java")) {
            content = readJavaFile(file);
        } else {
            content = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        }

        List<String> chunks = chunkContent(content, MAX_TOKENS);
        for (String chunk : chunks) {
            System.out.println("Uploading chunk: " + chunk);
            Resource resource = stringToResource(chunk);
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put("filename", filename);
            List<Document> documents = reader.get();
            vectorStore.add(documents);
        }
    }
	
	private String readJavaFile(MultipartFile file) throws Exception {
	    return new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
	            .lines().collect(Collectors.joining("\n"));
	}

	private String readXlsFile(MultipartFile file) throws Exception {
		StringBuilder content = new StringBuilder();
		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		for (Sheet sheet : workbook) {
			for (Row row : sheet) {
				for (Cell cell : row) {
					content.append(cell.toString()).append(" ");
				}
				content.append("\n");
			}
		}
		workbook.close();
		return content.toString();
	}

	private String readXmlFile(MultipartFile file) throws Exception {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    org.w3c.dom.Document document = builder.parse(new InputSource(file.getInputStream()));
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    StringWriter writer = new StringWriter();
	    transformer.transform(new DOMSource(document), new StreamResult(writer));
	    return writer.toString();
	}


	private String readPdfFile(MultipartFile file) throws Exception {
		PDDocument document = PDDocument.load(file.getInputStream());
		PDFTextStripper pdfStripper = new PDFTextStripper();
		String content = pdfStripper.getText(document);
		document.close();
		return content;
	}

	private String readDocxFile(MultipartFile file) throws Exception {
	    StringBuilder content = new StringBuilder();
	    try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
	        for (XWPFParagraph paragraph : doc.getParagraphs()) {
	            content.append(paragraph.getText()).append("\n");
	        }
	    }
	    return content.toString();
	}
	
	private Resource stringToResource(String content) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return new InputStreamResource(byteArrayInputStream);
    }
	
	private List<String> chunkContent(String content, int maxTokens) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(content.length(), start + maxTokens);
            chunks.add(content.substring(start, end));
            start = end;
        }
        return chunks;
    }

	private boolean isFileAlreadyUploaded(String filename) {
		List<Document> existingDocuments = vectorStore
				.similaritySearch(SearchRequest.builder().query(filename).build());
		for (Document doc : existingDocuments) {
			Map<String, Object> metadata = doc.getMetadata();
			if (filename.equals(metadata.get("filename"))) {
				return true;
			}
		}
		return false;
	}
}
