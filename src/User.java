public class User {
    private int idUser;
    private String nama;
    private String nim;
    private String nomorTelepon;
    private String email;
    private String password;
    private String role;

    public User(String nama, String nim, String nomorTelepon, String email, String password, String role) {
        this.nama = nama;
        this.nim = nim;
        this.nomorTelepon = nomorTelepon;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(int idUser, String nama, String nim, String nomorTelepon, String email, String password, String role) {
        this.idUser = idUser;
        this.nama = nama;
        this.nim = nim;
        this.nomorTelepon = nomorTelepon;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getter methods
    public int getIdUser() { return idUser; }
    public String getNama() { return nama; }
    public String getNim() { return nim; }
    public String getNomorTelepon() { return nomorTelepon; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    // Setter methods
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public void setNama(String nama) { this.nama = nama; }
    public void setNim(String nim) { this.nim = nim; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}