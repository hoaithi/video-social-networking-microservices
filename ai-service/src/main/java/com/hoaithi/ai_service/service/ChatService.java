package com.hoaithi.ai_service.service;

import ch.qos.logback.core.net.server.Client;
import com.hoaithi.ai_service.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }
    /**
     * Processes a chat request and returns the response.
     *
     * @param request the chat request containing the message
     * @return the response message
     */
    public String chat(ChatRequest request) {
        String systemPrompt = """
            Bạn là một chuyên gia marketing video.
            Nhiệm vụ của bạn: tạo ra chính xác 5 tiêu đề ngắn gọn, hấp dẫn cho video.
            Trả về kết quả dạng danh sách (mỗi tiêu đề trên một dòng).
            """;

        PromptTemplate template = new PromptTemplate(
                "{system}\n\nMô tả video: {description}\n\nHãy sinh 5 tiêu đề:"
        );

        Prompt prompt = template.create(Map.of(
                "system", systemPrompt,
                "description", request
        ));

        return chatClient.prompt(prompt).call().content();
    }
}
