package com.example.projet_qualoutdoor_client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class FileGenerator extends AsyncTask<Void, Void, ByteArrayOutputStream> {
	/*
	 * Cette classe va examiner la base de donn�e pour produire le fichier JSARRAY associ�
	 * � l'aide de son propre manager elle va naviguer dans la table de r�f�rence de la bdd
	 * � restranscrir et le contenu sera stock� dans un ByteArrayOutputStream
	 * 
	 * Elle s'executera dans une tache asynchrone, et renverra un call back une fois achev�e*/

	private ByteArrayOutputStream file;//objet qui r�cup�re ce que l'on �crit
	private SQLConnector connecteur;//possede un connecteur pour obtenir des d�tails sur la table de mesures
	private OnTaskCompleted callback;//l'objet sur lequel appliquer la methode de callbak une fois la tache termin�e
	//SOLUTION PROVISOIRE
	private String comments;//les commentaires � inserer dans le fichier
	
	private ProgressDialog progressDialog;	
	
	public FileGenerator(SQLConnector conn,String com,OnTaskCompleted cb){
		this.file= new ByteArrayOutputStream();
		this.comments=com;
		this.connecteur=conn;
		this.callback=cb;
		Log.d("DEBUG WRITER","01");
	}
	
	/*Fonction recursive qui permet de restranscrire un noeud dans l'outputStream : ses d�tails, et son contenu (ses fils)*/
	public void nodeRetranscription(DataBaseTreeManager managerWriter){
		Log.d("DEBUG WRITER","12");
		try{
			//si on pointe une feuille, on va chercher ses d�tails pour les �crire
			if(managerWriter.getMaxSide()-managerWriter.getMinSide()==1){
				int refFeuille = managerWriter.getCurrentReference();//on r�cupere la reference de la feuille
			
				ArrayList<String> details = connecteur.getLeafDetails(refFeuille);//on demande ses d�tails au connecteur
				
				Log.d("DEBUG DELETING","LEAF "+refFeuille+" DELETED");
				this.file.write("[".getBytes());//ouverture d'un noeud dans le fichier correspondant � la feuille
				int compteurVirgule1 = 1;
				for(String field : details){//on inscrit � la suite ses d�tails
					this.file.write(field.getBytes());
					if(compteurVirgule1!=details.size()){
						this.file.write(",".getBytes());//pour chaque �l�ment non dernier de la liste on les fait suivre d'un virgule
					}
					compteurVirgule1++;
				}
				this.file.write("]".getBytes());//on ferme le noued feuille du fichier
			}else{//on pointe un noeud non feuille on ecrit sa reference et on recommence avec ses fils
				int refNode = managerWriter.getCurrentReference();
				this.file.write(("["+refNode+",").getBytes());//on �crit le d�tail du noeud du fichier
				int compteurVirgule=1;
				ArrayList<int[]> children = managerWriter.getDirectChildSides();//on �crit r�cursivement les fils du noeud
				for (int[] child : children ){//pour chaque fils
					managerWriter.focusOn(child[0],child[1]);//on se place dessus
					nodeRetranscription(managerWriter);//on l'�crit
					if(compteurVirgule!=children.size()){//si ce n'est pas le dernier fils de la list on met une virgule
						this.file.write(",".getBytes());
					}
					compteurVirgule++;	
				}
				this.file.write("]".getBytes());//fermeture du noeud du fichier
			}
			}catch(DataBaseException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
	}
	
	/*Fonction qui permet d'initialiser nodeRetranscription : elle permet l'insertion des commentaires d'entete
	 * puis une fois l'�criture termin�e, elle pr�vient le connecteur de remettre � zero tout le systeme de stockage
	 * */
	public void completeRetranscription(String comments, DataBaseTreeManager managerWriter) throws DataBaseException{
		try {
			//on verifie s'il ya des feuilles � envoyer:
			if(this.connecteur.hasLeaf()){	
				this.file.write(("[["+comments+"],").getBytes());//ouverture du fichier

				this.nodeRetranscription(managerWriter);
				Log.d("DEBUG WRITER","13");
				this.file.write(("]").getBytes());
				Log.d("DEBUG WRITER","131");
				//une fois le fichier g�n�r� on remet � z�ro tout le systeme de stockage
				this.connecteur.completeReset();
				
				
			}else{
				throw new DataBaseException("no leaf to be write!");
			}
		} catch (IOException e) {//capte les exceptions li�es � l'�criture dans le buffer
			e.printStackTrace();
		}
	
	}
	
	//tache principale en background
	@Override
	protected ByteArrayOutputStream doInBackground(Void... params) {
		Log.d("DEBUG WRITER","11");
		try {
			completeRetranscription(this.comments, this.connecteur.prepareManager());
			return this.file;
		} catch (DataBaseException e) {//Dans le cas ou il n'y a pas de feuilles � �crire
			e.printStackTrace();
			return null;
		}
		
		
	}

	//recup�re l'output stream g�n�r� par la tache principale et v�rifie s'il est null ou pas, et appelle avec 
	//ce dernier le callback 
	@Override
	  protected void onPostExecute(ByteArrayOutputStream result) {
		  Log.d("DEBUG WRITER","15");
		  	if(result==null){
		  		Log.d("DEBUG GENERATOR", "NO TEXT GENERATED");
		  	}else{
		  		Log.d("DEBUG GENERATOR", result.toString());
		  	}
	    	this.callback.onFileReady(result);
	    	progressDialog.dismiss();
	    }
	
	@Override
	protected void onPreExecute() {
    	//CONFIGURATION DE LA BARRE DE PROGRES
    	progressDialog= ProgressDialog.show((Context)this.callback, "CREATING FILE TO SEND","FETCHING DATA FROM DB", true);
    	//permet d'avoir une interface graphique plus parlante avec remise � zero du code retour
	}
	
}
