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

	//private DataBaseTreeManager managerWriter;
	private ByteArrayOutputStream file;//objet qui récupère ce que l'on écrit
	private SQLConnector connecteur;
	private OnTaskCompleted callback;//l'objet sur lequel appliquer la methode de callbak une fois la tache terminée
	//SOLUTION PROVISOIRE
	private int user;
	private int group;
	private String comments;
	
	private ProgressDialog progressDialog;	
	
	public FileGenerator(SQLConnector conn,int usr,int gpe,String com,OnTaskCompleted cb){//DataBaseTreeManager manager){
		//this.managerWriter = manager;
		this.file= new ByteArrayOutputStream();
		this.comments=com;
		this.user=usr;
		this.group=gpe;
		this.connecteur=conn;
		this.callback=cb;
		Log.d("DEBUG WRITER","01");
	}
	
	public void nodeRetranscription(DataBaseTreeManager managerWriter){
		Log.d("DEBUG WRITER","12");
		try{
			//si on pointe une feuille, on va chercher ses détails pour les écrire
			if(managerWriter.getMaxSide()-managerWriter.getMinSide()==1){
				int refFeuille = managerWriter.getCurrentReference();//on récupere la reference de la feuille
				ArrayList<String> details = connecteur.getLeafDetails(refFeuille);//on demande ses détails au connecteur
				//on peut maintenant supprimer cette feuille et sa reference de la bdd.
				//managerWriter.deleteCurrentNode();//suppression de la reference
				//connecteur.deleteLeaf(refFeuille);//suppression de la feuille.
				this.file.write("[".getBytes());
				int compteurVirgule1 = 1;
				for(String field : details){//on inscrit à la suite ses détails
					this.file.write(field.getBytes());
					if(compteurVirgule1!=details.size()){
						this.file.write(",".getBytes());
					}
					compteurVirgule1++;
				}
				this.file.write("]".getBytes());
			}else{//on pointe un noeud non feuille on ecrit sa reference et on recommence avec ses fils
				int refNode = managerWriter.getCurrentReference();
				this.file.write(("["+refNode+",").getBytes());//on écrit le détail du noeud
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
				this.file.write("]".getBytes());
			}
			}catch(DataBaseException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
	}
	
	
	public void completeRetranscription(String comments,int group, int user, DataBaseTreeManager managerWriter) throws DataBaseException{
		try {
			//on verifie s'il ya des feuilles à envoyer:
			if(this.connecteur.hasLeaf()){	
				this.file.write(("[["+comments+"],["+group+",["+user+",").getBytes());//ouverture du fichier
				this.nodeRetranscription(managerWriter);
				Log.d("DEBUG WRITER","13");
				this.file.write(("]]]").getBytes());
				Log.d("DEBUG WRITER","131");
			}else{
				throw new DataBaseException("no leaf to be write!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	@Override
	protected ByteArrayOutputStream doInBackground(Void... params) {
		Log.d("DEBUG WRITER","11");
		try {
			completeRetranscription(this.comments,this.group, this.user, this.connecteur.prepareManager());
			return this.file;
		} catch (DataBaseException e) {//Dans le cas ou il n'y a pas de feuilles à écrire
			e.printStackTrace();
			return null;
		}
		
		
	}

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
