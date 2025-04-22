import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {
    public Login() {
        setTitle("Login");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel namaLabel = new JLabel("Nama:");
        JTextField namaField = new JTextField(20);
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(namaLabel);
        panel.add(namaField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginBtn);
        panel.add(registerBtn);

        add(panel);

        loginBtn.addActionListener(e -> {
            String nama = namaField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                System.out.println("Validasi gagal: Field wajib kosong");
                JOptionPane.showMessageDialog(this, "Nama, Email, dan Password harus diisi!");
                return;
            }

            System.out.println("Mencoba login: " + nama + ", " + email);
            boolean success = UserDAO.login(nama, email, password);
            if (success) {
                System.out.println("Login sukses untuk " + nama);
                JOptionPane.showMessageDialog(this, "Login berhasil!");
                new Dashboard(nama, email).setVisible(true);
                dispose();
            } else {
                System.out.println("Login gagal untuk " + nama);
                JOptionPane.showMessageDialog(this, "Nama, Email, atau Password salah.");
            }
        });

        registerBtn.addActionListener(e -> {
            System.out.println("Pindah ke form registrasi");
            new Register().setVisible(true);
            dispose();
        });
    }
}