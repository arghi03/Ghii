import javax.swing.*;
import java.awt.*;


public class Login extends JFrame {
    private JTextField nameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private UserDAO userDAO;

    public Login() {

        userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Login Aplikasi Perpustakaan");
        setSize(400, 300); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLocationRelativeTo(null);
        setResizable(false);

        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL; 

        // Judul
        JLabel titleLabel = new JLabel("Masuk ke Akun");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 58, 138));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; 
        gbc.insets = new Insets(5, 5, 20, 5); 
        panel.add(titleLabel, gbc);

        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Field Nama Lengkap
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nama Lengkap:"), gbc);
        nameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Field Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        passwordField.setEchoChar('•'); 
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false); // Transparan

        loginButton = createStyledButton("Masuk", new Color(30, 58, 138));
        registerButton = createStyledButton("Daftar", new Color(76, 175, 80));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5); 
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);

        loginButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (name.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama dan Password harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = userDAO.login(name, password);
            if (user != null) { 
                System.out.println("Login berhasil untuk nama: " + user.getNama() + ", isVerified: " + user.isVerified());
                JOptionPane.showMessageDialog(this, "Login berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                
                if (user.isVerified()) {
                    System.out.println("User sudah terverifikasi, navigasi ke Dashboard");
                    new Dashboard(user.getNama(), user.getEmail(), user.getIdRole()).setVisible(true);
                } else { 
                    System.out.println("User belum terverifikasi, navigasi ke VerificationScreen");
                    String otpCode = EmailService.sendOTP(user.getEmail()); 
                    if (otpCode != null) {
                        userDAO.saveOtp(user, otpCode);
                        new VerificationScreen(user).setVisible(true); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal mengirim OTP! Cek koneksi atau konfigurasi email.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                dispose();  
            } else {
               
                System.out.println("Login gagal untuk nama: " + nameField.getText());
                JOptionPane.showMessageDialog(this, "Login gagal! Nama atau password salah, atau akun Anda tidak aktif.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            System.out.println("Navigasi ke halaman Register");
            new Register().setVisible(true);
            dispose();
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 35));
        
        
        button.setOpaque(true);
        button.setBorderPainted(false);

        return button;
    }

    public static void main(String[] args) {
       
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}
