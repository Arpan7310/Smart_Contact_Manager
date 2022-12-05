package com.smart.service;


import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sound.midi.Soundbank;
import java.security.Principal;
import java.util.Properties;

@Service
public class EmailService {



  public  boolean  sendEmail (String message, String subject, String to) {

      //Variable for gmail
      // Simple mail transfer protocol(SMTP)
      // The server from which we are going to send the mail

      boolean f=false;

      String host="smtp.gmail.com";

      String from ="asde2wee@gmail.com";

      //get the system properties
      Properties properties= System.getProperties();


      System.out.println("PROPERTIES"+properties);

      //setting important information rto properties object
      //ssl->secure socket layer for security purpose
      //host set
      properties.put("mail.smtp.host",host);
      properties.put("mail.smtp.port","465");
      properties.put("mail.smtp.ssl.enable","true");
      properties.put("mail.smtp.auth","true");


      //Step 1 to get the session object

      //Anonymous class
      // The syntax of an anonymous class expression is like the invocation of a constructor, except that there is a class definition contained in a block of code.
      Session session=Session.getInstance(properties, new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(from,"hbvpqpkzwudjrolv");
          }
      });

      session.setDebug(true);

      //Step 2 compose the message[text,message]
      MimeMessage m= new MimeMessage(session);

      try {
          //From email id
          m.setFrom(from);

          //adding recipient to message
          m.addRecipient(Message.RecipientType.TO,new InternetAddress(to));

          //adding text to message
         // m.setText(message);
          m.setSubject(subject);
          m.setContent(message,"text/html");


          //send

          //Step:3  send the message using Transport class
          Transport.send(m);

          System.out.println("Send successfully");
          f=true;

      }
      catch (Exception e)
      {
          e.printStackTrace();
      }

      return f;

  }

}
