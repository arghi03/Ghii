import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;



public class Register extends JFrame {
    private JTextField nameField, emailField, nimField; // Tambah field NIM
    private JPasswordField passwordField;
    private JButton registerButton, backButton;
    private UserDAO userDAO;

    public Register() {
        userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Register");
        setSize(400, 350); // Tambah tinggi form biar muat NIM
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Daftar Akun Baru");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 58, 138));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        nameField = new JTextField(20);
        emailField = new JTextField(20);
        nimField = new JTextField(20); // Tambah input NIM
        passwordField = new JPasswordField(20);

        styleInput(nameField, "Nama Lengkap");
        styleInput(emailField, "Email");
        styleInput(nimField, "NIM"); // Placeholder buat NIM
        styleInput(passwordField, "Password");

        panel.add(nameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(nimField); // Tambah field NIM ke panel
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(registerButton = createStyledButton("Daftar", new Color(76, 175, 80)));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(backButton = createStyledButton("Kembali", new Color(107, 114, 128)));

        add(panel);

        registerButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String nim = nimField.getText().trim(); // Ambil nilai NIM
            String password = new String(passwordField.getPassword()).trim();

            // Validasi semua field
            if (name.isEmpty() || email.isEmpty() || nim.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.equals("Nama Lengkap") || email.equals("Email") || nim.equals("NIM") || password.equals("Password")) {
                JOptionPane.showMessageDialog(this, "Harap isi data dengan benar!", "Error", JOptionPane.ERROR_MESSAGE);
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
                newId,                
                name,                 
                nim,                
                email,                
                "",                   
                password,             
                3,                   
                false,               
                true                  
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
            passwordField.setEchoChar((char) 0);
            passwordField.setText(placeholder);
            passwordField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent evt) {
                    if (new String(passwordField.getPassword()).equals(placeholder)) {
                        passwordField.setText("");
                        passwordField.setEchoChar('•');
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
            Register register = new Register();
            register.setVisible(true);
        });
    }
}
