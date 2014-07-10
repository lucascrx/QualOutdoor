package com.example.projet_qualoutdoor_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

/*
 * Classe fille de Sender propre au transfert de type HTTP
 * 
 * Cette classe réunit les paramètres spécifiques à un transfert d'information de type http
 * et les méthodes pour réaliser ce transfert
 * 
 * Une connexion HTTP est indépendante des transferts qu'elle met en oeuvre ainsi on définit
 * différentes méthodes correspondant à des actions élémentaires qui seront ensuites reprises
 * dans la fonction envoyerFichier
*/

public class HttpFileSender implements Sender{
	//PARAMETRES PROPRES A UNE CONNEXION HTTP:
	
	private String fileFieldName;//nom du champs auquel se rapporte le fichier transferer
	/*La présence de ce paramètre peut s'averer problematique en effet elle crée une relation
	 * étroite entre l'envoi d'un fichier particulier et une instance de HttpFileSender
	 * alors qu'une même instance pourrait être utilisé pour l'envoi de plusieurs fichiers
	 * 
	 *  La valeur du champs n'a pas une importance considérable, ainsi dans le cas où l'on souhairait
	 *  mettre fin à cette dépendance on supprimera cet attribut et on laissera à la fonction UploadFile
	 *  le soin de fixer le nom du champ
	 */
	
	/*ATTENTION: ces paramètres ne sont pas spécifique aux transferts http mais ils permettent d'associer
	 * une instance HttpFileSender avec une communication donnée et de limiter le nombre d'arguments 
	 * des fonctions de la classe
	 * 
	 * Avec cette implementation il faut voir une instance comme un dialogue entre client et serveur
	 * dans lequel circulent plusieurs types de données*/	
	private HttpURLConnection connection;//la connexion correspondant au dialogue
	private String delimiter;//chaine de caractère delimitant les entrées du formulaire
	private OutputStream os;//output stream permettant d'écrire dans le contenu de la requete
	private PrintWriter writer;//outil issu de os qui va nous permettre d'ecrire des octets dans la requete

	/*Constructeur pertinant si on associe la connexion à un envoi simple d'un fichier
	 *Les autres attributs sont fixés avec la méthode initialize.
	 */
	public HttpFileSender(String fileFieldName) {
		this.fileFieldName = fileFieldName;//on fixe donc le nom du champs correspond au fichier à envoyer
	}
	
	
	/*Implementation de la méthode envoyerFichier
	 * 
	 * les paramètres sont donc les informations necessaires communes
	 * la méthode fera donc appel aux fonctions définies si dessous.
	 * 
	 * 
	 */
	@Override
	public String envoyerFichier(String url,String fileName,InputStream content){
		try{
			this.initialize(url);
			this.UploadTextFile(this.fileFieldName,fileName,content);
			this.endSending();
			return this.readResponseStatus();
		}catch(HttpTransfertException e){
			return "An exception occured :"+e.toString() ;
		}
	}
	
	
	public void initialize(String url) throws HttpTransfertException{
	/*fonction qui permet l'initialisation d'une requete HTTP post à partir d'une URL	
	 *cette fonction permet de construire l'entete de la requete HTTP	
	*/
		try{
			Log.d("DEBUG","...begining initialisation");
			//on construit l'url à partir de l'adresse passée en paramètre
			URL targetAddress = new URL(url);
			//ouverture de la connection vers l'addresse indiquée
			this.connection = (HttpURLConnection) targetAddress.openConnection();
			//on précise le type de la requête
			this.connection.setRequestMethod("POST");
			//on autorise les flux entrants dans la connexion: pour pouvoir écrire les informations
			this.connection.setDoInput(true);
			//on autotise les flux sortant de la connexion: pour pouvoir lire les informations
			this.connection.setDoOutput(true);
			//on rend la connection TCP sous jacente persistente
			this.connection.setRequestProperty("Connection", "Keep-Alive");
			//on génère un délimiteur
			this.delimiter = "******"+Long.toString(System.currentTimeMillis())+"******";
			//on précise le format de la partié données de la requete
			this.connection.setRequestProperty("Content-type", "multipart/form-data; boundary="+this.delimiter);
			//on établit la connexion
			this.connection.connect();
			//on crée un acces pour écrire dans la requête via un output stream
			this.os = connection.getOutputStream();
			//on définit le writer associé à l'output stream qui permettra d'écrire les octects dans le flux
		    this.writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
		    Log.d("DEBUG","...initialisation complete...trying to send post request to"+url);	
		    
		}catch(MalformedURLException e){
			Log.d("URL exception",e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("URL exception"+e.toString());
		}catch(IOException e){
			Log.d("initialize IO Exception", e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("initialize IO Exception :"+e.toString());
		}	
	}
	
	public void SendSimpleInput(String fieldName, String val){
		/*cette fonction permet d'inserer un champ élémentaire dans le formulaire à envoyer
		 *un input fieldName sera donc crée, comportant la valeur val
		 *cette fonction ne sera appelé que sur un sender initialisé.
		*/
			Log.d("DEBUG","...trying to write input" + fieldName +" in request");
			//on indique une nouvelle entrée du formulaire dans la requete avec une limite
			this.writer.append("--"+this.delimiter+"\r\n");
			//on indique une entrée du formulaire se rapportant au champ s'appelant fieldName
			this.writer.append("Content-Disposition: form-data; name=\""+fieldName+"\"\r\n");
			//on indique le format de la valeur du champ : ici texte encodé avec UTF8
			this.writer.append("Content-Type: text/plain; charset=UTF-8\r\n");
			//on indique la valeur que l'on affecte au champ
			this.writer.append("\r\n" + val + "\r\n");
			//on écrit le flux dans la requete
			this.writer.flush();
			Log.d("DEBUG","...input writen");
		
	}
	
	public void UploadTextFile(String fieldName, String fileName,InputStream content)throws HttpTransfertException{
		/*cette fonction permet d'inserer un fichier texte dans le formulaire à envoyer
		 *un input appelé filedName sera crée et affecté à un fichier fileName dont le contenu 
		 *sera récuprérer par la lecture de content
		 *cette fonction ne sera appelé que sur un sender initialisé.
		*/
		try{
			Log.d("DEBUG","...trying to write file" + fileName +" in request");
			Log.d("DEBUG","delimiter: "+this.delimiter);
			//on indique une nouvelle entrée du formulaire dans la requete avec une limite
			this.writer.append("--"+this.delimiter+"\r\n");
			//on indique une entrée du formulaire se rapportant au champ s'appelant nomChamp
			this.writer.append("Content-Disposition: form-data; name=\"" + fieldName+ "\"; filename=\"" + fileName+ "\"\r\n");
			//on indique que le contenu correspond à du texte
			this.writer.append("Content-Type: text/plain\r\n");
			//on indique comment sera encodé le texte pour la transmission
			this.writer.append("Content-Transfer-Encoding: binary\r\n");
			this.writer.append("\r\n");
			//on écrit le flux dans la requete
			this.writer.flush();
		
			/*il faut maintenant remplir la requête avec le contenu du fichier
			 *on met en place un système de lecture écriture
			 *on definit un buffer intermediaire pour transvaser les données
			 */
			byte[] temp = new byte[1024];
			//indicateur permettant de savoir si la lecture du fichier est terminée
			int indic;
			while((indic = content.read(temp)) != -1){//on lit le flux entrant dans le buffer
				os.write(temp, 0, indic);//on écrit le buffer dans le flux sortant
			}
			
			this.writer.append("\r\n");		
		}catch(IOException e){
			Log.d("Uploading text IO Exception", e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("Uploading text reading-writing mecanisme exception :"+e.toString());
		}	
		
	}
	
	public void endSending()throws HttpTransfertException{
		/*fonction qui permet de clore le champ data de la requete
		 * une fois que tous les champs ont été inscrits dedans
		 * on aura donc achevé de remplir la requête qui sera donc 
		 * communiquée à l'adresse cible. 
		 */
		try{
			
			Log.d("DEBUG","...ending request");
			//on inscrit le délimiteur final dans la requete. cette ligne signifie au server la fin de la requete
			this.writer.append("--"+this.delimiter+"--"+"\r\n");
			//on écrit le flux dans la requete
			this.writer.flush();	
			//on ferme tous les outils d'écriture
			this.writer.close();
			this.os.close();
			Log.d("DEBUG","...request ended");		
		}catch(IOException e){
			Log.d("end sending IO Exception", e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("end sending closing outputstream exception :"+e.toString());
			
		}	
		
	}
	
	public String readResponseStatus() throws HttpTransfertException {
		/*fonction qui permet de lire le code réponse du serveur après qu'on 
		 * lui ai adressé une requete, cette méthode doit donc etre appelé
		 * après avoir terminé l'écriture de la requete (après un endSending).
		 */
		int response = 0;
		String response2="";
		try{
			Log.d("DEBUG","...trying to read status");
			//on récupere le code retour envoyé par le serveur 
			response = this.connection.getResponseCode();
			//on récupère la réponse envoyé par le serveur:
			//on commence par ouvrir un flux de lecture
			InputStream fluxRetour = this.connection.getInputStream();
			//mise en place d'un mécanisme de lecture-ecriture
        	int retourLecture2;	        	
        	StringBuffer buffRecep = new StringBuffer();
        	while((retourLecture2 = fluxRetour.read())!=-1){
        		buffRecep.append((char)retourLecture2);
        	}
        	//on stocke la réponse lue
        	response2 = buffRecep.toString();
        	Log.d("reponse","reponse du serveur:"+response2);
			Log.d("DEBUG","...status read, end of transmission, status"+String.valueOf(response));
			//Si une exception est levée on génére un réponse en locale
		}catch(IOException e){
			Log.d("reading  IO Exception, returned response equals to 0 ", e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("readin server response exception :"+e.toString());
		}
		//renvoi de la réponse
		return response2 ;
		
	}
	
	
	
	
	
	

}
