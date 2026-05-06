package org.example.textanalyzer.model;

/**
 * Информация об ошибке при обработке конкретного файла.
 */
public class ErrorInfo {
    private String file;
    private String message;

    public ErrorInfo() {
    }

    public ErrorInfo(String file, String message) {
        this.file = file;
        this.message = message;
    }

    public String getFile() {
        return file;
    }

    public String getMessage() {
        return message;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
