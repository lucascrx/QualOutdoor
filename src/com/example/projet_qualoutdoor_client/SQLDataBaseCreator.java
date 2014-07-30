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
	private TableDB table_reference;
	private TableDB table_measure;
	
	
	
	
	//Constructeur du createur
	public SQLDataBaseCreator(Context context) throws DataBaseException {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		this.table_reference = new TableDB("recorder_tt",new String[] {"ls","rs","VALUE"}, new String[] {"INTEGER","INTEGER NOT NULL","INTEGER NOT NULL"});
		this.table_measure = new TableDB("measure_it",new String[] {"ID","DATE","LAT","LNG","DATA"}, new String[] {"INTEGER PRIMARY KEY AUTOINCREMENT","DATETIME","REAL","REAL","VARCHAR"});
		
	}
	
	/*Fonction à implementer obligatoirement
	 *elle décrit les requetes à executer sur la bdd lorsque  la version est augmentée
	 *Ici on indique de détruire nos tables et de les recréer */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//requete correspondant à la destruction de la table 
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_reference.getName() + "' ");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_measure.getName() + "'");
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
			db.execSQL(table_reference.createTableintoDB());//construction de la table de référence
			db.execSQL(table_measure.createTableintoDB());//construction de la table de mesure

				//On INSERE LE ROOT DANS LA TABLE DE REFERENCE
			db.execSQL("INSERT INTO "+this.table_reference.getName()+" (ls,rs,value) VALUES (1,2,0); ");
			
			Log.d("DEBUG SQL CREATOR", "tables créés");
			
		
	}
	
	
	
	public TableDB getTableReference() {
			return this.table_reference;
	}
	
	public TableDB getTableMeasure(){
		return this.table_measure;
	}
	
	
	
}
