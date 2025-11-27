package com.seuprojeto.millionaire.model;

public enum Lifeline {
    SKIP("Pular"),
    FIFTY_FIFTY("50:50"),
    AI_HELP("Ajuda da IA"),
    NONE("Nenhum");
    
    private final String description;
    
    Lifeline(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

