import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellEditor; 
import javax.swing.AbstractCellEditor; 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class BookManagementScreen extends JFrame {
    private BookDAO bookDAO; 
    private User currentUser; 
    private DefaultTableModel tableModel;
    private JTable booksTable;

    private Color primaryColor = new Color(30, 58, 138); 
    private Color editColor = new Color(255, 193, 7); 
    private Color dangerColor = new Color(220, 53, 69);
    private Color headerColor = new Color(224, 231, 255); 
    private Color backgroundColor = new Color(240, 242, 245);
    private Color neutralColor = new Color(107, 114, 128);

    public BookManagementScreen(User user) {
        this.currentUser = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());

        setTitle("Kelola Buku - Supervisor: " + currentUser.getNama());
        setSize(900, 550); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadAllBooks();

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Manajemen Daftar Buku", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID Buku", "Judul", "Penulis", "Rating", "Aksi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        booksTable = new JTable(tableModel);
        booksTable.setRowHeight(30); 
        booksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        booksTable.getTableHeader().setBackground(headerColor);
        booksTable.getTableHeader().setOpaque(false);
        booksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 

        TableColumnModel columnModel = booksTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(1).setPreferredWidth(300);
        columnModel.getColumn(2).setPreferredWidth(200);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(180);

        ActionPanelRendererAndEditor actionHandler = new ActionPanelRendererAndEditor(booksTable);
        columnModel.getColumn(4).setCellRenderer(actionHandler);
        columnModel.getColumn(4).setCellEditor(actionHandler);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton refreshButton = new JButton("Refresh Daftar");
        styleActionButton(refreshButton, primaryColor, 140, 35);
        refreshButton.addActionListener(e -> loadAllBooks());
        
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

    private void loadAllBooks() {
        tableModel.setRowCount(0); 
        List<Book> allBooks = bookDAO.getAllBooks(); 

        if (allBooks.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "Tidak ada buku di database", "-", "-", null});
        } else {
            for (Book book : allBooks) {
                tableModel.addRow(new Object[]{
                    book.getIdBook(),
                    book.getTitle(),
                    book.getAuthor(),
                    String.format("%.1f", book.getRating()),
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
        private JButton editButton;
        private JButton deleteButton;
        private JTable table;
        private int currentRow; 

        public ActionPanelRendererAndEditor(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            editButton = new JButton("Edit");
            styleActionButton(editButton, editColor, 80, 25);
            editButton.setActionCommand("edit");
            editButton.addActionListener(this);

            deleteButton = new JButton("Hapus");
            styleActionButton(deleteButton, dangerColor, 80, 25);
            deleteButton.setActionCommand("delete");
            deleteButton.addActionListener(this);

            panel.add(editButton);
            panel.add(deleteButton);
        }

        private void styleActionButton(JButton button, Color bgColor, int width, int height) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 10)); 
            button.setPreferredSize(new Dimension(width, height));
            button.setMargin(new Insets(2,2,2,2));
            button.setOpaque(true);
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
        public Object getCellEditorValue() {
            return null; 
        }

        // ✅ --- METHOD INI DI-UPDATE TOTAL --- ✅
        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped(); 
            
            int bookId = (int) table.getModel().getValueAt(this.currentRow, 0);
            String command = e.getActionCommand();

            if ("edit".equals(command)) {
                // Buka JDialog EditBookScreen yang baru kita buat.
                // Mengirim 'BookManagementScreen.this' sebagai 'owner' agar dialognya modal.
                new EditBookScreen(BookManagementScreen.this, bookId);
                
                // Setelah window edit ditutup, kode akan lanjut ke sini,
                // lalu kita refresh tabel untuk menampilkan data terbaru.
                loadAllBooks();

            } else if ("delete".equals(command)) {
                String bookTitle = (String) table.getModel().getValueAt(this.currentRow, 1);
                int confirm = JOptionPane.showConfirmDialog(table, 
                    "Yakin ingin menghapus buku '" + bookTitle + "'?\nBuku akan disembunyikan dari daftar.", 
                    "Konfirmasi Hapus", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = bookDAO.softDeleteBook(bookId);
                    if (success) {
                        JOptionPane.showMessageDialog(table, "Buku berhasil dihapus.");
                        loadAllBooks(); // Refresh tabel
                    } else {
                        JOptionPane.showMessageDialog(table, "Gagal menghapus buku.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}