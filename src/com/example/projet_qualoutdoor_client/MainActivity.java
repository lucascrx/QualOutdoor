package com.example.projet_qualoutdoor_client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.example.projet_qualoutdoor_client_http.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
	Button boutonbdd = null;//poign�e qui permetra d'avoir le controle sur le bouton de cr�ation de la bdd
	TextView destmail = null;//poign�e qui permet d'avoir l'adresse mail destination
	
	//POIGNEE ET WIDGETS RELATIF A LA BASE DONNEE
	
	TextView dbpanel;//poignee qui permet d'avoir le controle sur le textview affichiant l'�tat de la cr�ation de la bdd
	//Ci dessous les poign�es permettant de recuperer les valeurs inscrites dans la bdd
	//premiere ligne
	TextView num10 = null ;TextView num11 = null ;TextView num12 = null ;TextView num13 = null ;
    //deuxieme ligne
	TextView num20 = null ;TextView num21 = null ;TextView num22 = null ;TextView num23 = null ;
	//troisi�e ligne
	TextView num30 = null ;TextView num31 = null ;TextView num32 = null ;TextView num33 = null ;
	
	//input stream engendr� par la lecture de la base de donn�e:
	InputStream dbstream=null;
	
	
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
     //   filename = (EditText)findViewById(R.id.filename);
     //   filecontent = (EditText)findViewById(R.id.filecontent);
        
        tvMail = (TextView)findViewById(R.id.TVmail);    
        cbMail = (CheckBox)findViewById(R.id.cbmail);
        destmail = (EditText)findViewById(R.id.targetmail);
        
        //INITIALISATION DE LA BASE DE DONNEE:
        dbpanel = (TextView)findViewById(R.id.bddprinter);
        boutonbdd = (Button)findViewById(R.id.buttonbdd);
        num10 = (TextView)findViewById(R.id.v10); num11 = (TextView)findViewById(R.id.v11); num12 = (TextView)findViewById(R.id.v12) ; num13 = (TextView)findViewById(R.id.v13);
        num20 = (TextView)findViewById(R.id.v20); num21 = (TextView)findViewById(R.id.v21); num22 = (TextView)findViewById(R.id.v22) ; num23 = (TextView)findViewById(R.id.v23);
        num30 = (TextView)findViewById(R.id.v30); num31 = (TextView)findViewById(R.id.v31); num32 = (TextView)findViewById(R.id.v32) ; num33 = (TextView)findViewById(R.id.v33);
        
        
    	boutonbdd.setOnClickListener(new View.OnClickListener() {
    	      @Override
    	      public void onClick(View v) {
    	    	//LA PREMIERE CHOSE A FAIRE EST DE REMPLIR LA BASE DE DONNEES AVEC LES CHAMPS FOURNIS
    	    	  
    	    	  dbpanel.setText("from wigdet to csv file via bdd:");//initialisation du panel
    	    	  
    	    	  //on initialise donc la bdd
    	    	 //poign�e sur l'instance qui transfere nos instructions sur la bdd
    	    	 SQLConnector connecteur = new SQLConnector(thisActivity);//on instancie un nouveau connecteur ce qui provoque la creation d'un createur de bdd
    	          connecteur.open();//la bdd est gener�e � partir du cr�ateur
  	    		
    	          
  	    		//IL FAUDRAIT VERIFIER QUE TOUS LES CHAMPS DE LA BASE DE DONNEE SONT NON NULL
  	    		
    	    	try{
    	    	  
    	    	  
  	    		//on r�cupere les entiers entr�s
  	    		int i10 = Integer.parseInt(num10.getText().toString());int i11 = Integer.parseInt(num11.getText().toString());int i12 = Integer.parseInt(num12.getText().toString());int i13 = Integer.parseInt(num13.getText().toString());
  	    		int i20 = Integer.parseInt(num20.getText().toString());int i21 = Integer.parseInt(num21.getText().toString());int i22 = Integer.parseInt(num22.getText().toString());int i23 = Integer.parseInt(num23.getText().toString());
  	    		int i30 = Integer.parseInt(num30.getText().toString());int i31 = Integer.parseInt(num31.getText().toString());int i32 = Integer.parseInt(num32.getText().toString());int i33 = Integer.parseInt(num33.getText().toString());
  	    		//on pr�pare les lignes de la bdd stock�es sous forme d'arrayList
  	    		//premiere ligne
  	    		ArrayList<Integer> ligne1 = new ArrayList<Integer>();
  	    		ligne1.add(i10);ligne1.add(i11);ligne1.add(i12);ligne1.add(i13);
  	    		//deuxieme ligne
  	    		ArrayList<Integer> ligne2 = new ArrayList<Integer>();
  	    		ligne2.add(i20);ligne2.add(i21);ligne2.add(i22);ligne2.add(i23);
  	    		//troisieme ligne
  	    		ArrayList<Integer> ligne3 = new ArrayList<Integer>();
  	    		ligne3.add(i30);ligne3.add(i31);ligne3.add(i32);ligne3.add(i33);
  	    		
  	    		//insertion des lignes dans la base de donn�es
  	    		if(connecteur.insertLine(ligne1) != -1){
  	    			dbpanel.append("insertion ligne 1 ok; ");
  	    			Log.d("insert L1 ","OK");
  	    		}
  	    		if(connecteur.insertLine(ligne2) != -1){
  	    			dbpanel.append("insertion ligne 2 ok; ");
  	    			Log.d("insert L2 ","OK");
  	    		}
  	    		if(connecteur.insertLine(ligne3) != -1){
  	    			dbpanel.append("insertion ligne 3 ok; ");
  	    			Log.d("insert L3 ","OK");
  	    		}
  	    		dbpanel.append("insertion termin�e; ");
  	    		
  	    		//MAINTENANT IL FAUT A PARTIR DE LA BASE DE DONNEES CONSTRUIRE L'INPUT STREAM
    	    	  
  	    		dbstream = connecteur.getInputStream();
  	    		dbpanel.append("Stream g�n�r�; "); 
    	    	  
    	        connecteur.close();
    	    	}catch(NumberFormatException e){
    	    		Toast toast = Toast.makeText(getApplicationContext(), "please complete all fields of the db with numbers", Toast.LENGTH_SHORT);
	    			toast.show();
    	    		
    	    	}
    	        
    	      }
    	    });
   
       
        //cliquer sur le bouton aura pour effet de d�clancher l'upload du fichier �crit par l'utilisateur
        
		bouton.setOnClickListener(new View.OnClickListener() {    
			//on va definir les param�tres � utiliser pour l'upload dans la fonction onClick
	    	public void onClick(View v){
	    		try{
	    		//on verifie que le fichier � envoyer issu de la bdd existe bien
	    		
	    		if(dbstream==null){
	    			throw new FileToSendException("csv file not created!");
	    		}
	    		
	    		
	    		/*
	    		if(filename.getText().length()==0||filecontent.getText().length()==0){
	    			//getion du cas o� un des 2 champs est vide
	    			Toast toast = Toast.makeText(getApplicationContext(), "please complete all fields", Toast.LENGTH_SHORT);
	    			toast.show();
	    		}
	    		else{
	    		*/
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
		    		
		    		//nom g�n�r� avec un TIMESTAMP
		    		String name = "file"+System.currentTimeMillis();//filename.getText().toString();
		    		//on r�cup�re le contenu...
		    		//String contentString = filecontent.getText().toString();
		    		//..que l'on transforme en InputStream
		    		InputStream content = dbstream;//new ByteArrayInputStream(contentString.getBytes());
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
		    		
		    		
		    		
	    		}catch(FileToSendException e){
	    			Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
	    			toast.show();
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
