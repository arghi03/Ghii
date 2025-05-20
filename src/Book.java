public class Book {
    private int idBook;
    private String title;
    private String author;
    private String coverImagePath; // Tambah field untuk path gambar sampul
    private String bookFilePath;   // Tambah field untuk path file PDF

    public Book(int idBook, String title, String author) {
        this.idBook = idBook;
        this.title = title;
        this.author = author;
    }

    public Book(int idBook, String title, String author, String coverImagePath, String bookFilePath) {
        this.idBook = idBook;
        this.title = title;
        this.author = author;
        this.coverImagePath = coverImagePath;
        this.bookFilePath = bookFilePath;
    }

    // Getters dan Setters
    public int getIdBook() {
        return idBook;
    }

    public void setIdBook(int idBook) {
        this.idBook = idBook;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public String getBookFilePath() {
        return bookFilePath;
    }

    public void setBookFilePath(String bookFilePath) {
        this.bookFilePath = bookFilePath;
    }
}