import java.time.LocalDateTime;

public class User {
    private int idUser;
    private String nama;
    private String nim;
    private String email;
    private String nomorTelepon;
    private String password;
    private int idRole;
    private boolean isVerified;
    private boolean isActive; // FIELD BARU
    private String otpCode;
    private LocalDateTime otpExpiry;

    // Konstruktor utama diperbarui dengan parameter isActive
    public User(int idUser, String nama, String nim, String email, String nomorTelepon, String password, int idRole, boolean isVerified, boolean isActive) {
        this.idUser = idUser;
        this.nama = nama;
        this.nim = nim;
        this.email = email;
        this.nomorTelepon = nomorTelepon;
        this.password = password;
        this.idRole = idRole;
        this.isVerified = isVerified;
        this.isActive = isActive; // Inisialisasi field baru
    }

    // Getter dan Setter

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNomorTelepon() { return nomorTelepon; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getIdRole() { return idRole; }
    public void setIdRole(int idRole) { this.idRole = idRole; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }

    // Getter dan Setter untuk isActive
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }
}
