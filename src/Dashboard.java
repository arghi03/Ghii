import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Dashboard extends JFrame {
    public Dashboard(String nama, String email) {
        setTitle("Dashboard Perpustakaan");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel utama
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Label sambutan
        JLabel welcomeLabel = new JLabel("Selamat Datang, " + nama + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Panel tombol
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Tombol profil
        JButton profileButton = new JButton("Profil");
        profileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User user = UserDAO.getUserByNameAndEmail(nama, email);
                if (user != null) {
                    System.out.println("Navigasi ke profil untuk: " + nama);
                    new ProfileScreen(user);
                    dispose();
                } else {
                    System.out.println("Gagal memuat data profil untuk: " + nama);
                    JOptionPane.showMessageDialog(Dashboard.this, "Gagal memuat profil!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Tombol logout
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Logout: " + nama);
                dispose();
                new Login().setVisible(true);
            }
        });

        buttonPanel.add(profileButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
        setVisible(true);
    }
}