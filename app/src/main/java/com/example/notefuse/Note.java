package com.example.notefuse;

public class Note {
    private final long id;
    private final String title;
    private final String content;
    private final long createdAt;
    private final long expiryAt;

    public Note(long id, String title, String content, long createdAt, long expiryAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.expiryAt = expiryAt;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiryAt() { return expiryAt; }
}
