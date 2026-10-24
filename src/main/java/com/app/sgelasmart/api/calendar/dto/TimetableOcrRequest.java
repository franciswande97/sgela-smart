package com.app.sgelasmart.api.calendar.dto;

import jakarta.validation.constraints.NotBlank;

public class TimetableOcrRequest {

    @NotBlank(message = "OCR raw text is required")
    private String ocrRawText;

    public TimetableOcrRequest() {}

    public TimetableOcrRequest(String ocrRawText) {
        this.ocrRawText = ocrRawText;
    }

    public String getOcrRawText() {
        return ocrRawText;
    }

    public void setOcrRawText(String ocrRawText) {
        this.ocrRawText = ocrRawText;
    }
}
