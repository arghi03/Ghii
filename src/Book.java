public class Book {
    private int idBook;
    private String title;
    private String author;
    private String isbn;
    private String classificationCode; // ✅ FIELD BARU
    private String coverImagePath;  
    private String bookFilePath;    
    private float rating;           

    // ✅ SEMUA KONSTRUKTOR DIPERBARUI
    public Book(int idBook, String title, String author, String isbn, String classificationCode) {
        this.idBook = idBook;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.classificationCode = classificationCode;
    }

    public Book(int idBook, String title, String author, String isbn, String classificationCode, String coverImagePath, String bookFilePath) {
        this(idBook, title, author, isbn, classificationCode); // Memanggil konstruktor di atas
        this.coverImagePath = coverImagePath;
        this.bookFilePath = bookFilePath;
    }

    public Book(int idBook, String title, String author, String isbn, String classificationCode, String coverImagePath, String bookFilePath, float rating) {
        this(idBook, title, author, isbn, classificationCode, coverImagePath, bookFilePath); // Memanggil konstruktor di atas
        this.rating = rating;
    }

    // Getters dan Setters
    public int getIdBook() { return idBook; }
    public void setIdBook(int idBook) { this.idBook = idBook; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    // ✅ GETTER & SETTER BARU
    public String getClassificationCode() { return classificationCode; }
    public void setClassificationCode(String classificationCode) { this.classificationCode = classificationCode; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }
    public String getBookFilePath() { return bookFilePath; }
    public void setBookFilePath(String bookFilePath) { this.bookFilePath = bookFilePath; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}