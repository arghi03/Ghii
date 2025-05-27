import java.time.LocalDateTime;

public class Loan {
    private int idLoan;
    private int idUser;
    private int idBook;
    private String status;
    private int approvedBy;
    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
    private LocalDateTime returnDate; // Tambah field returnDate
    private String username; // Nama user yang meminjam
    private String bookTitle; // Judul buku yang dipinjam

    public Loan(int idLoan, int idUser, int idBook, String status, int approvedBy) {
        this.idLoan = idLoan;
        this.idUser = idUser;
        this.idBook = idBook;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public Loan(int idLoan, int idUser, int idBook, String status, int approvedBy, LocalDateTime requestDate, LocalDateTime approvedDate, LocalDateTime returnDate, String username, String bookTitle) {
        this.idLoan = idLoan;
        this.idUser = idUser;
        this.idBook = idBook;
        this.status = status;
        this.approvedBy = approvedBy;
        this.requestDate = requestDate;
        this.approvedDate = approvedDate;
        this.returnDate = returnDate;
        this.username = username;
        this.bookTitle = bookTitle;
    }

    public int getIdLoan() { return idLoan; }
    public void setIdLoan(int idLoan) { this.idLoan = idLoan; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public int getIdBook() { return idBook; }
    public void setIdBook(int idBook) { this.idBook = idBook; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getApprovedBy() { return approvedBy; }
    public void setApprovedBy(int approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }
    public LocalDateTime getReturnDate() { return returnDate; } // Getter returnDate
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; } // Setter returnDate
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
}