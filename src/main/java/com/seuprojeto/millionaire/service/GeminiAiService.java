package com.seuprojeto.millionaire.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seuprojeto.millionaire.dto.GeminiResponseDTO;
import com.seuprojeto.millionaire.model.Question;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAiService {
    
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    @Value("${spring.ai.google.gemini.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.google.gemini.model:gemini-2.5-flash}")
    private String model;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s";
    
    // Cache manual para evitar custos desnecessários
    private final ConcurrentHashMap<Long, GeminiResponseDTO> responseCache = new ConcurrentHashMap<>();
    
    @Retry(name = "geminiApi", fallbackMethod = "fallbackResponse")
    @Cacheable(value = "geminiResponses", key = "#question.id")
    public GeminiResponseDTO analyzeQuestion(Question question) {
        log.info("Chamando Gemini para analisar questão ID: {}", question.getId());
        log.info("API Key configurada: {}", apiKey != null && !apiKey.isEmpty() ? "SIM (tamanho: " + apiKey.length() + ")" : "NÃO");
        log.info("Modelo configurado: {}", model);
        
        // Verificar cache primeiro
        if (responseCache.containsKey(question.getId())) {
            log.info("Resposta encontrada em cache para questão ID: {}", question.getId());
            return responseCache.get(question.getId());
        }
        
        String systemPrompt = """
            Você é um assistente especialista no jogo "Quem Quer Ser Milionário?".
            Sua função é analisar perguntas de múltipla escolha e fornecer a resposta correta
            com um percentual de confiança e uma breve explicação.
            
            REGRAS IMPORTANTES:
            1. Responda APENAS no formato JSON especificado
            2. O campo "letter" deve ser APENAS uma letra: A, B, C ou D
            3. O campo "answer" deve conter o texto da alternativa correta
            4. O campo "confidence" deve ser um número entre 1 e 100
            5. O campo "explanation" deve ter no máximo 150 caracteres
            6. NÃO adicione texto fora do JSON
            7. Seja preciso e confiável
            """;
        
        String userPrompt = String.format("""
            Analise a seguinte pergunta e alternativas:
            
            PERGUNTA: %s
            
            A) %s
            B) %s
            C) %s
            D) %s
            
            Responda APENAS no formato JSON:
            {
              "letter": "X",
              "answer": "texto da resposta correta",
              "confidence": 95,
              "explanation": "breve explicação de até 150 caracteres"
            }
            """,
            question.getQuestionText(),
            question.getOptionA(),
            question.getOptionB(),
            question.getOptionC(),
            question.getOptionD()
        );
        
        try {
            // Construir requisição para API do Gemini
            // Combinar system prompt e user prompt em uma única mensagem
            String fullPrompt = systemPrompt + "\n\n" + userPrompt;
            
            Map<String, Object> requestBody = new HashMap<>();
            
            // Conteúdo da mensagem (formato simplificado que funciona melhor)
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", fullPrompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            // Adicionar configurações de geração
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 500);
            requestBody.put("generationConfig", generationConfig);
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Validar API key antes de fazer a requisição
            if (apiKey == null || apiKey.isEmpty()) {
                log.error("API Key não configurada!");
                throw new RuntimeException("API Key do Gemini não está configurada. Configure a variável GEMINI_API_KEY ou atualize application.yml");
            }
            
            // Verificar se está usando a chave padrão (que pode estar inválida)
            String defaultKey = "AIzaSyCgCcAidTAMenQhEaKnl5GrVZ_GvVYMLDw";
            if (apiKey.equals(defaultKey)) {
                log.warn("Usando chave da API padrão. Esta chave pode estar inválida ou expirada.");
                log.warn("Para obter uma nova chave, acesse: https://makersuite.google.com/app/apikey");
            }
            
            if (apiKey.length() < 30) {
                log.warn("API Key parece inválida (muito curta). Tamanho: {}", apiKey.length());
            }
            
            // Fazer chamada à API
            String url = String.format(GEMINI_API_URL, model, apiKey);
            log.info("=== CHAMANDO API GEMINI ===");
            log.info("Modelo: {}", model);
            log.info("URL: {}", url.replace(apiKey, "***API_KEY***"));
            log.info("API Key length: {}", apiKey.length());
            log.info("Request body: {}", objectMapper.writeValueAsString(requestBody));
            
            ResponseEntity<Map<String, Object>> response;
            try {
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> tempResponse = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
                );
                response = tempResponse;
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                log.error("=== ERRO HTTP CLIENT (4xx) ===");
                log.error("Status: {}", e.getStatusCode());
                log.error("Status Text: {}", e.getStatusText());
                log.error("Response Body: {}", e.getResponseBodyAsString());
                log.error("Headers: {}", e.getResponseHeaders());
                throw new RuntimeException("Erro na API Gemini (HTTP " + e.getStatusCode() + "): " + 
                    (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage()), e);
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                log.error("=== ERRO HTTP SERVER (5xx) ===");
                log.error("Status: {}", e.getStatusCode());
                log.error("Response Body: {}", e.getResponseBodyAsString());
                throw new RuntimeException("Erro no servidor Gemini (HTTP " + e.getStatusCode() + "): " + 
                    (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage()), e);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                log.error("=== ERRO DE CONEXÃO ===");
                log.error("Mensagem: {}", e.getMessage());
                log.error("Causa: {}", e.getCause() != null ? e.getCause().getMessage() : "N/A");
                throw new RuntimeException("Erro de conexão com a API Gemini: " + e.getMessage() + 
                    ". Verifique sua conexão com a internet.", e);
            } catch (Exception e) {
                log.error("=== ERRO INESPERADO ===");
                log.error("Tipo: {}", e.getClass().getName());
                log.error("Mensagem: {}", e.getMessage());
                log.error("Stack trace:", e);
                throw new RuntimeException("Erro inesperado ao chamar API Gemini: " + e.getMessage(), e);
            }
            
            // Verificar status da resposta
            log.info("Response Status: {}", response.getStatusCode());
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Erro na resposta da API Gemini. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Erro na API Gemini: " + response.getStatusCode());
            }
            
            // Extrair resposta
            Map<String, Object> responseBody = response.getBody();
            log.info("Response body recebido. Keys: {}", responseBody != null ? responseBody.keySet() : "null");
            log.debug("Response body completo: {}", responseBody);
            
            if (responseBody == null) {
                log.error("Response body é null");
                throw new RuntimeException("Resposta vazia da API Gemini");
            }
            
            // Verificar se há erro na resposta
            if (responseBody.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
                String errorMessage = error != null ? error.toString() : "Erro desconhecido";
                log.error("Erro retornado pela API Gemini: {}", errorMessage);
                throw new RuntimeException("Erro da API Gemini: " + errorMessage);
            }
            
            if (!responseBody.containsKey("candidates")) {
                log.error("Resposta não contém 'candidates'. Keys: {}", responseBody.keySet());
                throw new RuntimeException("Resposta inválida da API Gemini - não contém 'candidates'");
            }
            
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.error("Lista de candidates vazia ou null");
                throw new RuntimeException("Nenhum candidato retornado pela API");
            }
            
            Map<String, Object> candidate = candidates.get(0);
            
            // Verificar se há bloqueio de segurança
            if (candidate.containsKey("finishReason") && 
                "SAFETY".equals(candidate.get("finishReason"))) {
                log.warn("Resposta bloqueada por segurança");
                throw new RuntimeException("Resposta bloqueada por filtros de segurança");
            }
            
            Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
            if (contentMap == null) {
                log.error("Content é null no candidate");
                throw new RuntimeException("Content não encontrado na resposta");
            }
            
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
            if (parts == null || parts.isEmpty()) {
                log.error("Parts é null ou vazio");
                throw new RuntimeException("Parts não encontrado na resposta");
            }
            
            String responseText = (String) parts.get(0).get("text");
            if (responseText == null || responseText.trim().isEmpty()) {
                log.error("Texto da resposta é null ou vazio");
                throw new RuntimeException("Texto da resposta não encontrado");
            }
            
            log.debug("Resposta bruta do Gemini: {}", responseText);
            
            // Limpar possíveis markdown
            responseText = responseText.replaceAll("```json\\n?", "")
                                     .replaceAll("```\\n?", "")
                                     .trim();
            
            // Parse do JSON
            GeminiResponseDTO dto = objectMapper.readValue(responseText, GeminiResponseDTO.class);
            
            // Validações
            if (!isValidLetter(dto.getLetter())) {
                throw new IllegalArgumentException("Letra inválida retornada: " + dto.getLetter());
            }
            
            if (dto.getConfidence() < 1 || dto.getConfidence() > 100) {
                dto.setConfidence(Math.max(1, Math.min(100, dto.getConfidence())));
            }
            
            // Armazenar em cache
            responseCache.put(question.getId(), dto);
            
            log.info("Resposta processada com sucesso. Confiança: {}%", dto.getConfidence());
            return dto;
            
        } catch (RuntimeException e) {
            // Re-throw RuntimeExceptions (já foram logadas acima)
            throw e;
        } catch (Exception e) {
            log.error("=== ERRO GERAL AO PROCESSAR RESPOSTA ===");
            log.error("Tipo: {}", e.getClass().getName());
            log.error("Mensagem: {}", e.getMessage());
            log.error("Stack trace completo:", e);
            throw new RuntimeException("Falha na comunicação com IA: " + e.getMessage(), e);
        }
    }
    
    private boolean isValidLetter(String letter) {
        return letter != null && letter.matches("[A-Da-d]");
    }
    
    // Fallback se a API falhar após retries
    public GeminiResponseDTO fallbackResponse(Question question, Throwable t) {
        log.warn("Fallback ativado para questão ID: {}. Erro: {}", question.getId(), t.getMessage());
        return GeminiResponseDTO.builder()
            .letter("?")
            .answer("Não disponível")
            .confidence(0)
            .explanation("A IA está temporariamente indisponível. Confie no seu conhecimento!")
            .build();
    }
}
