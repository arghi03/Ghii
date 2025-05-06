import javax.swing.*;

public class Login extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private UserDAO userDAO;

    public Login() {
        userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        loginButton.addActionListener(e -> {
            User user = userDAO.login(emailField.getText(), new String(passwordField.getPassword()));
            if (user != null) {
                System.out.println("Login berhasil untuk email: " + user.getEmail() + ", isVerified: " + user.isVerified());
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
                System.out.println("Login gagal untuk email: " + emailField.getText());
                JOptionPane.showMessageDialog(this, "Login gagal!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            System.out.println("Navigasi ke halaman Register");
            new Register().setVisible(true);
            dispose();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}