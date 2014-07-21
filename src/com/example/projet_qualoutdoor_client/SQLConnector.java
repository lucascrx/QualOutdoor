package com.example.projet_qualoutdoor_client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*Classe qui a pour but de faire l'intermediaire entre le code java de l'application
 * android et la base de donnée SQLlite locale. Le code enverra des instructions à l'instance
 * de cette classe qui les convertira en des requêtes SQL*/

public class SQLConnector {
	
	private SQLiteDatabase db;//base de donnée sur laquelle agir.
	private SQLDataBaseCreator dbCreator;//createur de la base de donnée
	
	//le constructeur engendre un nouveau createur de bdd
	public SQLConnector(Context context){
		this.dbCreator = new SQLDataBaseCreator(context);
	}
	
	//open() permet de produire la base de donnée à partir du constructeur
	public void open() throws SQLException{
		this.db = this.dbCreator.getWritableDatabase();//on crée ou on ouvre la base de données qui sera en acces lecture ecriture
		//on recréer la table à chaque nouvelle utilisation
		//requete correspondant à la suppression des lignes de la tables
		db.delete(dbCreator.getTableName(),null,null);
	}
	
	//fermeture de la base de données
	public void close(){
		this.dbCreator.close();
	}
	
	//insertion d'une ligne dans la bdd à partir d'une arrayList de valeur à inserer
	public long insertLine(ArrayList<Integer> fields){
		Log.d("longueur du vecteur champs","taille : "+fields.size());
		Log.d("longueur du vecteur colonne","taille : "+this.dbCreator.getColumns().size());
		ContentValues values = new ContentValues();//création de la hasmap value
		int i=0;
		for (String column : this.dbCreator.getColumns()){
			//on remplit la hashmap avec les colonnes de la table à remplir et les valeurs passées en paramètre
			values.put(column, fields.get(i));
			i++;
		}
		//une fois le content value initialisé on l'insert dans la table
		long res = db.insert(dbCreator.getTableName(),null,values);
		return res;
	}
	
	//methode qui permet de transformer le contenu de la table en un inputStream sous le format csv
	public InputStream getInputStream(){
		//on initialise la requete à executer sur la bdd : ici on selectionne tout
		//le résultat est stocké dans un objet de type Cursor
	    Cursor cursor = this.db.rawQuery("SELECT * FROM "+this.dbCreator.getTableName(),null);
	  Log.d("inputStream creation","CURSOR OK");
	  //on initialise une String que l'on va remplir par itération
	    String str = "";
	    while(cursor.moveToNext()){
	    //à chaque ligne renvoyé par la requete on remplit la String avec les valeurs qu'elle contient	
	    	str = str +cursor.getInt(0)+","+cursor.getInt(1)+","+cursor.getInt(2)+","+cursor.getInt(3)+";";
	    }
		Log.d("inputStream creation", "STRING OK");
		Log.d("STRING TO SEND",str);
		//une fois la lecture du resultat finie on ferme le curseur
		cursor.close();
		//on transforme la string construite en inputstream
		InputStream is = new ByteArrayInputStream(str.getBytes());
		return is;
	}
	

}
