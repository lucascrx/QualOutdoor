package com.example.projet_qualoutdoor_client;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*Un DataBaseTree Manager va permettre l'insertion ordonn�e des lignes dans la table de reference selon une archiecture
 * arborescente. Ainsi il possede deux attribut qui representent les bornes de l'intervalle courant surlequel il pointe
 * 
 * Theoriquement le Manager pointe sur des noeuds de l'�tage NTC
*/
public class DataBaseTreeManager {
	private SQLiteDatabase db;//base de donn�e sur laquelle agir.
	private TableDB table;
	
	private int currentMinSide;
	private int currentMaxSide;
	

	public DataBaseTreeManager(SQLiteDatabase db, TableDB table) throws DataBaseException{
		this.db = db;
		this.table = table;
		this.reset();
		
	}
	
	/*Fonction qui permet de savoir si un noeud de valeur ref est contenu dans l'intervalle courant, 
	 * si c'est le cas le DataBaseTreeManager est mis a jour sur les bornes de l'intervalle trouv� et
	 * la valeur true est renvoy�e et sinon le DataBaseTreeManager n'est pas modifi� et la valeur false est retourn�e.
	 * 
	 * Fonctionne en partie grace � l'unicit� des valeurs caracterisant les noeuds (pour 2 �tages distincts!)
	 * 
	 * Si CurrentMinSide et CurrentMaxSide r�presentent en tout instant les bornes d'un intervalle correspondant � un noeud,
	 * alors il y aura au plus une ligne correspondant aux crit�res du select:
	 * CAR:
	 * -Si les 2 lignes repr�sentent le m�me �tage de l'arbre: on aurait 2 fils ou plus avec la meme valeur IMPOSSIBLE
	 * -Une reference ne sera jamais commune entre 2 noeuds de 2 �tages diff�rents
	 * */
	public boolean contains(int ref){
        String selectQuery = "SELECT * FROM "+table.getName()+" WHERE ls > ? AND rs < ? AND VALUE= ?";
        Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.currentMinSide), Integer.toString(this.currentMinSide), Integer.toString(ref)});
        if (c.moveToFirst()) {
             this.currentMinSide = c.getInt(c.getColumnIndex("ls"));
             this.currentMaxSide = c.getInt(c.getColumnIndex("rs"));
             return true;
        }else{
        	return false;	
        }  
		
	}
	
	/*Fonction qui permet d'inserer un nouveau noeud dans l'intervalle courant avec la valeur ref
	 * le DataBaseTreeManager sera mis a jour avec les bornes du nouvel intervalle
	 * 
	 * le noeud sera inser� du cot� des fortes valeurs.
	 * 
	 * pourque cette fonction agisse corresctement elle devra etre utilis�e sur des intervalles correspondant � des noeuds
	 * 
	 * */
	public void insert(int ref){
		/*il faut d�caler toutes les bornes d'abscisse sup�rieur � la Borne max de l'intervalle d'insertion
		 * pour faire de la place pour l'intervalle � ins�rer*/
		String updateQuery1 = "UPDATE "+table.getName()+" SET ls = ls + 2 WHERE ls >= "+this.currentMaxSide+";";
		String updateQuery2 = "UPDATE "+table.getName()+" SET rs = rs + 2 WHERE rs >= "+this.currentMaxSide+";";
		db.execSQL(updateQuery1);
		db.execSQL(updateQuery2);
		//on insert le nouvel intervalle
		String insertQuery = "INSERT INTO "+table.getName()+" (ls,rs,VALUE) VALUES ("+this.currentMaxSide+","+(this.currentMaxSide+1)+","+ref+");";
		db.execSQL(insertQuery);
		//on retourne l'intervalle du nouveau noeud.
		this.currentMinSide = this.currentMaxSide;
		this.currentMaxSide = this.currentMaxSide+1;	
	}
	
	/*Fonction qui permet d'inserer une feuille dans l'arbre : exactement pareille que la fonction pr�c�dente
	 * sauf que le manager pointera toujours les bornes du noeud o� a eu lieu l'insertion 
	 * */
	public void insertLeaf(int ref){
		/*il faut d�caler toutes les bornes d'abscisse sup�rieur � la Borne max de l'intervalle d'insertion
		 * pour faire de la place pour l'intervalle � ins�rer*/
		String updateQuery1 = "UPDATE "+table.getName()+" SET ls = ls + 2 WHERE ls >= "+this.currentMaxSide+";";
		String updateQuery2 = "UPDATE "+table.getName()+" SET rs = rs + 2 WHERE rs >= "+this.currentMaxSide+";";
		db.execSQL(updateQuery1);
		db.execSQL(updateQuery2);
		//on insert le nouvel intervalle
		String insertQuery = "INSERT INTO "+table.getName()+" (ls,rs,VALUE) VALUES ("+this.currentMaxSide+","+(this.currentMaxSide+1)+","+ref+");";
		db.execSQL(insertQuery);
		//il y a juste � faire une mise a jour de la borne max car cette derniere a �t� d�cal�e pour permettre l'insertion
		this.currentMaxSide = this.currentMaxSide +2;
		//l'autre borne n'a pas chang�e
	}
	
	/*fonction qui permet devoir si un noeud existe et s'il n'existe pas, elle le cr��e*/
	public void findOrCreate(int ref){
		if(!this.contains(ref)){//Si l'intervalle courant ne contient pas de noeud contenant la r�f�rence indiqu�e
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
		if(this.currentMinSide!=1){//Dans ce cas l'intervalle n'est pas la racine donc admet un p�re
			//le p�re est l'intervalle qui comprend strictement l'intervalle courant avec la plus grande valeur min
			 String selectQuery = "SELECT max(ls),rs FROM "+this.table.getName()+" WHERE ls < ? AND rs > ?";
		     Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.currentMinSide), Integer.toString(this.currentMaxSide) });
		     if (c.moveToFirst()) {
		    	 //on a retrouv� le p�re on met � jour les bornes du DataBaseTreeManager
	             int max = c.getInt(1);
	             int min = c.getInt(0); 
	             this.currentMaxSide= max;
	             this.currentMinSide = min;
	        }else{
	        	throw new DataBaseException("TREE MANAGER GET FATHER : can't find node father");
	        }
		}//si on pointe d�j� sur la racine, on ne fait rien.	
	}
	
	
	
	/*fonction qui remet � zero le manager: ses bornes sont repositionn�es sur la racine*/
	public void reset() throws DataBaseException{
		   String selectQuery = "SELECT * FROM "+table.getName()+" WHERE ls = 1";//on repert root car sa borneMin vaut toujours 1
		   Log.d("DEBUG TREE  :","1");
	       Cursor c = db.rawQuery(selectQuery, null);
	       Log.d("DEBUG TREE","2");
	       if (c.moveToFirst()) {
	    	   Log.d("DEBUG TREE","3");
	             int max = c.getInt(c.getColumnIndex("rs"));
	             this.currentMaxSide= max;
	             this.currentMinSide = 1;
	        }else{
	        	throw new DataBaseException("RESET TREE MANAGER : can't locate root node");
	        }
	       
	}
	
	
	
	
	
	
}
