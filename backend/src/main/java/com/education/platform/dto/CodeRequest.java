package com.education.platform.dto;

public class CodeRequest {
    private String code;
    private String language;
    private String version;
    private String input;

    public CodeRequest() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
}