import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import com.formdev.flatlaf.FlatClientProperties;  
import com.formdev.flatlaf.FlatLightLaf;
 


public class Login extends JFrame {
    private JTextField nameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private UserDAO userDAO;  

    public Login() {
        userDAO = new UserDAO(DBConnection.getConnection());  

        setTitle("LiteraSpace - Login");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() { 
        JPanel mainPanel = new JPanel(new BorderLayout());
 
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(72, 191, 172));
        leftPanel.setPreferredSize(new Dimension(400, 500));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginTitle = new JLabel("LOGIN");
        loginTitle.setFont(new Font("Arial", Font.BOLD, 48));
        loginTitle.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(60, 40, 40, 40);
        leftPanel.add(loginTitle, gbc);

        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.anchor = GridBagConstraints.WEST;  
        nameField = createPlaceholderTextField("Username atau Email");
        gbc.gridy = 1;
        leftPanel.add(nameField, gbc);

        passwordField = createPlaceholderPasswordField("Password");
        gbc.gridy = 2;
        leftPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
         
        loginButton = createRoundedButton("Login", Color.WHITE, 120, 40);
        registerButton = createRoundedButton("Sign Up", Color.WHITE, 120, 40);
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(30, 40, 60, 40);
        gbc.anchor = GridBagConstraints.CENTER;
        leftPanel.add(buttonPanel, gbc);
 
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(242, 237, 232));
        rightPanel.setPreferredSize(new Dimension(400, 500));
        
        GridBagConstraints rightGbc = new GridBagConstraints();
        
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        GridBagConstraints logoGbc = new GridBagConstraints();
        
        // Menambahkan ikon buku
        JLabel bookIcon = createBookIcon();
        logoGbc.gridx = 0;
        logoGbc.gridy = 0;
        logoGbc.insets = new Insets(0, 0, 20, 0);
        logoPanel.add(bookIcon, logoGbc);
        
        JLabel brandName = new JLabel("LiteraSpace");
        brandName.setFont(new Font("Arial", Font.BOLD, 36));
        brandName.setForeground(new Color(147, 112, 219));
        logoGbc.gridy = 1;
        logoPanel.add(brandName, logoGbc);
        
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightPanel.add(logoPanel, rightGbc);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        add(mainPanel);

        // Action Listeners
        loginButton.addActionListener(e -> loginUser());

        registerButton.addActionListener(e -> {
            new Register().setVisible(true);
            dispose();
        });
    }

    private void loginUser() {
        String nameOrEmail = nameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (nameOrEmail.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username/Email dan Password harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
 
        User user = userDAO.login(nameOrEmail, password);
        if (user != null) { 
            JOptionPane.showMessageDialog(this, "Login berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
             if (user.isVerified()) {
                  new Dashboard(user.getNama(), user.getEmail(), user.getIdRole()).setVisible(true);
             } else {
                   
             }
            dispose();  
        } else {
            JOptionPane.showMessageDialog(this, "Login gagal! Periksa kembali username/email dan password Anda.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    
    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(300, 40));
        textField.setForeground(Color.BLACK);
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    private JPasswordField createPlaceholderPasswordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);

        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return passwordField;
    }
    
    private JButton createRoundedButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        button.setBackground(bgColor);
        button.setForeground(new Color(72, 191, 172));
        button.setPreferredSize(new Dimension(width, height));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        Color originalBgColor = bgColor;
        Color hoverBgColor = bgColor.darker();
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverBgColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBgColor);
            }
        });
        return button;
    }

    private JLabel createBookIcon() {
        JLabel iconLabel = new JLabel("📖");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
        iconLabel.setForeground(new Color(147, 112, 219));
        return iconLabel;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch(Exception ex) {
            System.err.println("Gagal menginisialisasi Look and Feel.");
        }
        
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
