package com.example.mysportik;

public class User {
    private String id;
    private String email;
    private String username;

    // Обязательный пустой конструктор для Firebase
    public User() {}

    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
