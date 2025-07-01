import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import com.formdev.flatlaf.FlatClientProperties;  
import com.formdev.flatlaf.FlatLightLaf;
 


public class Register extends JFrame {
    private JTextField nameField, emailField, nimField, phoneField;
    private JPasswordField passwordField;
    private JButton registerButton, backButton; 

    public Register() { 
        setTitle("LiteraSpace - Daftar Akun Baru");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents(); 
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel Kiri (Logo dan Brand)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(242, 237, 232));
        leftPanel.setPreferredSize(new Dimension(400, 600));
        GridBagConstraints leftGbc = new GridBagConstraints();
        
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        GridBagConstraints logoGbc = new GridBagConstraints();
        
        JLabel bookIcon = createBookIcon();
        logoGbc.gridx = 0; logoGbc.gridy = 0;
        logoGbc.insets = new Insets(0, 0, 20, 0);
        logoPanel.add(bookIcon, logoGbc);
        
        JLabel brandName = new JLabel("LiteraSpace");
        brandName.setFont(new Font("Arial", Font.BOLD, 36));
        brandName.setForeground(new Color(147, 112, 219));
        logoGbc.gridy = 1;
        logoPanel.add(brandName, logoGbc);
        
        leftGbc.gridx = 0; leftGbc.gridy = 0;
        leftPanel.add(logoPanel, leftGbc);

        // Panel Kanan (Form Registrasi)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(72, 191, 172));
        rightPanel.setPreferredSize(new Dimension(400, 600));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("CREATE ACCOUNT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across columns
        gbc.insets = new Insets(20, 40, 20, 40);
        gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(8, 40, 8, 40);
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor
        gbc.gridwidth = 1; // Reset gridwidth
 
        nameField = createPlaceholderTextField("Nama Lengkap");
        gbc.gridy = 1;
        rightPanel.add(nameField, gbc);

        nimField = createPlaceholderTextField("NIM (Nomor Induk Mahasiswa)");
        gbc.gridy = 2;
        rightPanel.add(nimField, gbc);
        
        emailField = createPlaceholderTextField("Email");
        gbc.gridy = 3;
        rightPanel.add(emailField, gbc);
        
        phoneField = createPlaceholderTextField("Nomor Telepon");
        gbc.gridy = 4;
        rightPanel.add(phoneField, gbc);

        passwordField = createPlaceholderPasswordField("Password");
        gbc.gridy = 5;
        rightPanel.add(passwordField, gbc);

        // Panel untuk Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        registerButton = createRoundedButton("Register", Color.WHITE, 120, 40);
        backButton = createRoundedButton("Back", Color.WHITE, 120, 40);
        
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 40, 20, 40);
        gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(buttonPanel, gbc);

        // Menambahkan panel ke frame utama
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel);

        // Action Listeners
        registerButton.addActionListener(e -> registerUser());
        backButton.addActionListener(e -> {
            dispose();
             new Login().setVisible(true);  
        });
    }
    
    private void registerUser() { 
        JOptionPane.showMessageDialog(this, "Tombol Register Ditekan!");
    }
 
    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField textField = new JTextField(); 
        textField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        
        // Style tambahan
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(300, 40));
        textField.setForeground(Color.BLACK); // Warna teks saat diisi
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 2, 0, new Color(220, 220, 220)), // Garis bawah tipis
            BorderFactory.createEmptyBorder(5, 10, 5, 10) // Padding internal
        ));
        return textField;
    }
 
    private JPasswordField createPlaceholderPasswordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField(); 
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);

        // Style tambahan
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 2, 0, new Color(220, 220, 220)), // Garis bawah tipis
            BorderFactory.createEmptyBorder(5, 10, 5, 10) // Padding internal
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
        button.setForeground(new Color(72, 191, 172)); // Warna teks
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
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF");
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Register().setVisible(true));
    }
}
