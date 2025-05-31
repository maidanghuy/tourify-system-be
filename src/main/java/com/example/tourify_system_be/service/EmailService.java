package com.example.tourify_system_be.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // URL backend được lấy từ application.yml
    @Value("${app.backend.url}")
    private String backendUrl;

    /**
     * Gửi email xác nhận đăng ký bằng token
     *
     * @param email  Email người nhận
     * @param name   Tên người nhận (dùng để hiển thị)
     * @param token  Mã token xác thực
     */
    public void sendRegistrationConfirmationEmail(String email, String name, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Xác nhận đăng ký tài khoản");

            // Sửa lại link xác nhận đúng API đã viết
            String confirmationLink = backendUrl + "/api/auth/confirm?token=" + token;

            String htmlContent = "<html>" +
                    "<body>" +
                    "<p>Chào " + name + ",</p>" +
                    "<p>Bạn vừa đăng ký tài khoản Tourify. Vui lòng nhấn nút bên dưới để xác nhận:</p>" +
                    "<a href=\"" + confirmationLink + "\" " +
                    "style=\"display: inline-block; padding: 10px 20px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;\">" +
                    "Accept Registration</a>" +
                    "<p>Nếu bạn không đăng ký, vui lòng bỏ qua email này.</p>" +
                    "<br><p>Trân trọng,<br/>Tourify Team</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("noreply@tourify.com", "Tourify Support"));

            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
