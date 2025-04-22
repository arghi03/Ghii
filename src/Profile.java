public class Profile {
    private int profileId;
    private int idUser;
    private boolean verification;
    private String nama;
    private String nim;
    private String email;
    private String nomorTelepon;
    private String createdAt;

    // Constructor
    public Profile() {}

    public Profile(int profileId, int idUser, boolean verification, String nama, String nim, String email, String nomorTelepon, String createdAt) {
        this.profileId = profileId;
        this.idUser = idUser;
        this.verification = verification;
        this.nama = nama;
        this.nim = nim;
        this.email = email;
        this.nomorTelepon = nomorTelepon;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public boolean isVerification() { return verification; }
    public void setVerification(boolean verification) { this.verification = verification; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNomorTelepon() { return nomorTelepon; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
