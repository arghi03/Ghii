import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Import kelas
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.Loader;

public class PdfReaderScreen extends JFrame {

    private JPanel mainPanel;
    private JLabel imageLabel;
    private JScrollPane scrollPane;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageCounterLabel;

    private PDDocument document;
    private PDFRenderer pdfRenderer;
    private int currentPage = 0;

    public PdfReaderScreen(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            showError("File path tidak valid.");
            return;
        }

        try {
         
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                showError("File PDF tidak ditemukan di: " + filePath);
                return;
            }
        
            document = Loader.loadPDF(pdfFile);
            pdfRenderer = new PDFRenderer(document);
            
            initComponents();
            showPage(0);

            setTitle("Membaca: " + pdfFile.getName());
            setSize(800, 900);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Gagal membuka atau merender file PDF: " + e.getMessage());
        }
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());

       
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        prevButton = new JButton("<< Halaman Sebelumnya");
        pageCounterLabel = new JLabel();
        nextButton = new JButton("Halaman Berikutnya >>");
        
        navigationPanel.add(prevButton);
        navigationPanel.add(pageCounterLabel);
        navigationPanel.add(nextButton);
        
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
      
        scrollPane = new JScrollPane(imageLabel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);  

        mainPanel.add(navigationPanel, BorderLayout.SOUTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);

         
        nextButton.addActionListener(e -> {
            if (currentPage < document.getNumberOfPages() - 1) {
                currentPage++;
                showPage(currentPage);
            }
        });

        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                showPage(currentPage);
            }
        });
        
     
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (document != null) {
                        document.close();
                        System.out.println("Dokumen PDF ditutup.");
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private void showPage(int pageIndex) {
        try {
          
            BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageIndex, 150);
            
             
            imageLabel.setIcon(new ImageIcon(pageImage));
            
            
            pageCounterLabel.setText("Halaman " + (pageIndex + 1) + " / " + document.getNumberOfPages());
            
            
            prevButton.setEnabled(pageIndex > 0);
            nextButton.setEnabled(pageIndex < document.getNumberOfPages() - 1);

            SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));

        } catch (IOException e) {
            e.printStackTrace();
            showError("Gagal merender halaman " + (pageIndex + 1));
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  
        SwingUtilities.invokeLater(() -> dispose());
    }
}