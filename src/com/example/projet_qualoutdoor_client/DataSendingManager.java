package com.example.projet_qualoutdoor_client;

import java.io.InputStream;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

/*Ce script ne mettra pas en oeuvre des politiques complexes d'ordonnancement
 * on utilisera donc simplement une classe asynchrone dans laquelle on 
 * inserera les méthodes relatives à la transmission de données
 */
public class DataSendingManager extends AsyncTask<Void, Void, String> {

	/*Un DataSendingManager permet d'initialiser des connections de différents types selon
	 * les paramètres avec lesquels il a été initialisé et de les utiliser pour envoyer
	 * des requêtes. 
	 * 
	 * Il est donc défini avec des attributs qui seront tous les paramètres COMMUNS des différents 
	 * type de connexion : une analogie un peu tordue peut être que ces attributs sont le "PGCD" de l'ensemble 
	 * des paremètres necessaires pour chaque connexion :). 
	 * 
	 */	
	private String target;//l'addresse du serveur cible
	private HashMap<String,FileToUpload>  filesToUploadlist;//les fichiers à envoyers
	private TextView printer;//la vue pour afficher le résultat
	private String protocole;//le protocole à utiliser
	//Ces paramètres réprésentent donc tous les paramètres communs aux différents types de connexions
	
	private OnTaskCompleted callback;//l'objet sur lequel appliquer la methode de callbak une fois la tache terminée
	
	private ProgressDialog progressDialog;	
	
	//Constructeur avec lequel on intitalise le DataSendingManager
	public DataSendingManager(String url, HashMap<String,FileToUpload> filesToUpload,TextView vue,String proto,OnTaskCompleted cb){
		this.target=url;
		this.filesToUploadlist=filesToUpload;
		this.printer=vue;
		this.protocole=proto;
		this.callback=cb;
	}
	
	/*Fonction qui prend un temps relativement long par rapport au reste du code, on l'execute donc en arrière plan
	 * cette fonction ouvre les connection, effectue le transfert et retourne la réponse */
    protected String doInBackground(Void... params) {
    	/*IMPORTANT: afin de factoriser le code une fonction commmune à tous les sender à été créée
    	 * et permet d'envoyer un fichier or il existe des connexions qui peuvent supporter un envoie de fichier
    	 * multiples comme les connexions http mais il existes aussi des connexion qui dont l'existence est fondée 
    	 * sur le tranfert d'un et un seul fichier comme les connexions ftp. Ainsi cette méthode commune d'envoi
    	 * de fichier permettra l'envoi d'un fichier unique */
    	
    	//On se place dans le cas ou un seul fichier est dans la hashmap:
    	
    		//on récupere donc la première entrée de la hashmap
    		String cle = this.filesToUploadlist.keySet().iterator().next();
    		//on récupère le nom du fichier
    		String fileName = this.filesToUploadlist.get(cle).getFileName();
    		//on récupère le contenu du fichier
    		InputStream content = this.filesToUploadlist.get(cle).getContent();
    		//nouveaux sender
    		Sender sender = null;
    		
    		//Si le manager a été initialisé avec HTTP: on fera pointer la poignée vers un HTTPsender
    		if(this.protocole.equals("http")){
    			/*ici comme tous les paramètres communs des connexions ont déjà été fixé
    			*les Sender sont initialisés avec seulement les paramètres qui leurs sont spécifiques.
    			*
    			*ICI le seul paramètre spécifique à HTTP est le nom de l'entrée du formulaire à laquelle
    			*se rapporte le fichier: que l'on fixe donc (sans avis de l'utilisateur)
    			*/
    		 sender = new HttpFileSender("uploadedFile");
    		}
    		//Si le manager a été initialisé avec FTP: on fera pointer la poignée vers un FTPsender
    		else if(this.protocole.equals("ftp")){
    			/*
    			 * idem:
    			 * 
    			 * ICI les paramètres spécifiques à FTP sont le login et le mot de passe à présenter 
    			 * au serveur et le chemin de stockage dans le repertoire du serveur. Là aussi ce
    			 * n'est pas l'utilisateur qui les choisit
    			*/
    		 sender = new FtpFileSender("client", "alsett", "/myUploads/");
    		}
    		//ON RAJOUTE LE MAIL ICI!!
    		else if(this.protocole.equals("mail")){
    			//on etoffera le constructeur et la classe MailFileSender plus tard
    			sender = new MailFileSender();
    		}
    		
    		/*une fois le sender créé on peut donc appeler la méthode envoyerFichier qui comporte
    		*comme paramètres les attributs avec lesquells l'instance dataSendingManager a été crée
    		*ce sont donc les paramètres communs à toute connexion:
    		*
    		*on peut donc appeler cette methode sans savoir sur quel type de Sender on l'applique.
    		*/
    		String result = sender.envoyerFichier(this.target, fileName, content);
    		//envoyer fichier renvoie la réponse du serveur, on transfert cette derniere à la fonction de postexecution
    		return result;
    }
    
    /* Fonction executée après la fonction d'arrière plan, elle récupère la réponse du serveur
     * et l'affiche dans la vue avec laquelle le dataSendingManager a été créé.
     */
    @Override
    protected void onPostExecute(String result) {
    	//on affiche le résultat de l'opération dans la vue
    	this.printer.setText(result);
    	this.callback.onTaskCompleted(this.protocole, this.filesToUploadlist);
    	progressDialog.dismiss();
    }
    
    /*Fonction éxécutée avant la fonction d'arrière plan, elle permet juste d'initialiser ou de ré-initialiser
     * les affichages
     * */
    @Override
    protected void onPreExecute() {
    	//CONFIGURATION DE LA BARRE DE PROGRES
    	progressDialog= ProgressDialog.show((Context)this.callback, "SENDING DATA","USING "+this.protocole+" PROTOCOLE", true);
    	//permet d'avoir une interface graphique plus parlante avec remise à zero du code retour
    	this.printer.setText("transfert en cours");	    
     }
    
}