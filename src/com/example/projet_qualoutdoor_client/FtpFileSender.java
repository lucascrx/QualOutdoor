package com.example.projet_qualoutdoor_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/*
 * Classe fille de Sender propre au transfert de type FTP
 * 
 * Cette classe réunit les paramètres spécifiques à un transfert d'information de type ftp
 * et les méthodes pour réaliser ce transfert
 * 
 * Une connexion FTP étant intimement liée au transfert d'un fichier donné, la classe FtpFileSender
 * ne présentera que la méthode envoyerFichier. 
 * 
 * avec cette implementation il faut voir un objet FtpFileSender comme un envoi ponctuel entre un client
 * et un serveur
*/
public class FtpFileSender implements Sender{
	
	//PARAMETRES PROPRES A UNE CONNEXION FTP:
	
	private String user;//le login que doit présenter l'utilisateur au serveur
	private String password;//le mot de passe que doit présenter l'utilisateur au serveur
	private String storingPath;//le chemin de stockage du fichier dans le serveur
	
	
	public FtpFileSender(String user, String password, String storingPath) {
		this.user = user;
		this.password = password;
		this.storingPath = storingPath;
	}

	/*Implementation de la méthode envoyerFichier
	 * 
	 * les paramètres sont donc les informations necessaires communes
	 */
	@Override
	public String envoyerFichier(String url,String fileName,InputStream content){
		//initialisation de la réponse à retourner
		String response="";
		try {
			
	
			//mise en forme de l'url complete
			String target = "ftp://"+this.user+":"+this.password+"@"+url+this.storingPath+fileName+".csv";
			//création de l'URL
			URL targetAddress = new URL(target);
			//ouverture de la connection
			URLConnection connection = targetAddress.openConnection();
			//autorisation des entrées afin d'écrire le contenu du fichier
			connection.setDoOutput(true);
			//on ne peut pas autoriser à la fois sortie et entrée.
			
			
			//creation d'un flux d'écriture pour l'upload
			OutputStream os = connection.getOutputStream();
			
			//Mise en place d'un mécanisme de lecture-écriture: on lit dans le flux passé en paramètre et on écrit dans le flux crée
			
			//buffer de transfert
			byte[] temp = new byte[1024];
			//indicateur permettant de savoir si la lecture du fichier est terminée
			int indic;
			while((indic = content.read(temp)) != -1){//on lit le flux entrant dans le buffer
				os.write(temp, 0, indic);//on écrit le buffer dans le flux sortant
			}
			
			/*une fois le fichier envoyé on récupere la réponse
			 * or ici il ya un problème avec les sorties de la connexion
			 * on utilise donc une réponse générée en local : 
			 * si aucune exception a été levé lors de la suite d'instruction le transfert s'est bien passé
			 * (solution optimiste)
			 */
        	response = "reponse autogénérée : le transfert s'est bien effectué";
        	os.close();
        	
        	//dans les cas ou il y a eu une exception levée on l'indique dans la phrase de retour
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			response = "reponse autogénérée: mauvais transfert : URL malformé";
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			response = "reponse autogénérée: probleme avec la cible"+e.toString();
		}
		//à la fin, dans chaque cas on renvoie la phrase de retour
		return response;
		
	
	}
	
	
	
	
	
}
