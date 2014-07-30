package com.example.projet_qualoutdoor_client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*Classe qui a pour but de faire l'intermediaire entre le code java de l'application
 * android et la base de donn�e SQLlite locale. Le code enverra des instructions � l'instance
 * de cette classe qui les convertira en des requ�tes SQL*/

public class SQLConnector {
	
	private SQLiteDatabase db;//base de donn�e sur laquelle agir.
	private SQLDataBaseCreator dbCreator;//createur de la base de donn�e
	
	private DataBaseTreeManager manager;//manager de l'arbre de la bdd
	
	//le connector garde en memoire les derniers MCC,MNC,NTC,Metric acc�d�s afin d'indiquer au
	//manager comment se d�placer dans l'arbre pour une nouvelle insertion.
	//ce quadruplet represe la position courante du manager apr�s insertion
	private int lastMCC;
	private int lastMNC;
	private int lastNTC;
	private int lastMetric;
	
	
	
	//le constructeur engendre un nouveau createur de bdd
	public SQLConnector(Context context) throws DataBaseException{
		this.dbCreator = new SQLDataBaseCreator(context);
		
	}
	
	//open() permet de produire la base de donn�e � partir du constructeur
	public void open() throws SQLException, DataBaseException{
		//on cr�e ou on ouvre la base de donn�es qui sera en acces lecture ecriture
		this.db = this.dbCreator.getWritableDatabase();
		//on change finalement on ne remet pas � zero la bdd
		//elle sera flush�e � chaque nouvel envoi.
		//on initialise le manager
		this.manager = new DataBaseTreeManager(this.db,this.dbCreator.getTableReference());
	}
	
	//fermeture de la base de donn�es
	public void close(){
		this.dbCreator.close();
	}
	
	
	/*fonction qui permet d'inserer une ligne correspondant � une mesure dans la table de reference*/
	public void insertReference(int MCC, int MNC, int NTC,int metric, int ref ){
		try{
			Log.d("DEBUG **** TREE","MCC_OLD : "+this.lastMCC+" MCC_NEW : "+MCC);
			Log.d("DEBUG **** TREE","MNC_OLD : "+this.lastMNC+" MNC_NEW : "+MNC);
			Log.d("DEBUG **** TREE","NTC_OLD : "+this.lastNTC+" NTC_NEW : "+NTC);
			Log.d("DEBUG **** TREE","METRIC_OLD : "+this.lastMetric+" Metric_NEW : "+metric);
			
			if(this.lastMCC==MCC){//si la nouvelle mesure est dans le MCC actuel
				if(this.lastMNC==MNC){//si la nouvelle mesure est dans le MNC actuel
					if(this.lastNTC==NTC){//si la nouvelle mesure est dans le NTC actuel
						if(this.lastMetric==metric){//si la nouvelle mesure est dans la metrique actuelle
							this.manager.insertLeaf(ref);
						}
						else{//sinon on remonte au NTC et on se place sur ou on cr�e l'intervalle correspondant � la metric
							this.manager.getFather();//on remonte au NTC
							this.manager.findOrCreate(metric);//on redescend dans la bonne metrique
							//le manager pointe � pr�sent sur la bonne m�trique: on cr�e donc la feuille
							this.manager.insertLeaf(ref);
							//mise a jour de la vielle metrique
							this.lastMetric = metric;
						}
					}else{//sinon on remonte au MNC et on se place sur ou on cr�e l'intervalle correspondant au NTC
						this.manager.getFather();//on remonte au NTC
						this.manager.getFather();//on remonte au MNC
						this.manager.findOrCreate(NTC);//on redescend dans le nouveau NTC
						this.manager.findOrCreate(metric);//on redescend dans la nvlle metrique
						//le manager pointe a pr�sent sur l'intervalle de la nvlle metrique : on cr�e donc la feuille
						this.manager.insertLeaf(ref);
						//mise a jour du vieux NTC et de la vielle metrique
						this.lastNTC=NTC;
						this.lastMetric = metric;
					}	
				}else{//dans de cas on remonte jusqu'au MCC pour chercher ou cr�er le MNC indiqu�
					this.manager.getFather();//on remonte au NTC
					this.manager.getFather();//on remonte au MNC
					this.manager.getFather();//on remonte au MCC
					this.manager.findOrCreate(MNC);//on redescend dans le nvx MNC
					this.manager.findOrCreate(NTC);//on redescend dans le nvx NTC
					this.manager.findOrCreate(metric);//on redescend dans la nvlle metrique
					//le manager pointe a pr�sent sur l'intervalle de la nvlle metrique: on cr�e donc la feuille
					this.manager.insertLeaf(ref);
					//Mise � jour des vieux MNC et NTC et metrique
					this.lastMNC=MNC;
					this.lastNTC=NTC;
					this.lastMetric = metric;
				}	
			}else{//dans ce cas il faut remonter � la racine pour retrouver ou cr�er le MCC indiqu�
				this.manager.getFather();//on remonte au NTC
				this.manager.getFather();//on remonte au MNC
				this.manager.getFather();//on remonte au MCC
				this.manager.getFather();//on remonte � la racine
				this.manager.findOrCreate(MCC);//on redescend dans le nvx MCC
				this.manager.findOrCreate(MNC);//on redescend dans le nvx MNC
				this.manager.findOrCreate(NTC);//on redescend dans le nvx NTC		
				this.manager.findOrCreate(metric);//on redescend dans la nvlle metrique
				//le manager pointe a pr�sent sur l'intervalle de la nvlle metrique: on cr�e donc la feuille
				this.manager.insertLeaf(ref);
				//mise a jour des vieux MCC, MNC et NTC et metrique
				this.lastMCC=MCC;
				this.lastMNC=MNC;
				this.lastNTC=NTC;
				this.lastMetric = metric;
			}
		}catch(DataBaseException e){
			e.printStackTrace();
		}
	}
	
	/*Methode qui insere une mesure dans la table des mesures, elle renvoie l'identifiant de la
	 * ligne ins�r�e ou -1 en cas de probleme*/
	public int insertData(long lat, long lng,String data){
		int id = -1;
		try{//on pr�pare la requete d'insertion, la date est g�n�r�e par SQL
			String insertQuery = "INSERT INTO "+this.dbCreator.getTableMeasure().getName()+" (DATE , LAT , LNG, DATA) VALUES ( CURRENT_TIMESTAMP ,"+lat+","+lng+", '"+data+"' );";
			db.execSQL(insertQuery);//Execution de la requete d'insertion
			Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);//on r�cup�re l'ID du dernier �l�ment ins�r�
			if(c.moveToFirst()){//si on retrouve bien la ligne ins�r�e on retrouve son ID
				id =  c.getInt(0);
			}
			else{
				throw new DataBaseException("SQL CONNECTOR : can't find ID of inserted measure ! ");			
			}
		}catch(DataBaseException e){
			e.printStackTrace();
		}
		return id;
	}
	
	
	
	/*
	 *Fonction qui permet l'insertion d'une mesure complete dans le r�seau de tables de Recorder
	 *la donn�e complete sera pr�sent� sous forme de hasmap de type FielD/Value
	 *sauf pour les parametres destin�s aux tables de m�trique ou le nom de la table sera indiqu�
	 * 
	 * 
	 */
	public void insertMeasure(HashMap<String,Number> contextList, HashMap<Integer,String> dataList){
		int MCC = (Integer) contextList.get("MCC");
		int MNC = (Integer) contextList.get("MNC");
		int NTC = (Integer) contextList.get("NTC");
		long lng = (Long) contextList.get("lng");
		long lat = (Long) contextList.get("lat");

		//INSERTIONS SUCESSIVES DANS LA TABLE DE MESURES
		//CHAQUE INSERTION DE DATA EST SUIVIE PAR UNE INSERTION ASSOCIEE DANS LA TABLE DE REFERENCE
		int ref;
		for(int dataType : dataList.keySet()){
			ref = this.insertData(lat,lng,dataList.get(dataType));//on insert la data dans la table de mesure
			this.insertReference(MCC, MNC, NTC, dataType, ref);//on recupere l'id donn�e pour referencer la data dans la table de reference
		}
		
		//TEST
		
		String select = "SELECT * FROM "+this.dbCreator.getTableReference().getName() ;
		Cursor c = this.db.rawQuery(select,null);
		String str = DatabaseUtils.dumpCursorToString(c);
		Log.d("DEBUG REFERENCE",str);
		c.close();
		
		String select2 = "SELECT * FROM "+this.dbCreator.getTableMeasure().getName();
		Cursor c2 = this.db.rawQuery(select2,null);
		String str2 = DatabaseUtils.dumpCursorToString(c2);
		Log.d("DEBUG MEASURE",str2);
		c2.close();
		
		
		
	}

	
	/*
	//methode qui permet de transformer le contenu de la table en un inputStream sous le format csv
	public InputStream getInputStream(){
		try{
			//on initialise la requete � executer sur la bdd : ici on selectionne tout
			//le r�sultat est stock� dans un objet de type Cursor
		    Cursor cursor = this.db.rawQuery("SELECT * FROM "+this.dbCreator.getTable("table_ref").getName(),null);
		  Log.d("inputStream creation","CURSOR OK");
		  //on initialise une String que l'on va remplir par it�ration
		    String str = "";
		    while(cursor.moveToNext()){
		    //� chaque ligne renvoy� par la requete on remplit la String avec les valeurs qu'elle contient	
		    	str = str +cursor.getInt(0)+","+cursor.getInt(1)+","+cursor.getInt(2)+","+cursor.getInt(3)+";";
		    }
			Log.d("inputStream creation", "STRING OK");
			Log.d("STRING TO SEND",str);
			//une fois la lecture du resultat finie on ferme le curseur
			cursor.close();
			//on transforme la string construite en inputstream
			InputStream is = new ByteArrayInputStream(str.getBytes());
			return is;
		}catch(DataBaseException e){
			e.printStackTrace();
		}
	}*/
	
	
	
	

}
