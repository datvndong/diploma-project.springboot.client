package springboot.centralizedsystem.admin.services;

import javax.mail.MessagingException;

public interface SendEmailService {

    void sendEmail(String mail, String nameForm) throws MessagingException;
}
