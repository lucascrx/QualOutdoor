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
	 * Cette classe va examiner la base de donnée pour produire le fichier JSARRAY associé
	 * à l'aide de son propre manager elle va naviguer dans la table de référence de la bdd
	 * à restranscrir et le contenu sera stocké dans un ByteArrayOutputStream
	 * 
	 * Elle s'executera dans une tache asynchrone, et renverra un call back une fois achevée*/

	private ByteArrayOutputStream file;//objet qui récupère ce que l'on écrit
	private SQLConnector connecteur;//possede un connecteur pour obtenir des détails sur la table de mesures
	private OnTaskCompleted callback;//l'objet sur lequel appliquer la methode de callbak une fois la tache terminée
	//SOLUTION PROVISOIRE
	private String comments;//les commentaires à inserer dans le fichier
	
	private ProgressDialog progressDialog;	
	
	public FileGenerator(SQLConnector conn,String com,OnTaskCompleted cb){
		this.file= new ByteArrayOutputStream();
		this.comments=com;
		this.connecteur=conn;
		this.callback=cb;
		Log.d("DEBUG WRITER","01");
	}
	
	/*Fonction recursive qui permet de restranscrire un noeud dans l'outputStream : ses détails, et son contenu (ses fils)*/
	public void nodeRetranscription(DataBaseTreeManager managerWriter){
		Log.d("DEBUG WRITER","12");
		try{
			//si on pointe une feuille, on va chercher ses détails pour les écrire
			if(managerWriter.getMaxSide()-managerWriter.getMinSide()==1){
				int refFeuille = managerWriter.getCurrentReference();//on récupere la reference de la feuille
			
				ArrayList<String> details = connecteur.getLeafDetails(refFeuille);//on demande ses détails au connecteur
				
				Log.d("DEBUG DELETING","LEAF "+refFeuille+" DELETED");
				this.file.write("[".getBytes());//ouverture d'un noeud dans le fichier correspondant à la feuille
				int compteurVirgule1 = 1;
				for(String field : details){//on inscrit à la suite ses détails
					this.file.write(field.getBytes());
					if(compteurVirgule1!=details.size()){
						this.file.write(",".getBytes());//pour chaque élément non dernier de la liste on les fait suivre d'un virgule
					}
					compteurVirgule1++;
				}
				this.file.write("]".getBytes());//on ferme le noued feuille du fichier
			}else{//on pointe un noeud non feuille on ecrit sa reference et on recommence avec ses fils
				int refNode = managerWriter.getCurrentReference();
				this.file.write(("["+refNode+",").getBytes());//on écrit le détail du noeud du fichier
				int compteurVirgule=1;
				ArrayList<int[]> children = managerWriter.getDirectChildSides();//on écrit récursivement les fils du noeud
				for (int[] child : children ){//pour chaque fils
					managerWriter.focusOn(child[0],child[1]);//on se place dessus
					nodeRetranscription(managerWriter);//on l'écrit
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
	 * puis une fois l'écriture terminée, elle prévient le connecteur de remettre à zero tout le systeme de stockage
	 * */
	public void completeRetranscription(String comments, DataBaseTreeManager managerWriter) throws DataBaseException{
		try {
			//on verifie s'il ya des feuilles à envoyer:
			if(this.connecteur.hasLeaf()){	
				this.file.write(("[["+comments+"],").getBytes());//ouverture du fichier

				this.nodeRetranscription(managerWriter);
				Log.d("DEBUG WRITER","13");
				this.file.write(("]").getBytes());
				Log.d("DEBUG WRITER","131");
				//une fois le fichier généré on remet à zéro tout le systeme de stockage
				this.connecteur.completeReset();
				
				
			}else{
				throw new DataBaseException("no leaf to be write!");
			}
		} catch (IOException e) {//capte les exceptions liées à l'écriture dans le buffer
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
		} catch (DataBaseException e) {//Dans le cas ou il n'y a pas de feuilles à écrire
			e.printStackTrace();
			return null;
		}
		
		
	}

	//recupère l'output stream généré par la tache principale et vérifie s'il est null ou pas, et appelle avec 
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
    	//permet d'avoir une interface graphique plus parlante avec remise à zero du code retour
	}
	
}
