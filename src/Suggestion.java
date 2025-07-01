import java.time.LocalDateTime;

public class Suggestion {
    private int id;
    private int userId;
    private String username;  
    private String title;
    private String author;
    private String notes;
    private String status;
    private LocalDateTime createdAt;

    // Konstruktor untuk membuat saran baru dari UI
    public Suggestion(int userId, String title, String author, String notes) {
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.notes = notes;
    }

    // Konstruktor lengkap untuk mengambil data dari DB
    public Suggestion(int id, int userId, String username, String title, String author, String notes, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.title = title;
        this.author = author;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}