# Comandos para Windows PowerShell

## Importante

No Windows PowerShell, use os seguintes comandos:

### Executar o Projeto

```powershell
# Opção 1: Usar Maven Wrapper (recomendado)
.\mvnw.cmd clean spring-boot:run

# Opção 2: Se tiver Maven instalado
mvn clean spring-boot:run
```

### Executar Testes

```powershell
# Todos os testes
.\mvnw.cmd test

# Teste específico
.\mvnw.cmd test -Dtest=GameServiceTest

# Com cobertura (se configurado)
.\mvnw.cmd test jacoco:report
```

### Compilar o Projeto

```powershell
.\mvnw.cmd clean compile
```

### Instalar Dependências

```powershell
.\mvnw.cmd clean install
```

### Limpar e Recompilar

```powershell
.\mvnw.cmd clean package
```

## Primeira Execução

Na primeira vez que executar `.\mvnw.cmd`, o Maven Wrapper irá:
1. Baixar automaticamente o Maven (se necessário)
2. Baixar o maven-wrapper.jar
3. Executar o comando solicitado

Isso pode levar alguns minutos na primeira execução.

## Problemas Comuns

### Erro: "JAVA_HOME não encontrado"

Configure a variável JAVA_HOME:

```powershell
# Verificar se Java está instalado
java -version

# Configurar JAVA_HOME (ajuste o caminho)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Ou configurar permanentemente
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-21', 'User')
```

### Erro: "Maven não encontrado"

Se não tiver Maven instalado, use o Maven Wrapper (`.\mvnw.cmd`) que baixa automaticamente.

### Erro: "Porta 8080 em uso"

Altere a porta no `application.yml` ou mate o processo:

```powershell
# Encontrar processo na porta 8080
netstat -ano | findstr :8080

# Matar processo (substitua PID pelo número encontrado)
taskkill /PID <PID> /F
```

## Notas

- Use `.\mvnw.cmd` (com ponto e barra invertida) no PowerShell
- Use `./mvnw` (com barra normal) no Git Bash ou Linux/Mac
- O Maven Wrapper garante que todos usem a mesma versão do Maven

