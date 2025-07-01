import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {
    private static final String FROM_EMAIL = "literaspace25@gmail.com"; 
    private static final String EMAIL_PASSWORD = "evfkowedhpwoemsc"; 

    public static String sendOTP(String toEmail) {
        // Generate kode OTP (6 digit)
        String otp = String.format("%06d", new Random().nextInt(999999));
        System.out.println("Generated OTP: " + otp);

        // Konfigurasi email server (Gmail SMTP)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true"); // Tambah debug buat liat log SMTP

        // Buat session email
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, EMAIL_PASSWORD);
            }
        });

        try {
            // Buat email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Kode Verifikasi OTP");
            message.setText("Kode OTP Anda adalah: " + otp + "\nJangan bagikan kode ini kepada siapapun.");

            // Kirim email
            Transport.send(message);
            System.out.println("OTP berhasil dikirim ke: " + toEmail);
            return otp;
        } catch (MessagingException e) {
            System.err.println("Gagal mengirim email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}