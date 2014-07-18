package com.example.projet_qualoutdoor_client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.example.projet_qualoutdoor_client_http.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*Main activity est la classe pirincipale de l'application, est permet de mettre en place le layout
 * De r�cup�rer le contenu et le nom du fichier cr�� par l'utilisateur pour le transmettre � un ou
 * des DataSending Managers qui seront initialis�s dans cette classe*/

public class MainActivity extends Activity implements OnTaskCompleted {
	
	//on a besoin de recuperer l'activit� comme variable de classe car des m�thodes
	//appartenant � des classes diff�rentes auront besoin d'acceder ou bien � l'activit�
	//ou bien � l'ojet OnTaskCompleted
	static Activity thisActivity = null;
	
	/*On d�clare en variable globale de classe les poign�es agissants sur des vues car
	 * elles seront utilis�es aussi par les sous classes*/
	EditText filename = null;//poign�e avec laquelle on recupere le nom du fichier
	EditText filecontent = null;//poign�e avec laquelle on recupere le contenu du fichier
	CheckBox cbHttp = null;//poign�e qui permet de savoir si l'option envoi en http est choisie
	CheckBox cbFtp = null;//poign�e qui permet de savoir si l'option envoi en ftp est choisie
	CheckBox cbMail = null;//poign�e qui permet de savoir si l'option envoi par mail est choisie
	TextView tvHttp = null;//poign�e qui permet d'afficher le resultat du transfert http
	TextView tvFtp = null;//poign�e qui permet d'afficher le resultat du transfert ftp
	TextView tvMail = null;//poign�e qui permet d'afficher le resultat du transfert mail
	Button bouton = null;//poign�e qui permetra d'avoir le controle sur le bouton d'envoi

	TextView destmail = null;//poign�e qui permet d'avoir l'adresse mail destination
	
    
	
	/*fonction appell�e � la cr�ation de l'activit�,
	 *on affecte chaque poign�e � leur vue correspondante*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity=this;
        
        //on r�cup�re les �l�ments du layout qui nous interresse
        bouton = (Button)findViewById(R.id.button);
        tvHttp = (TextView)findViewById(R.id.TVhttp);
        tvFtp = (TextView)findViewById(R.id.TVftp);    
        cbHttp = (CheckBox)findViewById(R.id.cbhttp);
        cbFtp = (CheckBox)findViewById(R.id.cbftp);
        filename = (EditText)findViewById(R.id.filename);
        filecontent = (EditText)findViewById(R.id.filecontent);
        
        tvMail = (TextView)findViewById(R.id.TVmail);    
        cbMail = (CheckBox)findViewById(R.id.cbmail);
        destmail = (EditText)findViewById(R.id.targetmail);
        
       
        //cliquer sur le bouton aura pour effet de d�clancher l'upload du fichier �crit par l'utilisateur
        
		bouton.setOnClickListener(new View.OnClickListener() {    
			//on va definir les param�tres � utiliser pour l'upload dans la fonction onClick
	    	public void onClick(View v){
	    		
	    		if(filename.getText().length()==0||filecontent.getText().length()==0){
	    			//getion du cas o� un des 2 champs est vide
	    			Toast toast = Toast.makeText(getApplicationContext(), "please complete all fields", Toast.LENGTH_SHORT);
	    			toast.show();
	    		}
	    		else{
	    			
	    			
		    		/*Dans le projet il n'y a pour l'instant besoin que d'envoyer un seul fichier
		    		 * mais comme ce point n'est pas encore sur on d�finit une fonction capable de
		    		 * passer au DataSendingManager une hashMap d'input : param�tres �l�mentaires et une
		    		 * hashMap de fichiers*/
		    		
		    		/*ATTENTION CHANGEMENT LES INPUT ETANT PROPRE A HTTP IL EST MAINTENANT INCONCEVABLE
		    		 *DE PLACER A CE NIVEAU DU CODE UNE CARACTERISTIQUE SI SPECIFIQUE A UN PROTOCOLE
		    		 *D'ENVOI, ON NE GARDE DONC QUE LA HASMAP DE FICHIER ET LES INPUTS SERONT FIXES
		    		 *DANS LA CLASSE PROPRE A HTTP MAIS DU COUP INNACCESSIBLES PAR L'UTILISATEUR
		    		 */
		    			//Hashmap d'input �l�mentaires : si on envoie pas d'input simples, la hashmap sera vide
		    			//HashMap<String,String> inputToSend = new HashMap<String,String>();
		    		
		    		
		    		//Hashmap de fichiers:: si on envoie pas de fichiers, la hashmap sera vide
		    		HashMap<String,FileToUpload> filesToSend = new HashMap<String,FileToUpload>();
		    		
		    		//ICI ON RECUPERE LES OBJETS A ENVOYER DANS LA REQUETE QUE L'ON PLACE DANS LES HASHMAPS
		    		
		    		//dans notre cas il n'y a qu'un seul fichier que l'on r�cup�re avec les poign�es pr�c�demment d�finies
		    		//on r�cup�re le Nom
		    		String name = filename.getText().toString();
		    		//on r�cup�re le contenu...
		    		String contentString = filecontent.getText().toString();
		    		//..que l'on transforme en InputStream
		    		InputStream content = new ByteArrayInputStream(contentString.getBytes());    		
		    		//on construit donc une instance de fileToUpload
		    		FileToUpload monFichier = new FileToUpload(name,content);
		    		//on le place dans la hashmap en fixant le nom du champs du formulaire le fichier se rapportera
		    		//Ce champ a �t� d�lib�r�ment fix� en dur et non laiss� au choix de l'utilisateur!
		    		filesToSend.put("uploadedFile", monFichier);
		    		
		    		//LES HASHMAPS SONT PRETES ON PREPARE LE DATA SENDING MANAGER
		    	
		    		
		    		//en fonction de l'option d'envoi choisie on fixe les derniers param�tres pour initialiser les
		    		//data sending manager

		    		
		    		//CI DESSUS ON NE PEUT CHOISIR QU'UN SEUL PROTOCOLE CAR C'EST UNIQUEMENT LE 
		    		//PREMIER TRANSFERT A LANCER, SI PLUSIEURS TRANSFERT CHOISIS, ON UTILISERA
		    		//LA METHODE DE CALLBACK DE FACON A FAIRE CES TRANSFERTS L'UN APRES L'AUTRE
		    			
		    			
		    		//cas ou HTTP est choisi
		    		if(cbHttp.isChecked()){
		    			//on fixe l'adresse du serveur http comme adresse cible
		    			String url = "http://192.168.0.4:8080/upload";
		    			//on initialise donc le DataSendingManager en lui indiquant en plus la vue sur
		    			//laquelle il affichera le r�sultat de l'op�ration et le protocole � mettre en oeuvre
		    			DataSendingManager managerHTTP = new DataSendingManager(url,filesToSend,tvHttp,"http",(OnTaskCompleted)thisActivity);
		    			//...puis on lance le manager
		    			managerHTTP.execute();
		    		}
		    		
		    		//cas ou FTP est choisi
		    		else if(cbFtp.isChecked()){
		    			//on fixe l'adresse du serveur ftp comme adresse cible
		    			String url = "192.168.0.4";
		    			//on initialise donc le DataSendingManager en lui indiquant en plus la vue sur
		    			//laquelle il affichera le r�sultat de l'op�ration et le protocole � mettre en oeuvre
		    			DataSendingManager managerFTP = new DataSendingManager(url,filesToSend,tvFtp,"ftp",(OnTaskCompleted)thisActivity);
		    			//...puis on lance le manager
		    			managerFTP.execute();
		    		}
		    		
		    		//CAS OU LE MAIL EST CHOISI

		    		else if(cbMail.isChecked()){
		    			//on fixe l'adresse du serveur ftp comme adresse cible
		    			String url = destmail.getText().toString();
		    			sendFileByEmail(url,filesToSend);
		    		}
		    		
		    		//cas ou aucun protocole n'est choisi 
		    		if(!cbHttp.isChecked() && !cbFtp.isChecked() && !cbMail.isChecked()){
		    			//on affiche un message � l'utilisateur
		    			Toast toast = Toast.makeText(getApplicationContext(), "please choose a protocole", Toast.LENGTH_SHORT);
		    			toast.show();
		    		}
		    		
		    		
		    		
	    		}
	    		
	    	}
	   		
		});
    }
    
    public void onTaskCompleted(String protocole, HashMap<String,FileToUpload> filesSended){
    	if(protocole.equals("http")){//cas o� le transfert HTTP vient de terminer
    		//LA AUSSI CHOIX EXCLUSIF
    		if(cbFtp.isChecked()){//si l'option ftp est choisie on lance un DataSendingManagerOrient� FTP
    			//on fixe l'adresse du serveur ftp comme adresse cible
    			String url = "192.168.0.4";
    			//on initialise donc le DataSendingManager en lui indiquant en plus la vue sur
    			//laquelle il affichera le r�sultat de l'op�ration et le protocole � mettre en oeuvre
    			//on lui passe comme fichier, les fichiers envoy�s par la tache pr�c�dente
    			DataSendingManager managerFTP = new DataSendingManager(url,filesSended,tvFtp,"ftp",(OnTaskCompleted)thisActivity);
    			//...puis on lance le manager
    			managerFTP.execute();
    		}else if(cbMail.isChecked()){
    			//on fixe l'adresse du serveur ftp comme adresse cible
    			String url = destmail.getText().toString();
    			sendFileByEmail(url,filesSended);
    		}	
    	}
    	else if(protocole.equals("ftp")){//SI LE TRANSFERT FTP EST FINI ON REGARDE S'IL FAUT ENVOYER UN MAIL
    		if(cbMail.isChecked()){
    			//on fixe l'adresse du serveur ftp comme adresse cible
    			String url = destmail.getText().toString();
    			sendFileByEmail(url,filesSended);
    		}	
    	
    	}
    }
    
    public static void sendFileByEmail(String dest, HashMap<String,FileToUpload> filesToUpload){
    	//On se place dans le cas ou un seul fichier est dans la hashmap:
    	
		//on r�cupere donc la premi�re entr�e de la hashmap
		String cle = filesToUpload.keySet().iterator().next();
		//on r�cup�re le nom du fichier
		String fileName = filesToUpload.get(cle).getFileName();
		//on r�cup�re le contenu du fichier
		InputStream content = filesToUpload.get(cle).getContent();
		
		//on verifie le bon format de l'adresse fournier
		final Pattern rfc2822 = Pattern.compile(
		        "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
		);
		//dans le cas ou le format de l'adresse est mauvais
		if (!rfc2822.matcher(dest).matches()) {
			Toast toast = Toast.makeText(thisActivity, "invalid email address", Toast.LENGTH_SHORT);
			toast.show();
		}else{
			//ON PREPARE INTENT DE MAIL 
			Intent email = new Intent(Intent.ACTION_SEND);
			email.putExtra(Intent.EXTRA_EMAIL, new String[]{dest});//destinataire		  
			email.putExtra(Intent.EXTRA_SUBJECT, "my csv file");//sujet
			String stats;//string qui r�cupere le fichier pour l'afficher dans le contenu du mail
			java.util.Scanner s = new java.util.Scanner(content).useDelimiter("\\A");
	        if(s.hasNext()){
	        	stats = s.next();
	        }else{
	        	stats="";
	        	
	        }
	        //on edite donc le contenu du mail
			email.putExtra(Intent.EXTRA_TEXT, "here are my csv measures : \r\n \r\n \r\n "+stats);
			email.setType("message/rfc822");//type du mail
			thisActivity.startActivity(Intent.createChooser(email, "Choose an Email client :"));//on lance l'intent
    	
		}
    }		
 
}
