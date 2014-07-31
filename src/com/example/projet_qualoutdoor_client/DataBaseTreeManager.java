package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*Un DataBaseTree Manager va permettre l'insertion ordonnée des lignes dans la table de reference selon une archiecture
 * arborescente. Ainsi il possede deux attribut qui representent les bornes de l'intervalle courant surlequel il pointe
 * 
 * Theoriquement le Manager pointe sur des noeuds de l'étage NTC
*/
public class DataBaseTreeManager {
	private SQLiteDatabase db;//base de donnée sur laquelle agir.
	private TableDB table;
	
	private int currentMinSide;
	private int currentMaxSide;
	

	public DataBaseTreeManager(SQLiteDatabase db, TableDB table) throws DataBaseException{
		this.db = db;
		this.table = table;
		this.reset();
		
	}
	
	/*Fonction qui permet de savoir si un noeud de valeur ref est contenu dans l'intervalle courant, 
	 * si c'est le cas le DataBaseTreeManager est mis a jour sur les bornes de l'intervalle trouvé et
	 * la valeur true est renvoyée et sinon le DataBaseTreeManager n'est pas modifié et la valeur false est retournée.
	 * 
	 * Fonctionne en partie grace à l'unicité des valeurs caracterisant les noeuds (pour 2 étages distincts!)
	 * 
	 * Si CurrentMinSide et CurrentMaxSide répresentent en tout instant les bornes d'un intervalle correspondant à un noeud,
	 * alors il y aura au plus une ligne correspondant aux critères du select:
	 * CAR:
	 * -Si les 2 lignes représentent le même étage de l'arbre: on aurait 2 fils ou plus avec la meme valeur IMPOSSIBLE
	 * -Une reference ne sera jamais commune entre 2 noeuds de 2 étages différents
	 * */
	public boolean contains(int ref){
        String selectQuery = "SELECT * FROM "+table.getName()+" WHERE ls > ? AND rs < ? AND VALUE= ?";
        Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.currentMinSide), Integer.toString(this.currentMaxSide), Integer.toString(ref)});
        if (c.moveToFirst()) {
             this.currentMinSide = c.getInt(c.getColumnIndex("ls"));
             this.currentMaxSide = c.getInt(c.getColumnIndex("rs"));
             Log.d("DEBUG TREE******************","CURRENT CONTAINS"+ref);
             return true;
        }else{
        	Log.d("DEBUG TREE******************","CURRENT DOES NOT CONTAINS"+ref);
        	return false;	
        }  
		
	}
	
	/*Fonction qui permet d'inserer un nouveau noeud dans l'intervalle courant avec la valeur ref
	 * le DataBaseTreeManager sera mis a jour avec les bornes du nouvel intervalle
	 * 
	 * le noeud sera inseré du coté des fortes valeurs.
	 * 
	 * pourque cette fonction agisse corresctement elle devra etre utilisée sur des intervalles correspondant à des noeuds
	 * 
	 * */
	public void insert(int ref){
		/*il faut décaler toutes les bornes d'abscisse supérieur à la Borne max de l'intervalle d'insertion
		 * pour faire de la place pour l'intervalle à insérer*/
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
	
	/*Fonction qui permet d'inserer une feuille dans l'arbre : exactement pareille que la fonction précédente
	 * sauf que le manager pointera toujours les bornes du noeud où a eu lieu l'insertion 
	 * */
	public void insertLeaf(int ref){
		/*il faut décaler vers la droite toutes les bornes d'abscisse supérieur à la Borne max de l'intervalle d'insertion
		 * pour faire de la place pour l'intervalle à insérer*/
		String updateQuery1 = "UPDATE "+table.getName()+" SET ls = ls + 2 WHERE ls >= "+this.currentMaxSide+";";
		String updateQuery2 = "UPDATE "+table.getName()+" SET rs = rs + 2 WHERE rs >= "+this.currentMaxSide+";";
		db.execSQL(updateQuery1);
		db.execSQL(updateQuery2);
		//on insert le nouvel intervalle
		String insertQuery = "INSERT INTO "+table.getName()+" (ls,rs,VALUE) VALUES ("+this.currentMaxSide+","+(this.currentMaxSide+1)+","+ref+");";
		db.execSQL(insertQuery);
		//il y a juste à faire une mise a jour de la borne max car cette derniere a été décalée pour permettre l'insertion
		this.currentMaxSide = this.currentMaxSide +2;
		//l'autre borne n'a pas changée
	}
	
	/*
	 * Fonction qui permet de supprimer le noeud courrant : utile pour l'écriture du fichier, le manager sera ensuite replacé
	 * sur la racine, pas génant car les suppressions sont suivies de focusOn*/
	public void deleteCurrentNode() throws DataBaseException{
		int nb = db.delete(table.getName(),"ls = ? AND rs = ?",new String[] {Integer.toString(this.currentMinSide), Integer.toString(this.currentMaxSide) });
		if(nb!=1){
			throw new DataBaseException("unicity not preserved!");
		}else{
			/*la suppression a bien eu lieu
			 * il faut décaler vers la gauche toutes les bornes d'abscisse supérieur à la Borne min de l'intervalle d'insertion
			 * aspirer l'espace créer par la supression de l'intervalle*/
			String updateQuery1 = "UPDATE "+table.getName()+" SET ls = ls - 2 WHERE ls >= "+this.currentMinSide+";";
			String updateQuery2 = "UPDATE "+table.getName()+" SET rs = rs - 2 WHERE rs >= "+this.currentMinSide+";";
			db.execSQL(updateQuery1);
			db.execSQL(updateQuery2);
			//on place le curseur en root
			this.reset();//ATTENTION IL FAUT ACCOMPAGNER CE RESET D'UN RESET DU OLD CONTEXT ASSOCIE
		}
	}
	
	/*fonction qui permet devoir si un noeud existe et s'il n'existe pas, elle le créée*/
	public void findOrCreate(int ref){
		if(!this.contains(ref)){//Si l'intervalle courant ne contient pas de noeud contenant la référence indiquée
			this.insert(ref);//alors on le crée
			Log.d("DEBUG TREE*****",ref + "NOT CONTAINED THEN CREATION ");
		}//Dans tous les cas le DataBaseTreeManager aura ses bornes mises à jour
	}
	
	/*Fonction qui permet de retouver un noeud père à partir d'un noeud fils,
	 * l'intervalle pointé doit donc correspondre à celui du fils;
	 * 
	 * le DataBaseTreeManager sera Mis a jour avec les bornes de l'intervalle du père
	 * 
	 * si l'element n'a pas de père alors le DataBaseTreeManager sera inchangé
	 */
	public void getFather() throws DataBaseException{
		if(this.currentMinSide!=1){//Dans ce cas l'intervalle n'est pas la racine donc admet un père
			//le père est l'intervalle qui comprend strictement l'intervalle courant avec la plus grande valeur min
			 String selectQuery = "SELECT max(ls),rs FROM "+this.table.getName()+" WHERE ls < ? AND rs > ?";
		     Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.currentMinSide), Integer.toString(this.currentMaxSide) });
		     if (c.moveToFirst()) {
		    	 //on a retrouvé le père on met à jour les bornes du DataBaseTreeManager
	             int max = c.getInt(1);
	             int min = c.getInt(0);
	             Log.d("DEBUG TREE **********","FATHER OF NODE FOUND CURRENT INT IS "+this.currentMinSide+" "+this.currentMaxSide);
	             this.currentMaxSide= max;
	             this.currentMinSide = min;
	             Log.d("DEBUG TREE **********","POINTING OF FATHER NOW INT IS"+this.currentMinSide+" "+this.currentMaxSide);
	        }else{
	        	throw new DataBaseException("TREE MANAGER GET FATHER : can't find node father");
	        }
		}//si on pointe déjà sur la racine, on ne fait rien.	
	}
	
	
	
	/*fonction qui remet à zero le manager: ses bornes sont repositionnées sur la racine*/
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
	
	/*
	 * fonction qui permet de placer le manager sur un intervalle désiré, utile pour le manager écrivain
	 * */
	public void focusOn(int bordMin, int bordMax){
		this.currentMinSide=bordMin;
		this.currentMaxSide=bordMax;
	}
	
	/*
	 * renvoie le bord max de l'intervalle pointé
	 * */
	public int getMaxSide(){
		return this.currentMaxSide;
	}
	
	/*
	 * renvoie le bord Min de l'intervalle pointé
	 * */
	public int getMinSide(){
		return this.currentMinSide;
	}
	
	/*Renvoie la référence de l'intervalle pointé
	 * */
	public int getCurrentReference() throws DataBaseException{
		int ref;
		String selectQuery = "SELECT VALUE FROM "+table.getName()+" WHERE ls = ? AND rs = ?";
		Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(this.currentMinSide), Integer.toString(this.currentMaxSide) });
		if (c.moveToFirst()) {
            ref = c.getInt(0);
		}else{
			throw new DataBaseException("Reference is unreachable ! ");
		}
		return ref;
		
	}
	
	/*
	 * Renvoi la liste des intervalles correspondants à tous les noeuds fils de l'intervalle pointé
	 */
	public ArrayList<int[]> getDirectChildSides() throws DataBaseException{
		ArrayList<int[]> intervals = new ArrayList<int[]>();
		int bordMax;
		int refSide = this.currentMinSide+1;//abscisse de reference qui va pointer sur les bord minimum de tous les fils
		//ici on pointe sur le bord min du premier fils (s'il y en a un)
		while(refSide != this.currentMaxSide){//tant que l'on a pas atteint le bord max du noeud
			//on récupère la borne droite de l'intervalle associé à la borne gauche récupérée
			String selectQuery = "SELECT rs FROM "+table.getName()+" WHERE ls = ?";
			Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(refSide)});
			if (c.moveToFirst()) {
				bordMax = c.getInt(0);
				//on a donc un intervalle correspondant à un fils, on l'ajoute à la liste
				intervals.add(new int[] {refSide,bordMax});
				//on met a jour refSide en le faisant pointer sur le bord min du voisin
				refSide = bordMax+1; 
			}else{
				throw new DataBaseException("can't locate children ! ");
			}	
		}
		return intervals;
	}
	
	
	
}
