import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SuggestionListScreen extends JFrame {
    private SuggestionDAO suggestionDAO;
    private User currentUser;
    private DefaultTableModel tableModel;
    private JTable suggestionTable;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Palet Warna
    private Color primaryColor = new Color(30, 58, 138); 
    private Color viewColor = new Color(23, 162, 184);  
    private Color dangerColor = new Color(220, 53, 69);
    private Color headerColor = new Color(224, 231, 255); 
    private Color backgroundColor = new Color(240, 242, 245);
    private Color neutralColor = new Color(107, 114, 128);

    public SuggestionListScreen(User user) {
        this.currentUser = user;
        this.suggestionDAO = new SuggestionDAO(DBConnection.getConnection());

        setTitle("Daftar Saran Buku - " + currentUser.getNama());
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadSuggestions();

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Daftar Saran Buku dari Pengguna", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Judul Saran", "Penulis Saran", "Disarankan Oleh", "Tanggal", "Status", "Aksi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;  
            }
        };
        suggestionTable = new JTable(tableModel);
        suggestionTable.setRowHeight(35);
        suggestionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        suggestionTable.getTableHeader().setBackground(headerColor);

        TableColumnModel columnModel = suggestionTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(50);
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(100);
        columnModel.getColumn(6).setPreferredWidth(200);

        // Action handler untuk tombol di dalam tabel
        ActionPanel actionHandler = new ActionPanel(suggestionTable);
        columnModel.getColumn(6).setCellRenderer(actionHandler);
        columnModel.getColumn(6).setCellEditor(actionHandler);
        
        // Center align untuk beberapa kolom
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);
        columnModel.getColumn(4).setCellRenderer(centerRenderer);
        columnModel.getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(suggestionTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol Bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton refreshButton = new JButton("Refresh Daftar");
        refreshButton.addActionListener(e -> loadSuggestions());
        
        JButton backButton = new JButton("Kembali ke Dashboard");
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

    private void loadSuggestions() {
        tableModel.setRowCount(0);
        List<Suggestion> suggestions = suggestionDAO.getAllSuggestions();

        if (suggestions.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "Belum ada saran yang masuk.", "-", "-", "-", "-", null});
        } else {
            for (Suggestion s : suggestions) {
                tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getTitle(),
                    s.getAuthor(),
                    s.getUsername(),
                    s.getCreatedAt().format(DATETIME_FORMATTER),
                    s.getStatus(),
                    "Aksi" // Placeholder
                });
            }
        }
    }

    // Inner class untuk tombol-tombol aksi
    class ActionPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton viewButton, deleteButton;
        private JTable table;
        private int currentRow;

        public ActionPanel(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            panel.setOpaque(true);

            viewButton = new JButton("Tandai Dibaca");
            styleButton(viewButton, viewColor);
            viewButton.setActionCommand("view");
            viewButton.addActionListener(this);

            deleteButton = new JButton("Hapus");
            styleButton(deleteButton, dangerColor);
            deleteButton.setActionCommand("delete");
            deleteButton.addActionListener(this);

            panel.add(viewButton);
            panel.add(deleteButton);
        }
        
        private void styleButton(JButton button, Color color) {
            button.setFont(new Font("Arial", Font.BOLD, 10));
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() { return null; }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            
            // Ambil ID saran dari kolom pertama
            int suggestionId = (int) table.getModel().getValueAt(this.currentRow, 0);
            String command = e.getActionCommand();

            if ("view".equals(command)) {
                boolean success = suggestionDAO.updateSuggestionStatus(suggestionId, "viewed");
                if(success) {
                    loadSuggestions();  
                } else {
                    JOptionPane.showMessageDialog(table, "Gagal mengubah status saran.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } else if ("delete".equals(command)) {
                int confirm = JOptionPane.showConfirmDialog(table, "Yakin ingin menghapus saran ini secara permanen?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = suggestionDAO.deleteSuggestion(suggestionId);
                    if (success) {
                        loadSuggestions(); 
                    } else {
                        JOptionPane.showMessageDialog(table, "Gagal menghapus saran.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}