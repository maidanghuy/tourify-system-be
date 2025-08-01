package com.example.tourify_system_be.service;

import com.example.tourify_system_be.entity.BookingTour;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBookingConfirmationEmail(String email, String name, String tourName,
                                             int total, String start, String end) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Xác nhận đặt tour thành công");

            String htmlContent = "<html><body>" +
                    "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                    "<p>Bạn đã đặt thành công tour <strong>" + tourName + "</strong>.</p>" +
                    "<p><b>Thời gian:</b> " + start + " → " + end + "</p>" +
                    "<p><b>Tổng tiền:</b> " + total + " VND</p>" +
                    "<p>Cảm ơn bạn đã sử dụng dịch vụ của Tourify.</p>" +
                    "<br><p>Trân trọng,<br/>Tourify Team</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("noreply@tourify.com", "Tourify Support"));

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBookingPAIDEmail(BookingTour bookingTour, Tour tour, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');

        DecimalFormat formatterPrice = new DecimalFormat("#,###", symbols);
        String formattedPrice = formatterPrice.format(tour.getPrice());
        String formattedTotalPrice = formatterPrice.format(bookingTour.getTotalPrice());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Thanh toán tour thành công");

            String htmlContent = "<table align=\"center\" style=\"Margin:0 auto;border-collapse:collapse;border-spacing:0;float:none;letter-spacing:-.2px;margin:0 auto;padding:0;text-align:center;vertical-align:top;width:100%\">\n" +
                    "   <tbody>\n" +
                    "      <tr style=\"padding:0;text-align:left;vertical-align:top;\">\n" +
                    "         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "            <table align=\"center\" class=\"m_-7048626751723638706container\" style=\"background:0 0;border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;margin:0 auto;margin-bottom:0;margin-top:0;padding:0;text-align:inherit;vertical-align:top;width:580px\">\n" +
                    "               <tbody>\n" +
                    "                  <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                     <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                        <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <td height=\"5px\" style=\"border-collapse:collapse!important;color:#555863;font-size:5px;font-weight:400;line-height:5px;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">&nbsp;</td>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                        <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:0;padding-bottom:5px!important;padding-left:16px;padding-right:16px;text-align:left;width:564px\">\n" +
                    "                                    <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                       <tbody>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                             <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left\">\n" +
                    "                                                <a href=\"https://www.tourify.com\" style=\"color:#0f81d7;font-weight:400;line-height:1.6;margin:0;padding:0;text-align:center;text-decoration:none\" target=\"_blank\" data-saferedirecturl=\"https://www.google.com/url?q=https://www.ivivu.com&amp;source=gmail&amp;ust=1752468589128000&amp;usg=AOvVaw3Od7-tT6OxCvNTFPlHGZpH\">\n" +
                    "                                                   <center style=\"min-width:532px;width:100%\">\n" +
                    "                                                      <img width=\"60px\" src=\"https://res.cloudinary.com/djq3ddowy/image/upload/v1753979276/76a2c847-8f11-4675-a6db-1e01ff069871.png\" alt=\"tourify.com\" align=\"center\" style=\"Margin:0 auto;border:none;clear:both;display:block;float:none;margin:0 auto;max-width:100%;outline:0;padding:0;text-align:center;text-decoration:none;width:273px!important;height:58px!important\" class=\"CToWUd\" data-bit=\"iit\">\n" +
                    "                                                   </center>\n" +
                    "                                                </a>\n" +
                    "                                             </th>\n" +
                    "                                             <th class=\"m_-7048626751723638706expander\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0!important;text-align:left;width:0\"></th>\n" +
                    "                                          </tr>\n" +
                    "                                       </tbody>\n" +
                    "                                    </table>\n" +
                    "                                 </th>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                     </td>\n" +
                    "                  </tr>\n" +
                    "               </tbody>\n" +
                    "            </table>\n" +
                    "            <table align=\"center\" class=\"m_-7048626751723638706container\" style=\"background:#fefefe;border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;margin:0 auto;margin-bottom:0;margin-top:5px;padding:0;text-align:inherit;vertical-align:top;width:580px\">\n" +
                    "               <tbody>\n" +
                    "                  <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                     <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                        <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <td height=\"20px\" style=\"border-collapse:collapse!important;color:#555863;font-size:20px;font-weight:400;line-height:20px;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">&nbsp;</td>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                        <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:0;padding-bottom:16px;padding-left:16px;padding-right:16px;text-align:left;width:564px\">\n" +
                    "                                    <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                       <tbody>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                             <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left\">\n" +
                    "                                                <h4 style=\"color:#003c71;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:24px;font-weight:400;line-height:1.2;margin:0!important;margin-bottom:10px;padding:0;text-align:center;word-wrap:normal\">THANH TOÁN THÀNH CÔNG</h4>\n" +
                    "                                             </th>\n" +
                    "                                             <th class=\"m_-7048626751723638706expander\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0!important;text-align:left;width:0\"></th>\n" +
                    "                                          </tr>\n" +
                    "                                       </tbody>\n" +
                    "                                    </table>\n" +
                    "                                 </th>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                        <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:0;padding-bottom:5px!important;padding-left:16px;padding-right:16px;text-align:left;width:564px\">\n" +
                    "                                    <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                       <tbody>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                             <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left\">\n" +
                    "                                                <p style=\"font-size:14px;font-weight:400;line-height:1.4;margin:0;margin-bottom:5px;padding:0;text-align:left\">Cảm ơn Quý Khách đã tin tưởng và sử dụng dịch vụ Tourify.com.</p>\n" +
                    "                                                <p style=\"font-size:14px;font-weight:400;line-height:1.4;margin:0;margin-bottom:5px;padding:0;text-align:left\"><b>Tourify.com sẽ kiểm tra tình trạng tour theo thông tin bên dưới và phản hồi cho Quý khách trong thời gian sớm nhất</b></p>\n" +
                    "                                             </th>\n" +
                    "                                             <th class=\"m_-7048626751723638706expander\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0!important;text-align:left;width:0\"></th>\n" +
                    "                                          </tr>\n" +
                    "                                       </tbody>\n" +
                    "                                    </table>\n" +
                    "                                 </th>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                        <table class=\"m_-7048626751723638706row m_-7048626751723638706collapse\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:0;padding-left:0;padding-right:0;text-align:left;width:588px\">\n" +
                    "                                    <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                       <tbody>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                             <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left\">\n" +
                    "                                                <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                   <tbody>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                         <td colspan=\"4\" style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:500;line-height:1.4;margin:0;padding:8px;padding-left:17px;padding-right:17px;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                                                            <h6 style=\"color:#26bed6;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:30.5px;margin:0!important;margin-bottom:10px;padding:0;text-align:left;word-wrap:normal\"><a href=\"https://www.tourify.com/tourify/tourDetail?id="+ tour.getTourId() +"\" style=\"color:#26bed6;font-weight:400;line-height:1.6;margin:0;padding:0;text-align:left;text-decoration:none\" target=\"_blank\" data-saferedirecturl=\"https://www.google.com/url?q=https://www.ivivu.com/du-lich/tour-trung-quoc-5n4d-ha-noi-thuong-hai-o-tran-hang-chau-bay-vj/5061&amp;source=gmail&amp;ust=1752468589128000&amp;usg=AOvVaw1kSBibrplaKpeUh9EDQcyo\"><b style=\"color:#00c1de\"><u>"+ tour.getTourName() + "</u></b></a></h6>\n" +
                    "                                                         </td>\n" +
                    "                                                      </tr>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;padding-left:17px;text-align:left;vertical-align:top;width:22%;word-wrap:break-word\">Mã tour:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word;width:27%\">" + tour.getTourId() +"</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;width:20%;word-wrap:break-word\">Khởi hành từ:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;padding-right:17px;text-align:left;vertical-align:top;word-wrap:break-word\">Hà Nội</td>\n" +
                    "                                                      </tr>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;padding-left:17px;text-align:left;vertical-align:top;width:22%;word-wrap:break-word\">Thời gian:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word;width:27%\">"+ tour.getDuration() +" ngày</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;width:20%;word-wrap:break-word\">Số khách:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word\">" + bookingTour.getAdultNumber() + " người lớn <br> " + bookingTour.getChildNumber() + " trẻ em </td>\n" +
                    "                                                      </tr>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top;border-bottom:5px solid #f4f4f4\">\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;padding-left:17px;text-align:left;vertical-align:top;width:22%;word-wrap:break-word\">Ngày khởi hành:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word;width:27%\">" + bookingTour.getDayStart().toLocalDate().format(formatter) + "</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;width:20%;word-wrap:break-word\">Ngày kết thúc:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word\">" + bookingTour.getDayEnd().toLocalDate().format(formatter) + "</td>\n" +
                    "                                                      </tr>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                         <td colspan=\"4\" style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:500;line-height:1.4;margin:0;padding:8px;padding-left:17px;padding-right:17px;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                                                            <h6 style=\"color:#003c71;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:20px;font-weight:400;line-height:30.5px;margin:0!important;margin-bottom:10px;padding:0;text-align:left;word-wrap:normal\">Thông tin khách hàng</h6>\n" +
                    "                                                         </td>\n" +
                    "                                                      </tr>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;padding-left:17px;text-align:left;vertical-align:top;width:22%;word-wrap:break-word\">Họ tên:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word;width:27%\">" + user.getFirstName() + " " + user.getLastName() + "</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;width:20%;word-wrap:break-word\">Điện thoại:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;padding-right:17px;text-align:left;vertical-align:top;word-wrap:break-word\">" + user.getPhoneNumber() +"</td>\n" +
                    "                                                      </tr>\n" +
                    "                                                   </tbody>\n" +
                    "                                                </table>\n" +
                    "                                                <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                   <tbody>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;padding-left:17px;text-align:left;vertical-align:top;width:22%;word-wrap:break-word\">Email:</td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word\"><a href=\"mailto:" + user.getEmail() + "\" target=\"_blank\">" + user.getEmail() + "</a></td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word\"></td>\n" +
                    "                                                         <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:bold;line-height:1.4;margin:0;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word\"></td>\n" +
                    "                                                      </tr>\n" +
                    "                                                   </tbody>\n" +
                    "                                                </table>\n" +
                    "                                             </th>\n" +
                    "                                             <th class=\"m_-7048626751723638706expander\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0!important;text-align:left;width:0\"></th>\n" +
                    "                                          </tr>\n" +
                    "                                       </tbody>\n" +
                    "                                    </table>\n" +
                    "                                 </th>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                        <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:8px 17px;padding-bottom:5px!important;text-align:left;width:564px\">\n" +
                    "                                    <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                       <tbody>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                             <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left\">\n" +
                    "                                                <h6 style=\"color:#003c71;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:18px;font-weight:400;line-height:1.4;margin:0!important;margin-bottom:10px;padding:0;text-align:left;word-wrap:normal\">Giá tour chi tiết</h6>\n" +
                    "                                             </th>\n" +
                    "                                             <th class=\"m_-7048626751723638706expander\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0!important;text-align:left;width:0\"></th>\n" +
                    "                                          </tr>\n" +
                    "                                          <tr></tr>\n" +
                    "                                       </tbody>\n" +
                    "                                    </table>\n" +
                    "                                 </th>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                        <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr>\n" +
                    "                                 <td style=\"border-collapse:collapse\">\n" +
                    "                                    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#fff3d9;border-spacing:0;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:11px;margin:0;max-width:100%;padding:0;width:100%;word-wrap:break-word\" bgcolor=\"#FFFFFF\">\n" +
                    "                                       <thead>\n" +
                    "                                          <tr>\n" +
                    "                                             <th style=\"background:#ffedc6;border-bottom-color:#ffe7b3;border-bottom-style:solid;border-top-color:#ffe7b3!important;border-top-style:solid!important;border-width:1px 0;color:#555863;font-weight:700;padding:8px;text-align:left;vertical-align:top;word-wrap:break-word;width:25%;padding-left:16px\" align=\"center\" bgcolor=\"#F1F1F2\" valign=\"top\">\n" +
                    "                                                <span style=\"font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;line-height:20px;font-size:12px\">Nội dung</span>\n" +
                    "                                             </th>\n" +
                    "                                             <th style=\"background:#ffedc6;border-bottom-color:#ffe7b3;border-bottom-style:solid;border-top-color:#ffe7b3!important;border-top-style:solid!important;border-width:1px 0;color:#555863;font-weight:700;padding:8px;text-align:right;vertical-align:top;word-wrap:break-word;width:25%;padding-right:30px\" align=\"center\" bgcolor=\"#F1F1F2\" valign=\"top\">\n" +
                    "                                                <span style=\"font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;line-height:20px;font-size:12px\">Giá/người</span>\n" +
                    "                                             </th>\n" +
                    "                                             <th style=\"background:#ffedc6;border-bottom-color:#ffe7b3;border-bottom-style:solid;border-top-color:#ffe7b3!important;border-top-style:solid!important;border-width:1px 0;color:#555863;font-weight:700;padding:8px;text-align:right;vertical-align:top;word-wrap:break-word;width:25%;padding-right:29px\" bgcolor=\"#F1F1F2\" valign=\"top\">\n" +
                    "                                                <span style=\"font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;line-height:20px;font-size:12px\">Số lượng</span>\n" +
                    "                                             </th>\n" +
                    "                                             <th style=\"background:#ffedc6;border-bottom-color:#ffe7b3;border-bottom-style:solid;border-top-color:#ffe7b3!important;border-top-style:solid!important;border-width:1px 0;color:#555863;font-weight:700;padding:8px;text-align:right;vertical-align:top;word-wrap:break-word;width:25%;padding-right:18px\" align=\"center\" bgcolor=\"#F1F1F2\" valign=\"top\">\n" +
                    "                                                <span style=\"font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;line-height:20px;font-size:12px\">Thành tiền</span>\n" +
                    "                                             </th>\n" +
                    "                                          </tr>\n" +
                    "                                       </thead>\n" +
                    "                                       <tbody>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top;color:#000000\">\n" +
                    "                                             <td style=\"padding:5px 0 5px 16px;border-bottom:1px #ffe7b3 solid;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:left\">\n" +
                    "                                                <span style=\"color:#555863\">Giá tour cơ bản</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding:5px 0 5px 8px;border-bottom:1px #ffe7b3 solid;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:30px\">\n" +
                    "                                                <span style=\"color:#555863\">" + formattedPrice + "</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding:5px 0 5px 7px;border-bottom:1px #ffe7b3 solid;width:70px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:29px\">\n" +
                    "                                                <span style=\"color:#555863\">" + (bookingTour.getAdultNumber() + bookingTour.getChildNumber())  +"</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding:5px 0 5px 6px;border-bottom:1px #ffe7b3 solid;width:150px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:20px\">\n" +
                    "                                                <span style=\"color:#555863\">" + formattedTotalPrice + "</span>\n" +
                    "                                             </td>\n" +
                    "                                          </tr>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top;color:#000000;display:none\">\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;padding-left:16px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:left\">\n" +
                    "                                                <span style=\"color:#555863\">Giá tour trẻ em</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;padding-left:8px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:30px\">\n" +
                    "                                                <span style=\"color:#555863\">0</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;width:70px;padding-left:7px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:29px\">\n" +
                    "                                                <span style=\"color:#555863\">0</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;width:150px;padding-left:6px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:20px\">\n" +
                    "                                                <span style=\"color:#555863\">0</span>\n" +
                    "                                             </td>\n" +
                    "                                          </tr>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top;color:#000000;display:none\">\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;padding-left:16px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:left\">\n" +
                    "                                                <span style=\"color:#555863\">Phụ thu</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;padding-left:8px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:30px\">\n" +
                    "                                                <span style=\"color:#555863\">0</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;width:70px;padding-left:7px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:29px\">\n" +
                    "                                                <span style=\"color:#555863\"></span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding-top:5px;padding-bottom:5px;border-bottom:1px #ffe7b3 solid;width:150px;padding-left:6px;font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;text-align:right;padding-right:20px\">\n" +
                    "                                                <span style=\"color:#555863\">0</span>\n" +
                    "                                             </td>\n" +
                    "                                          </tr>\n" +
                    "                                       </tbody>\n" +
                    "                                       <tfoot style=\"font-size:12px\">\n" +
                    "                                          <tr style=\"color:#ec971f\">\n" +
                    "                                             <td colspan=\"3\" style=\"padding:16px;line-height:10px;text-align:left;vertical-align:top;word-wrap:break-word;color:#555863\" align=\"left\" valign=\"top\">\n" +
                    "                                                <span style=\"font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px\">Tổng giá tour</span>\n" +
                    "                                             </td>\n" +
                    "                                             <td style=\"padding:8px;text-align:right;vertical-align:top;word-wrap:break-word;font-weight:bold;padding-top:5px;padding-right:17px\" align=\"right\" valign=\"top\">\n" +
                    "                                                <span style=\"font-family:Roboto,RobotoDraft,Helvetica,Arial,sans-serif;font-size:14px;line-height:30.9px\">" + formattedTotalPrice + " VND</span>\n" +
                    "                                             </td>\n" +
                    "                                          </tr>\n" +
                    "                                       </tfoot>\n" +
                    "                                    </table>\n" +
                    "                                 </td>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                        <table class=\"m_-7048626751723638706row\" style=\"display:none;border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" colspan=\"2\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:8px 17px;text-align:left;width:274px;border-bottom:5px solid #f4f4f4\">\n" +
                    "                                    <p>\n" +
                    "                                       <span style=\"color:#555863;font-size:14px;line-height:0.5\">\n" +
                    "                                       Yêu cầu khác:\n" +
                    "                                       </span> \n" +
                    "                                    </p>\n" +
                    "                                 </th>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                     </td>\n" +
                    "                  </tr>\n" +
                    "               </tbody>\n" +
                    "            </table>\n" +
                    "            <table align=\"center\" class=\"m_-7048626751723638706container\" style=\"background:#ffffff;border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;margin:0 auto;margin-bottom:0;margin-top:0;padding:0;text-align:inherit;vertical-align:top;width:580px\">\n" +
                    "               <tbody>\n" +
                    "                  <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                     <td style=\"border-collapse:collapse!important;color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word;border-bottom:30px solid #f4f4f4\">\n" +
                    "                        <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                           <tbody>\n" +
                    "                              <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                 <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:0;padding-bottom:5px!important;padding-left:16px;padding-right:16px;text-align:left;width:564px\">\n" +
                    "                                    <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                       <tbody>\n" +
                    "                                          <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                             <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left\">\n" +
                    "                                                <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                   <tbody>\n" +
                    "                                                      <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                         <td height=\"15px\" style=\"border-collapse:collapse!important;color:#555863;font-size:15px;font-weight:400;line-height:15px;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">&nbsp;</td>\n" +
                    "                                                      </tr>\n" +
                    "                                                   </tbody>\n" +
                    "                                                </table>\n" +
                    "                                                <div>\n" +
                    "                                                   <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                      <tbody>\n" +
                    "                                                         <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                            <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:0;padding-bottom:5px!important;padding-left:0!important;padding-right:0!important;text-align:left;width:100%\">\n" +
                    "                                                               <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                                  <tbody>\n" +
                    "                                                                     <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                                        <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;padding-bottom:0;text-align:left\"><a href=\"https://www.tourify.com\" style=\"color:#0f81d7;font-weight:400;line-height:1;margin:0;padding:0;text-align:left;text-decoration:none\" target=\"_blank\" data-saferedirecturl=\"https://www.google.com/url?q=https://www..com&amp;source=gmail&amp;ust=1752468589128000&amp;usg=AOvVaw3Od7-tT6OxCvNTFPlHGZpH\"><b style=\"font-size:24px;font-weight:500\"><span style=\"color:#003c71;font-size:14px\">tourify</span><span style=\"color:#26bed6;font-size:14px\">.com</span></b></a><span style=\"border-collapse:collapse!important;color:#555863;font-size:14px!important;text-align:left;word-wrap:break-word\"> - Hệ thống đặt phòng khách sạn &amp; tours trực tuyến hàng đầu Việt Nam.</span></th>\n" +
                    "                                                                        <th class=\"m_-7048626751723638706expander\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0!important;padding-bottom:0;text-align:left;width:0\"></th>\n" +
                    "                                                                     </tr>\n" +
                    "                                                                  </tbody>\n" +
                    "                                                               </table>\n" +
                    "                                                            </th>\n" +
                    "                                                         </tr>\n" +
                    "                                                      </tbody>\n" +
                    "                                                   </table>\n" +
                    "                                                   <table class=\"m_-7048626751723638706row\" style=\"border-collapse:collapse;border-spacing:0;display:table;font-size:12px!important;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                      <tbody>\n" +
                    "                                                         <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                            <th class=\"m_-7048626751723638706small-12 m_-7048626751723638706columns\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0 auto;padding:0;padding-bottom:5px!important;padding-left:0!important;padding-right:0!important;text-align:left;width:100%\">\n" +
                    "                                                               <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                                  <tbody>\n" +
                    "                                                                     <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                                        <th style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left\">\n" +
                    "                                                                           <div style=\"font-size:12px!important;line-height:2.2\">\n" +
                    "                                                                              <table style=\"border-collapse:collapse;border-spacing:0;letter-spacing:-.2px;padding:0;text-align:left;vertical-align:top;width:100%\">\n" +
                    "                                                                                 <tbody>\n" +
                    "                                                                                    <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                                                       <td colspan=\"2\" style=\"border-collapse:collapse!important;color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                                                                                          <b>Hỗ trợ khách hàng: 07h30 - 21h00 hằng ngày</b>\n" +
                    "                                                                                       </td>\n" +
                    "                                                                                    </tr>\n" +
                    "                                                                                    <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                                                       <td colspan=\"2\" style=\"border-collapse:collapse!important;color:#003c71;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:5px 0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                                                                                          <img width=\"15\" src=\"https://ci3.googleusercontent.com/meips/ADKq_NaKBXwV1vS96imT5_om4KqB0Gpqua8mb1DmkgQnP53cWhJJsXA9kKT9hYJJPHTZHflZ0Limg6YIHP7236qixM3Zhqu9ca-9mIpEMwA=s0-d-e1-ft#https://res.ivivu.com/img/iconnn/Vector-(Stroke)-1.png\" alt=\"☎\" class=\"CToWUd\" data-bit=\"iit\"> 1900 2045 &nbsp; &nbsp; &nbsp;\n" +
                    "                                                                                          <img width=\"15\" src=\"https://ci3.googleusercontent.com/meips/ADKq_NYFxwigZIrB-QrCyDAxmXcLXwuO8P8-jKmIaSbSxnDb6p18pD65AHrP_Bg0VnTbv1E0kLfS9jya3-_k8kzx6_PdEA=s0-d-e1-ft#https://res.ivivu.com/img/iconnn/ic_mail.png\" alt=\"✉\" class=\"CToWUd\" data-bit=\"iit\"> <a href=\"mailto:tour@tourify.info\" target=\"_blank\">tour@tourify.info</a>\n" +
                    "                                                                                       </td>\n" +
                    "                                                                                    </tr>\n" +
                    "                                                                                    <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                                                       <td style=\"width:13%;border-collapse:collapse!important;color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\"><b>HCM</b></td>\n" +
                    "                                                                                       <td style=\"border-collapse:collapse!important;color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                                                                                          <p style=\"color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;margin-bottom:5px;padding:0;text-align:left\">Tầng 2, Tòa nhà Anh Đăng, 215 Nam Kỳ Khởi Nghĩa, P. Võ Thị Sáu, Q.3, TP. HCM</p>\n" +
                    "                                                                                       </td>\n" +
                    "                                                                                    </tr>\n" +
                    "                                                                                    <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                                                       <td style=\"width:13%;border-collapse:collapse!important;color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\"><b>Hà Nội</b></td>\n" +
                    "                                                                                       <td style=\"border-collapse:collapse!important;color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                                                                                          <p style=\"color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;margin-bottom:5px;padding:0;text-align:left\">P308, Tầng 3, tòa nhà The One, số 2 Chương Dương Độ, P.Chương Dương, Q.Hoàn Kiếm, Hà Nội</p>\n" +
                    "                                                                                       </td>\n" +
                    "                                                                                    </tr>\n" +
                    "                                                                                    <tr style=\"padding:0;text-align:left;vertical-align:top\">\n" +
                    "                                                                                       <td style=\"width:13%;border-collapse:collapse!important;color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\"><b>Cần Thơ</b></td>\n" +
                    "                                                                                       <td style=\"border-collapse:collapse!important;color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;padding:0;text-align:left;vertical-align:top;word-wrap:break-word\">\n" +
                    "                                                                                          <p style=\"color:#555863;font-size:12px!important;font-weight:400;line-height:1.4;margin:0;margin-bottom:5px;padding:0;text-align:left\">Tầng 7 - Tòa nhà STS - 11B Đại Lộ Hòa Bình, P. Tân An, Q. Ninh Kiều, TP. Cần Thơ</p>\n" +
                    "                                                                                       </td>\n" +
                    "                                                                                    </tr>\n" +
                    "                                                                                 </tbody>\n" +
                    "                                                                              </table>\n" +
                    "                                                                           </div>\n" +
                    "                                                                        </th>\n" +
                    "                                                                        <th class=\"m_-7048626751723638706expander\" style=\"color:#555863;font-size:14px;font-weight:400;line-height:1.4;margin:0;padding:0!important;text-align:left;width:0\"></th>\n" +
                    "                                                                     </tr>\n" +
                    "                                                                  </tbody>\n" +
                    "                                                               </table>\n" +
                    "                                                            </th>\n" +
                    "                                                         </tr>\n" +
                    "                                                      </tbody>\n" +
                    "                                                   </table>\n" +
                    "                                                </div>\n" +
                    "                                             </th>\n" +
                    "                                          </tr>\n" +
                    "                                       </tbody>\n" +
                    "                                    </table>\n" +
                    "                                 </th>\n" +
                    "                              </tr>\n" +
                    "                           </tbody>\n" +
                    "                        </table>\n" +
                    "                     </td>\n" +
                    "                  </tr>\n" +
                    "               </tbody>\n" +
                    "            </table>\n" +
                    "         </td>\n" +
                    "      </tr>\n" +
                    "   </tbody>\n" +
                    "</table>";

            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("tour@tourify.com", "Tourify Support"));

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
