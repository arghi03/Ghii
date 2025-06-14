import javax.swing.*;
import java.awt.*;
// Hapus import FocusAdapter dan FocusEvent karena tidak diperlukan lagi
// import java.awt.event.FocusAdapter;
// import java.awt.event.FocusEvent;

// Pastikan User, UserDAO, DBConnection diimport jika berada di package berbeda
// import com.perpustakaan.model.User;
// import com.perpustakaan.dao.UserDAO;
// import com.perpustakaan.util.DBConnection;

public class Register extends JFrame {
    private JTextField nameField, emailField, nimField, phoneField;
    private JPasswordField passwordField;
    private JButton registerButton, backButton;
    private UserDAO userDAO;

    public Register() {
        userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Register");
        setSize(400, 450); // Sesuaikan tinggi form untuk layout baru
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // --- PERUBAHAN UTAMA: MENGGUNAKAN GridBagLayout untuk UI yang lebih rapi ---
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Jarak antar komponen
        gbc.fill = GridBagConstraints.HORIZONTAL; // Komponen mengisi ruang horizontal

        // Judul
        JLabel titleLabel = new JLabel("Daftar Akun Baru");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 58, 138));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span 2 kolom
        gbc.insets = new Insets(5, 5, 20, 5); // Margin bawah lebih besar
        panel.add(titleLabel, gbc);

        // Reset GridBagConstraints
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Field Nama
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nama Lengkap:"), gbc);
        nameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        // Field Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        // Field NIM
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("NIM:"), gbc);
        nimField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nimField, gbc);

        // Field Nomor Telepon
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Nomor Telepon:"), gbc);
        phoneField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        // Field Password
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        passwordField.setEchoChar('•'); // Langsung set karakter sensor
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Panel untuk tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false); // Transparan

        registerButton = createStyledButton("Daftar", new Color(76, 175, 80));
        backButton = createStyledButton("Kembali", new Color(107, 114, 128));
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5); // Margin atas
        gbc.fill = GridBagConstraints.NONE; // Tombol tidak perlu stretch
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);

        // Action Listener untuk Tombol Daftar
        registerButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String nim = nimField.getText().trim(); 
            String phone = phoneField.getText().trim(); 
            String password = new String(passwordField.getPassword()).trim();

            // Validasi input tidak kosong (logika placeholder tidak diperlukan lagi)
            if (name.isEmpty() || email.isEmpty() || nim.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newId;
            try {
                newId = userDAO.getNewUserId();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal generate ID user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            User user = new User(
                newId,                // idUser
                name,                 // nama
                nim,                  // nim
                email,                // email
                phone,                // nomorTelepon
                password,             // password
                3,                    // idRole (default user)
                false,                // isVerified
                true                  // isActive
            );

            boolean success = userDAO.register(user);
            if (success) {
                JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.");
                dispose();
                new Login().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Registrasi gagal! Email atau NIM mungkin sudah terdaftar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });
    }

    // Method styleInput tidak diperlukan lagi, kita gunakan styling langsung atau helper yang lebih sederhana
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 35)); // Sedikit sesuaikan ukuran
        
        // --- PERBAIKAN DI SINI ---
        // Baris ini memaksa tombol untuk menggambar background-nya, 
        // sehingga warna yang kita set akan tampil di semua sistem operasi.
        button.setOpaque(true);
        // Baris ini membuat tampilan lebih modern tanpa border default tombol.
        button.setBorderPainted(false);

        return button;
    }

    public static void main(String[] args) {
        // Gunakan Look and Feel sistem untuk tampilan yang lebih modern
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Register register = new Register();
            register.setVisible(true);
        });
    }
}
