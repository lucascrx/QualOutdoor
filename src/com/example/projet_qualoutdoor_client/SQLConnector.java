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
	
	//le connector garde en memoire les derniers MCC,MNC,NTC acc�d�s afin d'indiquer au
	//manager comment se d�placer dans l'arbre pour une nouvelle insertion.
	//ce triplet represent la position courante du manager apr�s insertion
	private int lastMCC;
	private int lastMNC;
	private int lastNTC;
	
	
	
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
		this.manager = new DataBaseTreeManager(this.db,this.dbCreator.getTable("table_ref"));
	}
	
	//fermeture de la base de donn�es
	public void close(){
		this.dbCreator.close();
	}
	
	
	/*fonction qui permet d'inserer une ligne correspondant � une mesure dans la table de reference*/
	public void insertMeasureReference(int MCC, int MNC, int NTC, int ref ){
		try{
			Log.d("DEBUG **** TREE","MCC_OLD : "+this.lastMCC+" MCC_NEW : "+MCC);
			Log.d("DEBUG **** TREE","MNC_OLD : "+this.lastMNC+" MNC_NEW : "+MNC);
			Log.d("DEBUG **** TREE","NTC_OLD : "+this.lastNTC+" NTC_NEW : "+NTC);
			
			if(this.lastMCC==MCC){//si la nouvelle mesure est dans le MCC actuel
				if(this.lastMNC==MNC){//si la nouvelle mesure est dans le MNC actuel
					if(this.lastNTC==NTC){//si la nouvelle mesure est dans le NTC actuel
						
						this.manager.insertLeaf(ref);
					}else{//sinon on remonte au MNC et on se place sur ou on cr�e l'intervalle correspondant au NTC
						this.manager.getFather();//on remonte au MNC
						this.manager.findOrCreate(NTC);//on redescend dans le nouveau NTC
						//le manager pointe a pr�sent sur l'intervalle du nouveau NTC: on cr�e donc la feuille
						this.manager.insertLeaf(ref);
						//mise a jour du vieux NTC
						this.lastNTC=NTC;
					}	
				}else{//dans de cas on remonte jusqu'au MCC pour chercher ou cr�er le MNC indiqu�
					this.manager.getFather();//on remonte au MNC
					this.manager.getFather();//on remonte au MCC
					this.manager.findOrCreate(MNC);//on redescend dans le nvx MNC
					this.manager.findOrCreate(NTC);//on redescend dans le nvx NTC
					//le manager pointe a pr�sent sur l'intervalle du nouveau NTC: on cr�e donc la feuille
					this.manager.insertLeaf(ref);
					//Mise � jour des vieux MNC et NTC
					this.lastMNC=MNC;
					this.lastNTC=NTC;
				}	
			}else{//dans ce cas il faut remonter � la racine pour retrouver ou cr�er le MCC indiqu�
				this.manager.getFather();//on remonte au MNC
				this.manager.getFather();//on remonte au MCC
				this.manager.getFather();//on remonte � la racine
				this.manager.findOrCreate(MCC);//on redescend dans le nvx MCC
				this.manager.findOrCreate(MNC);//on redescend dans le nvx MNC
				this.manager.findOrCreate(NTC);//on redescend dans le nvx NTC		
				//le manager pointe a pr�sent sur l'intervalle du nouveau NTC: on cr�e donc la feuille
				this.manager.insertLeaf(ref);
				//mise a jour des vieux MCC, MNC et NTC
				this.lastMCC=MCC;
				this.lastMNC=MNC;
				this.lastNTC=NTC;
			}
		}catch(DataBaseException e){
			e.printStackTrace();
		}
	}
	
	/*Methode qui insere une mesure GEO TIME dans la table des mesures, elle renvoie l'identifiant de la
	 * ligne ins�r�e ou -1 en cas de probleme*/
	public int insertMeasure(long lng, long lat){
		int id = -1;
		try{//on pr�pare la requete d'insertion, la date est g�n�r�e par SQL
			String insertQuery = "INSERT INTO "+this.dbCreator.getTable("table_meas").getName()+" (DATE , LAT , LNG) VALUES ( CURRENT_TIMESTAMP ,"+lat+","+lng+");";
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
	 * Fonction qui permet d'inserer une ligne dans une table de metrique se rapportant � une
	 * mesure dont l'identifiant est id
	 * 
	 * comme les tables de metriques sont toutes compos�es de 2 colonnes ID et value l'insertion
	 * de la valeur pass� se fera toujours dans la 2e colonne
	 */
	public void insertMetric(String tableName, int id, Number value){
		try{
			TableDB table = this.dbCreator.getTable(tableName);
			String insertQuery = "INSERT INTO "+table.getName()+" ( ID_MEASURE , "+table.getColumsName().get(1)+" ) VALUES ( "+id+", "+value+" );";
			db.execSQL(insertQuery);
		}catch(DataBaseException e){
			e.printStackTrace();
		}
	}
	
	
	/*
	 *Fonction qui permet l'insertion d'une mesure complete dans le r�seau de tables de Recorder
	 *la donn�e complete sera pr�sent� sous forme de hasmap de type FielD/Value
	 *sauf pour les parametres destin�s aux tables de m�trique ou le nom de la table sera indiqu�
	 * 
	 * 
	 */
	public void insertMeasure(HashMap<String,Number> completeMeasure){
		int MCC = (Integer) completeMeasure.get("MCC");
		completeMeasure.remove("MCC");
		int MNC = (Integer) completeMeasure.get("MNC");
		completeMeasure.remove("MNC");
		int NTC = (Integer) completeMeasure.get("NTC");
		completeMeasure.remove("NTC");
		
		long lng = (Long) completeMeasure.get("lng");
		completeMeasure.remove("lng");
		long lat = (Long) completeMeasure.get("lat");
		completeMeasure.remove("lat");
		//insertion dans la table GEOTIME
		int ref = this.insertMeasure(lng, lat);
		//insertion dans la table de reference 
		this.insertMeasureReference(MCC, MNC, NTC, ref);
		//il faut maintenant remplir les tables des m�triques:
		Log.d("DEBUG MEASURE", "0");
		for(String table : completeMeasure.keySet()){
			Log.d("DEBUG MEASURE", "1 in for loop with metric"+table);
			this.insertMetric(table, ref, completeMeasure.get(table));
		}
		//TEST
		
		String select = "SELECT * FROM "+askCreatorForTableName("table_ref") ;
		Cursor c = this.db.rawQuery(select,null);
		String str = DatabaseUtils.dumpCursorToString(c);
		Log.d("DEBUG REFERENCE",str);
		c.close();
		
		String select2 = "SELECT * FROM "+askCreatorForTableName("table_meas") ;
		Cursor c2 = this.db.rawQuery(select2,null);
		String str2 = DatabaseUtils.dumpCursorToString(c2);
		Log.d("DEBUG MEASURE",str2);
		c2.close();
		
		String select3 = "SELECT * FROM "+askCreatorForTableName("table_cell") ;
		Cursor c3 = this.db.rawQuery(select3,null);
		String str3 = DatabaseUtils.dumpCursorToString(c3);
		Log.d("DEBUG CELL", "cell"+str3);
		c3.close();
		
	}
	
	
	public String askCreatorForTableName(String tableName){
		String name = "";
		try{
			name = this.dbCreator.getTable(tableName).getName();
		}catch(DataBaseException e){
			e.printStackTrace();
		}
		return name;
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
