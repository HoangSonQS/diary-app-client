package com.mydiary.diaryappclient.model.dto;

// DTO này không cần response, chỉ cần dùng để gửi đi
public class EntryRequest {
    private String title;
    private String content;

    public EntryRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}