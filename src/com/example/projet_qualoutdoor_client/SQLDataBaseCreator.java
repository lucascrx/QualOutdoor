package com.example.projet_qualoutdoor_client;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/*Classe ayant pour but de créer la base de donnée et de la 
 * réinitialiser en cas de mise à jour, elle definit aussi 
 * le nom de la table et le nom de ses colonnes 
 * 
 * Le Systeme de base de données de Recorder est constitué de 7 tables:
 * une table de référence et 6 tables annexes
 * 
 */
public class SQLDataBaseCreator extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "recorder.db";//nom de la base de donnée
	private static final int DATABASE_VERSION = 1;//version de la base de donnée
	//les différentes tables de la base de données
	private TableDB table_ref;
	private TableDB table_meas;
	private TableDB table_cell;
	private TableDB table_ss;
	private TableDB table_call;
	private TableDB table_upload;
	private TableDB table_download;
	
	
	
	//Constructeur du createur
	public SQLDataBaseCreator(Context context) throws DataBaseException {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		this.table_ref = new TableDB("recorder_tt",new String[] {"ls","rs","VALUE"}, new String[] {"INTEGER","INTEGER NOT NULL","INTEGER NOT NULL"});
		this.table_meas = new TableDB("measure_it",new String[] {"ID","DATE","LAT","LNG"}, new String[] {"INTEGER PRIMARY KEY AUTOINCREMENT","DATETIME","REAL","REAL"});
		this.table_cell = new TableDB("cellule_identity_it",new String[] {"ID_MEASURE","VALUE"}, new String[] {"INTEGER","INTEGER"});
		this.table_ss = new TableDB("signal_strengh_it",new String[] {"ID_MEASURE","VALUE"}, new String[] {"INTEGER","REAL"});
		this.table_call = new TableDB("call_it",new String[] {"ID_MEASURE","STATE"}, new String[] {"INTEGER","INTEGER"});
		this.table_upload = new TableDB("upload_it",new String[] {"ID_MEASURE","YIELD"}, new String[] {"INTEGER","REAL"});
		this.table_download = new TableDB("download_it",new String[] {"ID_MEASURE","YIELD"}, new String[] {"INTEGER","REAL"});
	}
	
	/*Fonction à implementer obligatoirement
	 *elle décrit les requetes à executer sur la bdd lorsque  la version est augmentée
	 *Ici on indique de détruire nos tables et de les recréer */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//requete correspondant à la destruction de la table 
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_ref.getName() + "' ");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_meas.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_cell.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_ss.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_call.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_upload.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_download.getName() + "'");
		//on recrée les nouvelles tables
		onCreate(db);
	}
	
	/*Fonction à implementer obligatoirement
	 *elle décrit les requetes à executer sur la bdd à la création du créateur
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {	
	
			//CONSTRUCTION DES TABLES DE LA BASE DE DONNEE:
				//CREATION DES OBJETS TableDB
			
				//EXECUTION DES REQUETES
			db.execSQL(table_ref.createTableintoDB());//construction de la table de référence
			db.execSQL(table_meas.createTableintoDB());//construction de la table de mesure
			db.execSQL(table_cell.createTableintoDB());//construction de la table de cellule
			db.execSQL(table_ss.createTableintoDB());//construction de la table signal strengh
			db.execSQL(table_call.createTableintoDB());//construction de la table call
			db.execSQL(table_upload.createTableintoDB());//construction de la table upload
			db.execSQL(table_download.createTableintoDB());//construction de la table download
				//On INSERE LE ROOT DANS LA TABLE DE REFERENCE
			db.execSQL("INSERT INTO "+this.table_ref.getName()+" (ls,rs,value) VALUES (1,2,0); ");
			
			Log.d("DEBUG SQL CREATOR", "tables créés");
			
		
	}
	
	
	
	public TableDB getTable(String str) throws DataBaseException{
		try {
			TableDB table = null;
			//on regarde si dans la classe il existe un attribut du nom demandé
			Field result = this.getClass().getDeclaredField(str);
			//on récupère l'acces à l'attribut
			table = (TableDB) result.get(this);
			return table;
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			throw new DataBaseException("Get Table Exception : "+ e.toString());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			throw new DataBaseException("Get Table Exception : "+ e.toString());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			throw new DataBaseException("Get Table Exception : "+ e.toString());
		}		
	}
	
	
	
	
}
