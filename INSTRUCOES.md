# Instruções Rápidas de Execução

## 1. Configurar Chave do Gemini

### Opção A: Variável de Ambiente (Recomendado)

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="sua-chave-aqui"
```

### Opção B: Editar application.yml

Abra `src/main/resources/application.yml` e substitua:
```yaml
api-key: ${GEMINI_API_KEY:COLOQUE_SUA_CHAVE_AQUI}
```

Por:
```yaml
api-key: ${GEMINI_API_KEY:AIzaSuaChaveAqui}
```

## 2. Executar o Projeto

**Windows PowerShell:**
```powershell
.\mvnw.cmd clean spring-boot:run
```

**Ou com Maven instalado:**
```bash
mvn clean spring-boot:run
```

## 3. Acessar a Aplicação

- **Aplicação:** http://localhost:8080

## 4. Testar

1. Acesse http://localhost:8080
2. Clique em "JOGAR AGORA"
3. Digite um apelido (mín. 3 caracteres)
4. Comece a jogar!

## Problemas Comuns

### Erro: "API Key inválida"
- Verifique se a chave do Gemini está configurada corretamente
- Obtenha uma nova chave em: https://makersuite.google.com/app/apikey

### Erro: "Porta 8080 em uso"
- Altere a porta em `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

### Erro de compilação
- Certifique-se de ter Java 21 instalado
- Execute: `mvn clean install`


