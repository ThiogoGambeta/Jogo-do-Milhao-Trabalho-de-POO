### Quem Quer Ser Milionário?

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M4-blue)
![Gemini](https://img.shields.io/badge/Gemini-AI-yellow)
![License](https://img.shields.io/badge/License-MIT-green)

### Sobre o Projeto

O Jogo é uma aplicação web inspirada no programa de TV do SBT quem quer ser um milionario.

O grande diferencial do projeto é o uso da **API Gemini do Google** como "ajudante inteligente", permitindo que jogadores solicitem análises de perguntas com percentual de confiança e explicações geradas por IA.

---

### Funcionalidades Principais

### Gameplay

- 15 níveis de perguntas com dificuldade progressiva
- Perguntas categorizadas (História, Geografia, Ciências, etc.)
- Timer de 30 segundos por pergunta com feedback visual
- Animações fluidas
- Sistema de prêmios idêntico ao programa original

### Coringas

- **Pular:** 3 usos para descartar pergunta atual
- **50:50:** 1 uso para eliminar 2 alternativas erradas
- 🤖 **Ajuda da IA:** 1 uso para consultar Gemini AI

### Integração com IA

- Análise inteligente de perguntas via **Gemini 2.0 Flash**
- Percentual de confiança da IA (1-100%)
- Explicações curtas e educativas
- Sistema de retry com fallback para garantir disponibilidade
- Cache de respostas para otimizar custos

### Ranking

- Top 10 maiores prêmios conquistados
- Data e hora dos jogos
- Destaque para vitórias de 1 milhão

---

## Design e UX

O projeto reproduz fielmente o visual do programa do SBT (temporada 2024-2025):

- **Fundo:** Gradiente radial azul-marinho com partículas douradas animadas
- **Holofotes:** Animação de rotação contínua no background
- **Logo:** Tipografia Bebas Neue com efeito de brilho dourado pulsante
- **Pergunta:** Card centralizado com círculo dourado pulsando
- **Alternativas:** Placas com efeito hover de "levantar" e animações de acerto/erro
- **Pirâmide:** 15 níveis com acendimento progressivo
- **Timer:** Círculo animado com mudança de cor em urgência (<10s)
- **Confetti:** Celebração visual na vitória do milhão

### Cores Oficiais

```css
Azul Marinho:  #001f3f
Dourado:       #FFD700
Verde Acerto:  #00ff00
Vermelho Erro: #ff0000
```

---

## Tecnologias Utilizadas

### Backend

- Java 21 (LTS)
- Spring Boot 3.3.5
- Spring MVC
- Spring Data JPA
- Spring AI 1.0.0-M4
- Spring Cache
- Hibernate 6.x
- PostgreSQL (configurado para produção)
- Resilience4j (retry e circuit breaker)
- Lombok (redução de boilerplate)

### Frontend

- Thymeleaf 3.1+ (template engine)
- Bootstrap 5.3 (framework CSS responsivo)
- JavaScript Vanilla
- Particles.js (efeitos visuais)
- Confetti.js (celebração)

### IA

- Google Gemini 2.0 Flash via Spring AI
- Modelo otimizado para custo/qualidade
- Temperatura: 0.3 (respostas precisas)
- Max tokens: 500

### Build & Deploy

- Maven 3.9+
- Spring Boot DevTools (hot-reload)



##  Como Executar

### Pré-requisitos

- Java 21 ou superior
- Maven 3.9+ (ou usar `./mvnw` incluído)
- Chave de API do Google Gemini (gratuita)

### Passo 1: Obter Chave do Gemini

1. Acesse [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Faça login com sua conta Google
3. Clique em "Get API Key" → "Create API Key"
4. Copie a chave gerada (formato: `AIza...`)

### Passo 2: Configurar Variável de Ambiente


**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="sua-chave-aqui"
```

**OU** edite `application.yml` e substitua:
```yaml
api-key: ${GEMINI_API_KEY:SUA_CHAVE_AQUI}
```

### Passo 3: Executar o Projeto

```bash
# Clone o repositório (se aplicável)
git clone https://github.com/seu-usuario/quem-quer-ser-milionario.git
cd quem-quer-ser-milionario

# Compile e execute

**Windows PowerShell:**
```powershell
.\mvnw.cmd clean spring-boot:run
```

**Ou com Maven instalado:**
```bash
mvn clean spring-boot:run
```
```

### Passo 4: Acessar a Aplicação

Abra o navegador e acesse:
- **Aplicação:** http://localhost:8080


## Executar Testes

**Windows PowerShell:**
```powershell
# Executar todos os testes
.\mvnw.cmd test

---

## Detalhes da Integração com Gemini AI

### Como Funciona

1. Jogador clica "Ajuda da IA" durante uma pergunta
2. Frontend envia requisição AJAX para `/game/ai-help`
3. Backend monta prompt estruturado:
   - **System Message:** Você é especialista no jogo...
   - **User Message:** Analise a pergunta: [texto]
     - A) [opção A]
     - B) [opção B]
     - ...
4. Spring AI chama API do Gemini com configurações:
   - Model: `gemini-2.0-flash-exp`
   - Temperature: 0.3 (precisão)
   - Max tokens: 500
   - Timeout: 10s
5. IA retorna JSON:
   ```json
   {
     "letter": "C",
     "answer": "Brasília",
     "confidence": 95,
     "explanation": "Brasília é a capital federal desde 1960"
   }
   ```
6. Backend valida e cacheia resposta
7. Frontend exibe em modal estilizado

## Licença

Este projeto é licenciado sob a MIT License. Veja o arquivo LICENSE para mais detalhes.

---

## Autores

- **Thiogo Antonio Gambeta, Arthur Quadros e Gabriel Fagundes**
- **Universidade Univille**

---

## Agradecimentos

- **Google** - Pela API do Gemini
- **Spring Team** - Pelo framework incrível
- **SBT** - Pela inspiração do programa original
- **Comunidade Open Source** - Por todas as bibliotecas utilizadas

---

## Contexto Acadêmico

Este projeto foi desenvolvido como Trabalho de Conclusão de Matéria do curso de Engenharia de SoftWare da Univille, sob orientação do professor Leanderson da matéria Programação Orientada a Objetos.

**Objetivo:** Demonstrar habilidades em desenvolvimento full-stack, integração com APIs de IA, design responsivo e arquitetura de software escalável.

---
