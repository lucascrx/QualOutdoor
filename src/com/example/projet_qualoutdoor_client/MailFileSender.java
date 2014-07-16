package com.example.projet_qualoutdoor_client;


import java.io.InputStream;
import java.util.Properties;


import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import android.util.Log;

public class MailFileSender implements Sender{

	@Override
	public String envoyerFichier(String URL, String fileName,InputStream content) {
		String response;
		try {
			  Properties pros = new Properties();
              pros.put("mail.smtp.auth", "true");
              pros.put("mail.smtp.starttls.enable", "true");
              pros.put("mail.smtp.host", "smtp.gmail.com");
              pros.put("mail.smtp.port", "587");

              Session session = Session.getInstance(pros,
                      new javax.mail.Authenticator() {
                          protected PasswordAuthentication getPasswordAuthentication() {
                              return new PasswordAuthentication(
                                      "lucas.croixmarie@gmail.com", "68AuguBlan013");
                          }
                      });
              
           
			Message message = new MimeMessage(session);	
			message.setFrom(new InternetAddress("lucas.croixmarie@gmail.com"));
	        message.addRecipient(Message.RecipientType.TO, new InternetAddress(URL));
	        // Set Subject: header field
	        message.setSubject("QualOutdoor csv file");
	
	        //lecture de l'input stream pour le placer dans le corps du mail:
	        String stats;
	        java.util.Scanner s = new java.util.Scanner(content).useDelimiter("\\A");
	        if(s.hasNext()){
	        	stats = s.next();
	        }else{
	        	stats="";
	        }
	        
	        // Now set the actual message
	        message.setText("please find below what I measured \r\n"+ "name of file: "+fileName+"\r\n \r\n"	+ stats);
	        
	        // Send message
	        Transport.send(message);
	        Log.d("debug","Sent message successfully....");
			response="Sent message successfully";
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response="email error :"+e.toString();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response="email error :"+e.toString();
			}
			
			return response;
		}
		
	

}
