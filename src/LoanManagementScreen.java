import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellEditor; 
import javax.swing.AbstractCellEditor;   
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoanManagementScreen extends JFrame {
    private LoanDAO loanDAO;
    private User currentUser; 
    private DefaultTableModel tableModel;
    private JTable pendingLoansTable;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private Color primaryColor = new Color(30, 58, 138); 
    private Color successColor = new Color(28, 132, 74); 
    private Color dangerColor = new Color(220, 53, 69);  
    private Color headerColor = new Color(224, 231, 255); 
    private Color backgroundColor = new Color(240, 242, 245);
    private Color neutralColor = new Color(107, 114, 128);

    public LoanManagementScreen(User user) {
        this.currentUser = user;
        this.loanDAO = new LoanDAO(DBConnection.getConnection());

        setTitle("Kelola Peminjaman Pending - Supervisor: " + currentUser.getNama());
        setSize(900, 550); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadPendingLoans();

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Daftar Permintaan Peminjaman Buku", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID Pinjam", "Nama Peminjam", "Judul Buku", "Tgl Permintaan", "Aksi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; 
            }
        };
        pendingLoansTable = new JTable(tableModel);
        pendingLoansTable.setRowHeight(30); 
        pendingLoansTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        pendingLoansTable.getTableHeader().setBackground(headerColor);
        pendingLoansTable.getTableHeader().setOpaque(false);
        pendingLoansTable.setFont(new Font("Arial", Font.PLAIN, 12));
        pendingLoansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 

        TableColumnModel columnModel = pendingLoansTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(250);
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setPreferredWidth(180);

        ActionPanelRendererAndEditor actionHandler = new ActionPanelRendererAndEditor(pendingLoansTable);
        columnModel.getColumn(4).setCellRenderer(actionHandler);
        columnModel.getColumn(4).setCellEditor(actionHandler);


        JScrollPane scrollPane = new JScrollPane(pendingLoansTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton refreshButton = new JButton("Refresh Daftar");
        styleActionButton(refreshButton, primaryColor, 140, 35);
        refreshButton.addActionListener(e -> loadPendingLoans());
        
        JButton backButton = new JButton("Kembali ke Dashboard");
        styleActionButton(backButton, neutralColor, 180, 35);
        backButton.addActionListener(e -> dispose()); 

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.add(refreshButton);
        
        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottomPanel.setOpaque(false);
        rightBottomPanel.add(backButton);

        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightBottomPanel, BorderLayout.EAST);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadPendingLoans() {
        tableModel.setRowCount(0); 
        List<Loan> pendingLoans = loanDAO.getPendingLoans(); 

        if (pendingLoans.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "Tidak ada permintaan pending", "-", "-", null});
        } else {
            for (Loan loan : pendingLoans) {
                tableModel.addRow(new Object[]{
                        loan.getIdLoan(),
                        loan.getUsername() != null ? loan.getUsername() : "N/A",
                        loan.getBookTitle() != null ? loan.getBookTitle() : "N/A",
                        loan.getRequestDate() != null ? loan.getRequestDate().format(DATETIME_FORMATTER) : "N/A",
                        "Aksi" 
                });
            }
        }
    }
    
    private void styleActionButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, height));
        
      
        button.setOpaque(true);
        button.setBorderPainted(false);
        
        Color originalBgColor = bgColor; 
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor);
            }
        });
    }

    class ActionPanelRendererAndEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton approveButton;
        private JButton rejectButton;
        private JTable table;
        private int currentRow; 

        public ActionPanelRendererAndEditor(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            approveButton = new JButton("Setujui");
            styleActionButton(approveButton, successColor, 80, 25);
            approveButton.setActionCommand("approve");
            approveButton.addActionListener(this);

            rejectButton = new JButton("Tolak");
            styleActionButton(rejectButton, dangerColor, 80, 25);
            rejectButton.setActionCommand("reject");
            rejectButton.addActionListener(this);

            panel.add(approveButton);
            panel.add(rejectButton);
        }

        private void styleActionButton(JButton button, Color bgColor, int width, int height) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.PLAIN, 10)); 
            button.setPreferredSize(new Dimension(width, height));
            button.setMargin(new Insets(2,2,2,2));
            
            button.setOpaque(true);
            button.setBorderPainted(false);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            this.currentRow = row;
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null; 
        }

        @Override
        public boolean isCellEditable(java.util.EventObject anEvent) {
            return true; 
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped(); 

            Object idLoanObj = table.getModel().getValueAt(this.currentRow, 0);
            if (idLoanObj == null || idLoanObj.toString().equals("-")) { 
                System.err.println("Tidak bisa melakukan aksi: ID Loan tidak valid di baris " + this.currentRow);
                return;
            }
            
            if (!(idLoanObj instanceof Integer)) {
                System.err.println("Tidak bisa melakukan aksi: Tipe data ID Loan tidak valid di baris " + this.currentRow + ". Ditemukan: " + idLoanObj.getClass().getName());
                return;
            }
            int idLoan = (int) idLoanObj;

            String command = e.getActionCommand();
            boolean success = false;
            String actionMessage = "";

            if ("approve".equals(command)) {
                success = loanDAO.updateLoanStatus(idLoan, "approved", currentUser.getIdUser());
                actionMessage = success ? "Peminjaman ID " + idLoan + " berhasil disetujui." : "Gagal menyetujui peminjaman ID " + idLoan + ".";
            } else if ("reject".equals(command)) {
                success = loanDAO.updateLoanStatus(idLoan, "rejected", currentUser.getIdUser());
                actionMessage = success ? "Peminjaman ID " + idLoan + " berhasil ditolak." : "Gagal menolak peminjaman ID " + idLoan + ".";
            }

            if (success) {
                JOptionPane.showMessageDialog(table.getParent().getParent(), actionMessage, "Status Update", JOptionPane.INFORMATION_MESSAGE);
                loadPendingLoans(); 
            } else {
                JOptionPane.showMessageDialog(table.getParent().getParent(), actionMessage, "Error Update", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
