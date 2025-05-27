import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Login extends JFrame {
    private JTextField nameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private UserDAO userDAO;

    public Login() {
        userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Masuk ke Akun");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 58, 138));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        nameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = createStyledButton("Masuk", new Color(30, 58, 138));
        registerButton = createStyledButton("Daftar", new Color(76, 175, 80));

        styleInput(nameField, "Nama Lengkap");
        styleInput(passwordField, "Password");

        panel.add(nameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(registerButton);

        add(panel);

        loginButton.addActionListener(e -> {
            User user = userDAO.login(nameField.getText(), new String(passwordField.getPassword()));
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
                JOptionPane.showMessageDialog(this, "Login gagal!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            System.out.println("Navigasi ke halaman Register");
            new Register().setVisible(true);
            dispose();
        });
    }

    private void styleInput(JComponent component, String placeholder) {
        component.setFont(new Font("Arial", Font.PLAIN, 14));
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        component.setMaximumSize(new Dimension(300, 40));
        component.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setText(placeholder);
            textField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent evt) {
                    if (textField.getText().equals(placeholder)) {
                        textField.setText("");
                    }
                }
                public void focusLost(FocusEvent evt) {
                    if (textField.getText().isEmpty()) {
                        textField.setText(placeholder);
                    }
                }
            });
        } else if (component instanceof JPasswordField) {
            JPasswordField passwordField = (JPasswordField) component;
            passwordField.setEchoChar((char) 0); // Tampilkan placeholder sebagai teks biasa
            passwordField.setText(placeholder);
            passwordField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent evt) {
                    if (new String(passwordField.getPassword()).equals(placeholder)) {
                        passwordField.setText("");
                        passwordField.setEchoChar('•'); // Kembali ke mode password
                    }
                }
                public void focusLost(FocusEvent evt) {
                    if (new String(passwordField.getPassword()).isEmpty()) {
                        passwordField.setEchoChar((char) 0);
                        passwordField.setText(placeholder);
                    }
                }
            });
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
            login.setVisible(true);
        });
    }
}