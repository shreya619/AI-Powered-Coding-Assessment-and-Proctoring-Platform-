package com.education.platform.dto;

public class CodeResponse {
    private String stdout;
    private String stderr;
    private int exitCode;
    private boolean success;

    public CodeResponse() {}

    public CodeResponse(String stdout, String stderr, int exitCode, boolean success) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
        this.success = success;
    }

    public String getStdout() { return stdout; }
    public void setStdout(String stdout) { this.stdout = stdout; }
    
    public String getStderr() { return stderr; }
    public void setStderr(String stderr) { this.stderr = stderr; }
    
    public int getExitCode() { return exitCode; }
    public void setExitCode(int exitCode) { this.exitCode = exitCode; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}