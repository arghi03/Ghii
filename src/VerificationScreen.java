import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class VerificationScreen extends JFrame {
    private User user;

    public VerificationScreen(User user) {
        this.user = user;

        setTitle("Verifikasi Email");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Masukkan OTP untuk verifikasi email: " + user.getEmail());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField otpField = new JTextField(10);
        otpField.setMaximumSize(new Dimension(200, 30));
        otpField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton verifyButton = new JButton("Verifikasi");
        verifyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(otpField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(verifyButton);

        add(panel);

        verifyButton.addActionListener(e -> {
            String otpInput = otpField.getText().trim();
            if (otpInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan kode OTP!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
                JOptionPane.showMessageDialog(this, "Kode OTP tidak ditemukan! Silakan login ulang.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
                JOptionPane.showMessageDialog(this, "Kode OTP sudah kadaluarsa! Silakan login ulang untuk mendapatkan kode baru.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!otpInput.equals(user.getOtpCode())) {
                JOptionPane.showMessageDialog(this, "Kode OTP salah!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserDAO userDAO = new UserDAO(DBConnection.getConnection());
            user.setVerified(true);
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            boolean success = userDAO.updateUser(user);
            if (success) {
                JOptionPane.showMessageDialog(this, "Verifikasi berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                new Dashboard(user.getNama(), user.getEmail(), user.getIdRole()).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Verifikasi gagal!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}