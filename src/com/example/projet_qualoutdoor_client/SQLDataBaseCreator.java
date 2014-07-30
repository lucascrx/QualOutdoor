package com.example.projet_qualoutdoor_client;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/*Classe ayant pour but de cr�er la base de donn�e et de la 
 * r�initialiser en cas de mise � jour, elle definit aussi 
 * le nom de la table et le nom de ses colonnes 
 * 
 * Le Systeme de base de donn�es de Recorder est constitu� de 7 tables:
 * une table de r�f�rence et 6 tables annexes
 * 
 */
public class SQLDataBaseCreator extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "recorder.db";//nom de la base de donn�e
	private static final int DATABASE_VERSION = 1;//version de la base de donn�e
	//les diff�rentes tables de la base de donn�es
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
	
	/*Fonction � implementer obligatoirement
	 *elle d�crit les requetes � executer sur la bdd lorsque  la version est augment�e
	 *Ici on indique de d�truire nos tables et de les recr�er */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//requete correspondant � la destruction de la table 
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_ref.getName() + "' ");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_meas.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_cell.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_ss.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_call.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_upload.getName() + "'");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_download.getName() + "'");
		//on recr�e les nouvelles tables
		onCreate(db);
	}
	
	/*Fonction � implementer obligatoirement
	 *elle d�crit les requetes � executer sur la bdd � la cr�ation du cr�ateur
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {	
	
			//CONSTRUCTION DES TABLES DE LA BASE DE DONNEE:
				//CREATION DES OBJETS TableDB
			
				//EXECUTION DES REQUETES
			db.execSQL(table_ref.createTableintoDB());//construction de la table de r�f�rence
			db.execSQL(table_meas.createTableintoDB());//construction de la table de mesure
			db.execSQL(table_cell.createTableintoDB());//construction de la table de cellule
			db.execSQL(table_ss.createTableintoDB());//construction de la table signal strengh
			db.execSQL(table_call.createTableintoDB());//construction de la table call
			db.execSQL(table_upload.createTableintoDB());//construction de la table upload
			db.execSQL(table_download.createTableintoDB());//construction de la table download
				//On INSERE LE ROOT DANS LA TABLE DE REFERENCE
			db.execSQL("INSERT INTO "+this.table_ref.getName()+" (ls,rs,value) VALUES (1,2,0); ");
			
			Log.d("DEBUG SQL CREATOR", "tables cr��s");
			
		
	}
	
	
	
	public TableDB getTable(String str) throws DataBaseException{
		try {
			TableDB table = null;
			//on regarde si dans la classe il existe un attribut du nom demand�
			Field result = this.getClass().getDeclaredField(str);
			//on r�cup�re l'acces � l'attribut
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
