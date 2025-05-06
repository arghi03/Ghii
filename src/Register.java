import javax.swing.*;

public class Register extends JFrame {
    private UserDAO userDAO;
    private JTextField namaField, nimField, emailField, nomorTeleponField;
    private JPasswordField passwordField;
    private JButton registerButton;

    public Register() {
        userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Register");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        namaField = new JTextField(20);
        nimField = new JTextField(20);
        emailField = new JTextField(20);
        nomorTeleponField = new JTextField(20);
        passwordField = new JPasswordField(20);
        registerButton = new JButton("Register");

        panel.add(new JLabel("Nama:"));
        panel.add(namaField);
        panel.add(new JLabel("NIM:"));
        panel.add(nimField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Nomor Telepon:"));
        panel.add(nomorTeleponField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(registerButton);

        add(panel);

        registerButton.addActionListener(e -> {
            int newId = userDAO.getNewUserId();
            User user = new User(
                newId,
                namaField.getText(),
                nimField.getText(),
                emailField.getText(),
                nomorTeleponField.getText(),
                new String(passwordField.getPassword()),
                3,
                false
            );

            boolean success = userDAO.register(user);
            if (success) {
                JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                new Login().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registrasi gagal!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}