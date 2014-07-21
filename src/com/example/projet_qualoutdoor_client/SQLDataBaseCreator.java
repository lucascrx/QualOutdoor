package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/*Classe ayant pour but de cr�er la base de donn�e et de la 
 * r�initialiser en cas de mise � jour, elle definit aussi 
 * le nom de la table et le nom de ses colonnes */
public class SQLDataBaseCreator extends SQLiteOpenHelper{
	
	public static final String COLUMN_0 = "VAL0";//premiere colone de la bdd
	public static final String COLUMN_1 = "VAL1";//deuxieme    "      " 
	public static final String COLUMN_2 = "VAL2";//troisi�me   "      "
	public static final String COLUMN_3 = "VAL3";//quatrieme   "      "
	
	private static final String TABLE_NAME = "table_values";//nom de la table que l'on souhaite cr�er
	private static final String DATABASE_NAME = "recorder.db";//nom de la base de donn�e dans laquelle on souhaite travailler
	private static final int DATABASE_VERSION = 1;//version de la base de donn�e
	
	
	//Constructeur du createur
	public SQLDataBaseCreator(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	/*Fonction � implementer obligatoirement
	 *elle d�crit les requetes � executer sur la bdd lorsque  la version est augment�e
	 *Ici on indique de d�truire notre table et de la recr�er */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//requete correspondant � la destruction de la table 
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME+" ;");
		onCreate(db);
	}
	
	/*Fonction � implementer obligatoirement
	 *elle d�crit les requetes � executer sur la bdd � la cr�ation du cr�ateur
	 *Comme on souhaite � chaque fois travailler sur une table neuve, on en recr�era
	 *une nouvelle � chaque nouveau cr�ateur */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//requete correspondant la cr�ation de la table	
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
