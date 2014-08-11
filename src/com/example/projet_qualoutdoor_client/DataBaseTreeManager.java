package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*Un DataBaseTree Manager va permettre l'insertion ordonn�e des lignes dans la table de reference selon une archiecture
 * arborescente. La nouvelle representation de l'arbre dans la base de donn�es �tant un "applatissage" de l'arbre
 * il va avoir un cuseur pointant sur une ligne de la table qui correspond � un noeud de l'arbre
*/
public class DataBaseTreeManager {
	private SQLiteDatabase db;//base de donn�e sur laquelle le manager envoie les requetes.
	private TableDB table;//table de la base de donn�e sur laquelle il se d�place
	
	private TreeCursor cursor;//curseur qui pointera sur une ligne pr�cise de la table de reference donc sur un noeud de l'arbre
	
	/*Un manager est donc construit avec une bdd, une table sur laquelle il se d�place, on appelle la 
	 * m�thode reset pour qu'il se positionnne sur la racine de l'arbre*/
	public DataBaseTreeManager(SQLiteDatabase db, TableDB table) throws DataBaseException{
		this.db = db;
		this.table = table;
		this.cursor=new TreeCursor();
		this.cursor.init();
		
	}
	
	/*Fonction qui permet de trouver la ligne du dernier �l�ment du sous arbre dont le curseur pointe sur la racine*/
	public int getSubTreeBoundary(){
		Log.d("debug tree","looking for boudary of sub tree rooted by " +this.cursor.getReference()+" on line "+this.cursor.getLine());
		int lastChild;
		String selectQuery = "SELECT line , VALUE , LEVEL  FROM "+table.getName()+" WHERE level <= ? AND line > ?  ORDER BY LINE ASC ";
		Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.cursor.level), Integer.toString(this.cursor.line)});
		if (c.moveToFirst()) {//si un noeud de m�me �tage est trouv�
			lastChild = c.getInt(0)-1;//on recup�re la ligne correspondant au dernier �l�ment du sous arbre
			Log.d("debug tree____","sub tree boundary found on line "+lastChild);
			Log.d("debug tree_____","next brother details are ref: "+c.getInt(1)+" lev: "+c.getInt(2)+" Line:"+c.getInt(0) );
		}else{//sinon tous les autres �l�ments de la liste font partie du sous arbre
			String countQuery = "SELECT Count(*) FROM "+table.getName();//on regarde le nombre d'�l�ments dans la table
			Cursor c2 = db.rawQuery(countQuery,null);
			c2.moveToFirst();
			lastChild = c2.getInt(0)+1;//la ligne du dernier �l�ment de la liste correspond au nombre total de ligne grace � notre convention	 
			c2.close();
			Log.d("debug tree____","sub tree boundary not found : considering last node of the whole tree, line is "+lastChild);
		}  
		c.close();
		return lastChild;
	}
	
	/*Fonction qui permet de savoir si un noeud de valeur ref est contenue dans le sous arbre dont le curseur est la racine
	 * si le noeud est trouv�  la valeur true est renvoy�e et le curseur sera mis � jour sur ce dernier
	 * et sinon le curseur n'est pas modifi� et la valeur false est retourn�e.
	 * */
	public boolean contains(int ref){
		boolean result;
		//on rep�re les limites du sous arbre donc le cuseur pointe sur la racine afin de borner l'intervalle de recherche:
		int boundary = getSubTreeBoundary();
		Log.d("debug tree____","limite of sub tree rooted by "+this.cursor.getReference()+ "is "+boundary);
		//on recherche la r�f�rence indiqu� en se limitant au boundary
		String selectQuery = "SELECT  LINE , VALUE , LEVEL  FROM "+table.getName()+" WHERE VALUE = ? AND line <= ? AND line > ?";
		Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(ref), Integer.toString(boundary),Integer.toString(this.cursor.line)});
		if (c.moveToFirst()) {//si un noeud de m�me �tage est trouv� on update le curseur et on renvoie true
			this.cursor.update(c.getInt(0), c.getInt(1), c.getInt(2));
			result = true;
			Log.d("____debug tree___","node found "+ref+" , details are line: "+c.getInt(0)+" ref: "+c.getInt(1)+" level:"+c.getInt(2));
		}else{
			Log.d("____debug tree___","node not found "+ref);
			result = false; 
		}
		return result;	
	}
	
	/*Fonction qui permet d'inserer un nouveau noeud dans l'arbre dans la ligne suivante
	 * de celle qui est point�e � l'instant courant avec la valeur ref*/
	public void insert(int ref){
		/*il faut d�caler toutes les lignes dont l'index est strictement sup�rieur � la ligne courante*/
		String updateQuery1 = "UPDATE "+table.getName()+" SET line = line + 1 WHERE LINE > "+this.cursor.line+";";
		db.execSQL(updateQuery1);
		//on insert la nouvelle ligne
		String insertQuery = "INSERT INTO "+table.getName()+" (LINE,VALUE,LEVEL) VALUES ("+(this.cursor.line+1)+","+ref+","+(this.cursor.level+1)+");";
		db.execSQL(insertQuery);
		//on met a jour le curseur sur le nouveau noeud
		this.cursor.update(this.cursor.line+1, ref, this.cursor.level+1);	
	}
	
	/*Fonction qui permet d'inserer une feuille dans l'arbre : exactement pareille que la fonction pr�c�dente
	 * sauf que le curseur pointera vers le noeud pere de la feuille
	 * */
	public void insertLeaf(int ref){
		/*il faut d�caler toutes les lignes dont l'index est strictement sup�rieur � la ligne courante*/
		String updateQuery1 = "UPDATE "+table.getName()+" SET line = line + 1 WHERE LINE > "+this.cursor.line+";";
		db.execSQL(updateQuery1);
		//on insert la nouvelle ligne
		String insertQuery = "INSERT INTO "+table.getName()+" (LINE,VALUE,LEVEL) VALUES ("+(this.cursor.line+1)+","+ref+","+(this.cursor.level+1)+");";
		db.execSQL(insertQuery);
	}
	
	/*fonction qui permet devoir si un noeud existe et s'il n'existe pas, elle le cr��e*/
	public void findOrCreate(int ref){
		if(!this.contains(ref)){//Si l'intervalle courant ne contient pas de noeud contenant la r�f�rence indiqu�e
			Log.d("____debug tree___","node created "+ref);
			this.insert(ref);//alors on le cr�e
		}//Dans tous les cas le DataBaseTreeManager aura ses bornes mises � jour
	}
	
	/*Fonction qui permet de retouver un noeud p�re � partir d'un noeud fils,
	 * l'intervalle point� doit donc correspondre � celui du fils;
	 * 
	 * le DataBaseTreeManager sera Mis a jour avec les bornes de l'intervalle du p�re
	 * 
	 * si l'element n'a pas de p�re alors le DataBaseTreeManager sera inchang�
	 */
	public void getFather() throws DataBaseException{
		if(this.cursor.level!=0){//Dans ce cas l'intervalle n'est pas la racine donc admet un p�re
			//le p�re est la ligne la plus proche au dessus du curseur dont le level vaut le level courant +1
			 String selectQuery = "SELECT max( LINE ) , VALUE , LEVEL FROM "+this.table.getName()+" WHERE LINE < ? AND LEVEL = ?";
		     Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.cursor.line), Integer.toString(this.cursor.level - 1) });
		     if (c.moveToFirst()) {
		    	 //on a retrouv� le p�re on met � jour les champs du curseur
	             this.cursor.update(c.getInt(0), c.getInt(1), c.getInt(2));
	             Log.d("debug tree","manager now pointing on " + c.getInt(1) +" on level "+ c.getInt(2));
	        }else{
	        	throw new DataBaseException("TREE MANAGER GET FATHER : can't find node father");
	        }
		    c.close();
		}//si on pointe d�j� sur la racine, on ne fait rien.	
		else{
			Log.d("debug tree","manager now pointing on root");
		}
	}
	
	
	
	/*Fonction qui permet de r�initialiser le manager en repla�ant son curseur sur la racine*/
	public void reset(){
		this.cursor.init();
	}
	
	public TreeCursor getCursor(){
		return this.cursor;
	}
	
	/*Fonction qui permet d'analyser la ligne suivante de la table de ref : utile pour l'�crivain*/
	public boolean moveToNextLine(){
		boolean exists;
		String selectQuery = "SELECT  LINE, VALUE , LEVEL FROM "+this.table.getName()+" WHERE LINE = ?";
	     Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.cursor.line+1)});
	     if (c.moveToFirst()) {//si la ligne suivante existe
	    	 this.cursor.update(c.getInt(0), c.getInt(1),c.getInt(2));
	    	 exists = true;
	     }else{
	    	 exists = false;
	     }
	     return exists;
	}
	
	/*Classe interne qui permet simplement de caracteriser l'objet curseur*/
	public class TreeCursor {
		private int line;//numero de la ligne point�e par le curseur;
		private int reference;//le champs reference de la ligne point�e par le curseur;
		private int level; //le champs level de la ligne point�e par le curseur;
		
		public int getLevel(){
			return this.level;
		}
		
		public int getReference(){
			return this.reference;
		}
		
		public int getLine(){
			return this.line;
		}
		
		public void init(){//on positionne le curseur en root;
			this.line=2;
			this.reference=0;
			this.level=0;
		}
		
		public void update(int li, int ref, int lev){
			this.line=li;
			this.reference=ref;
			this.level=lev;
		}
	}
	
}
