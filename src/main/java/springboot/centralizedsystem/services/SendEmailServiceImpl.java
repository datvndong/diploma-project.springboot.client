package springboot.centralizedsystem.services;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import springboot.centralizedsystem.resources.Views;

@Service
public class SendEmailServiceImpl implements SendEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendEmail(String mailAccount, String nameForm) throws MessagingException {
        Context context = new Context();
        context.setVariable("title", "Notification from C.D System");

        String body = templateEngine.process(Views.EMAIL, context);

        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);

        helper.setTo(mailAccount);
        helper.setSubject("You've got new report named \"" + nameForm + "\"");
        helper.setText(body, true);

        mailSender.send(mail);
    }
}
