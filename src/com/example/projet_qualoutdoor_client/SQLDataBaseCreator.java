package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/*Classe ayant pour but de créer la base de donnée et de la 
 * réinitialiser en cas de mise à jour, elle definit aussi 
 * le nom de la table et le nom de ses colonnes */
public class SQLDataBaseCreator extends SQLiteOpenHelper{
	
	public static final String COLUMN_0 = "VAL0";//premiere colone de la bdd
	public static final String COLUMN_1 = "VAL1";//deuxieme    "      " 
	public static final String COLUMN_2 = "VAL2";//troisième   "      "
	public static final String COLUMN_3 = "VAL3";//quatrieme   "      "
	
	private static final String TABLE_NAME = "table_values";//nom de la table que l'on souhaite créer
	private static final String DATABASE_NAME = "recorder.db";//nom de la base de donnée dans laquelle on souhaite travailler
	private static final int DATABASE_VERSION = 1;//version de la base de donnée
	
	
	//Constructeur du createur
	public SQLDataBaseCreator(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	/*Fonction à implementer obligatoirement
	 *elle décrit les requetes à executer sur la bdd lorsque  la version est augmentée
	 *Ici on indique de détruire notre table et de la recréer */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//requete correspondant à la destruction de la table 
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME+" ;");
		onCreate(db);
	}
	
	/*Fonction à implementer obligatoirement
	 *elle décrit les requetes à executer sur la bdd à la création du créateur
	 *Comme on souhaite à chaque fois travailler sur une table neuve, on en recréera
	 *une nouvelle à chaque nouveau créateur */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//requete correspondant la création de la table	
		String construction = 
				"CREATE TABLE "+TABLE_NAME+" ( "+COLUMN_0+" INT NOT NULL , "+COLUMN_1+" INT NOT NULL, "+COLUMN_2+" INT NOT NULL, "+COLUMN_3+" INT NOT NULL);";
		  db.execSQL(construction);
	}
	
	//methode qui renvoie les colonnes de la tables sous forme d'une arrayList
	public ArrayList<String> getColumns(){
		ArrayList<String> columns = new ArrayList<String>();
		columns.add(this.COLUMN_0);
		columns.add(this.COLUMN_1);
		columns.add(this.COLUMN_2);
		columns.add(this.COLUMN_3);
	
		return columns;
	}
	
	//methode qui renvoie le nom de la table
	public String getTableName(){
		return this.TABLE_NAME;
	}
	
	
}
