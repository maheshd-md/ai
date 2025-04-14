//package com.example.spring_ai_demo.controller;
//
//import org.springframework.ai.huggingface.HuggingfaceChatModel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("hg")
//public class HuggingfaceController {
//
//    private final HuggingfaceChatModel chatModel;
//
//    @Autowired
//    public HuggingfaceController(HuggingfaceChatModel chatModel) {
//        this.chatModel = chatModel;
//    }
//
//    @GetMapping("/ai/generate")
//    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a short joke") String message) {
//        return Map.of("generation", this.chatModel.call(message));
//    }
//}