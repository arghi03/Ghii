import javax.swing.*;
import java.awt.*;

public class Register extends JFrame {
    public Register() {
        setTitle("Register");
        setSize(300, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField namaField = new JTextField(20);
        JTextField nimField = new JTextField(20);
        JTextField telpField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JButton regBtn = new JButton("Register");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Nama:"));
        panel.add(namaField);
        panel.add(new JLabel("NIM:"));
        panel.add(nimField);
        panel.add(new JLabel("No Telp:"));
        panel.add(telpField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(regBtn);

        add(panel);

        regBtn.addActionListener(e -> {
            String nama = namaField.getText().trim();
            String nim = nimField.getText().trim();
            String telp = telpField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());

            // ✅ FIX: role di-set otomatis ke "user"
            String role = "user";

            if (nama.isEmpty() || nim.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                System.out.println("Validasi gagal: Field wajib kosong");
                JOptionPane.showMessageDialog(this, "Nama, NIM, Email, dan Password harus diisi!");
                return;
            }

            System.out.println("Mencoba registrasi: " + nama + ", " + email + ", role: " + role);
            User newUser = new User(nama, nim, telp, email, pass, role);
            boolean success = UserDAO.register(newUser);
            if (success) {
                System.out.println("Registrasi sukses untuk " + nama);
                JOptionPane.showMessageDialog(this, "Registrasi berhasil!");
                new Dashboard(nama, email).setVisible(true);
                dispose();
            } else {
                System.out.println("Registrasi gagal untuk " + nama);
                JOptionPane.showMessageDialog(this, "Registrasi gagal. Coba lagi.");
            }
        });
    }
}
