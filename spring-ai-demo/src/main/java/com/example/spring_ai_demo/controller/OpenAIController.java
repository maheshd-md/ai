package com.example.spring_ai_demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.ai.reader.TextReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_ai_demo.service.OpenAIService;

@RestController
@RequestMapping("openai")
public class OpenAIController {

	@Autowired
	private OpenAIService openAIService;

	@GetMapping("/generate")
	public String generate(@RequestParam("message") String message) {
		message += "\nShort answer";
		return openAIService.generate(message);
	}

	@GetMapping("/generateWithRAG")
	public String generateWith(@RequestParam("message") String message) {
		message += "\nShort answer";
		return openAIService.generateWithRAG(message);
	}

	@GetMapping("/generateWithMemory")
	public Map<String, String> generateWithMemory(
			@RequestParam(required = false, defaultValue = "", value = "sessionId") String sessionId,
			@RequestParam("message") String message) {
		message += "\nShort answer";
		return openAIService.generateWithMemory(sessionId, message);
	}

	@PostMapping("/file")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {

		return openAIService.uploadFile(file);
	}
}
