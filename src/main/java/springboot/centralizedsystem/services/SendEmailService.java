package springboot.centralizedsystem.services;

import javax.mail.MessagingException;

public interface SendEmailService {

    void sendEmail(String mail, String nameForm) throws MessagingException;
}
