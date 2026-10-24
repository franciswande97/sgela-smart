package com.app.sgelasmart.api.calendar.dto;

import jakarta.validation.constraints.NotBlank;

public class TimetableParseRequest {

    @NotBlank(message = "ocr_raw_text is required")
    private String ocrRawText;

    public String getOcrRawText() {
        return ocrRawText;
    }

    public void setOcrRawText(String ocrRawText) {
        this.ocrRawText = ocrRawText;
    }
}
