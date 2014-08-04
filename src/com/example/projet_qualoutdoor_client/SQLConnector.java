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
 * android et la base de donnée SQLlite locale. Le code enverra des instructions à l'instance
 * de cette classe qui les convertira en des requêtes SQL*/

public class SQLConnector {
	
	private SQLiteDatabase db;//base de donnée sur laquelle agir.
	private SQLDataBaseCreator dbCreator;//createur de la base de donnée
	
	private DataBaseTreeManager manager;//manager de l'arbre de la bdd:MANAGER D'INSERTION
	
	
	//le connector garde en memoire les derniers Group,USer,MCC,MNC,NTC,Metric accèdés afin d'indiquer au
	//manager comment se déplacer dans l'arbre pour une nouvelle insertion.
	//ce sixtuplet represe la position courante du manager après insertion, il est stocké dans un contexte
	
	private MeasureContext oldContext;
	
	
	
	//le constructeur engendre un nouveau createur de bdd
	public SQLConnector(Context context) throws DataBaseException{
		this.dbCreator = new SQLDataBaseCreator(context);
		this.oldContext = new MeasureContext(6);
	}
	
	//open() permet de produire la base de donnée à partir du constructeur
	public void open() throws SQLException, DataBaseException{
		//on crée ou on ouvre la base de données qui sera en acces lecture ecriture
		this.db = this.dbCreator.getWritableDatabase();
		//on change finalement on ne remet pas à zero la bdd
		//elle sera flushée à chaque nouvel envoi.
		//on initialise le manager
		this.manager = new DataBaseTreeManager(this.db,this.dbCreator.getTableReference());
	}
	
	//fermeture de la base de données
	public void close(){
		this.dbCreator.close();
	}
	
	
	/*fonction qui permet d'inserer une ligne correspondant à une mesure dans la table de reference*/
	public void insertReference(MeasureContext newContext, int ref) throws CollectMeasureException, DataBaseException{
		//verification de la cohérence des curseurs
		if(this.oldContext.getlength()!=newContext.getlength() || this.oldContext.getCursor()!=newContext.getCursor()){
			throw new CollectMeasureException("new context doesn't match with old one");
		}
		//si les noeuds sont identiques
		if(newContext.getStage(newContext.getCursor())==this.oldContext.getStage(this.oldContext.getCursor())){
			Log.d("****************DEBUG REFERENCE", "Noeuds identiques : "+newContext.getStage(newContext.getCursor()));
			//si on est à la fin on insere la feuille
			if(this.oldContext.isAtEnd()){
				Log.d("****************DEBUG REFERENCE", "on  insere la feuille "+ref);
				this.manager.insertLeaf(ref);
			}else{//sinon on compare les noeuds fils
				Log.d("****************DEBUG REFERENCE", "on  passe au noeud FILS");
				this.oldContext.moveToChild();//on prend le fils
				newContext.moveToChild();//on prend le fils
				insertReference(newContext,ref);//on rappelle la fonction sur les fils
			}
		}else{//si les noeuds ne sont pas identiques
			//on determine la distance du noeud père du noeud de "conflit" à la fin de l'abre pour faire remonter le
			//manager jusqu'à lui
			Log.d("****************DEBUG REFERENCE", "Noeuds différents : new : "+newContext.getStage(newContext.getCursor())+" old :"+this.oldContext.getStage(this.oldContext.getCursor()));
			int distFatherLeaf = this.oldContext.getlength()-this.oldContext.getCursor();
			//on fait remonter le manager
			for(int i=0;i<distFatherLeaf;i++){
				Log.d("****************DEBUG REFERENCE","on remonte le manager");
				this.manager.getFather();
			}
			//on le fait maintenant redescendre en suivant le bon chemin et en mettant à jour oldContext
			for(int u=0;u<distFatherLeaf;u++){
				Log.d("****************DEBUG REFERENCE","on descend le manager vers "+newContext.getStage(newContext.getCursor()) );
				//on se dirige vers le bon fils
				manager.findOrCreate(newContext.getStage(newContext.getCursor()));
				//mise a jour du vieux cuseur
				this.oldContext.set(this.oldContext.getCursor(),newContext.getStage(newContext.getCursor()));
				//on descend d'un étage
				this.oldContext.moveToChild();
				newContext.moveToChild();
			}
			Log.d("****************DEBUG REFERENCE","FIN DE LA DESCENTE : "+newContext.getStage(newContext.getCursor())  );
			//à la fin on est arrivé au bout de l'arbre : on peut inserer la feuille:
			this.manager.insertLeaf(ref);
		}
		
	}
	
	/*public void insertReference(int MCC, int MNC, int NTC,int metric, int ref ){
		try{
			Log.d("DEBUG **** TREE","MCC_OLD : "+this.lastMCC+" MCC_NEW : "+MCC);
			Log.d("DEBUG **** TREE","MNC_OLD : "+this.lastMNC+" MNC_NEW : "+MNC);
			Log.d("DEBUG **** TREE","NTC_OLD : "+this.lastNTC+" NTC_NEW : "+NTC);
			Log.d("DEBUG **** TREE","METRIC_OLD : "+this.lastMetric+" Metric_NEW : "+metric);
			
			if(this.lastMCC==MCC){//si la nouvelle mesure est dans le MCC actuel
				if(this.lastMNC==MNC){//si la nouvelle mesure est dans le MNC actuel
					if(this.lastNTC==NTC){//si la nouvelle mesure est dans le NTC actuel
						if(this.lastMetric==metric){//si la nouvelle mesure est dans la metrique actuelle
							this.manager.insertLeaf(ref);
						}
						else{//sinon on remonte au NTC et on se place sur ou on crée l'intervalle correspondant à la metric
							this.manager.getFather();//on remonte au NTC
							this.manager.findOrCreate(metric);//on redescend dans la bonne metrique
							//le manager pointe à présent sur la bonne métrique: on crée donc la feuille
							this.manager.insertLeaf(ref);
							//mise a jour de la vielle metrique
							this.lastMetric = metric;
						}
					}else{//sinon on remonte au MNC et on se place sur ou on crée l'intervalle correspondant au NTC
						this.manager.getFather();//on remonte au NTC
						this.manager.getFather();//on remonte au MNC
						this.manager.findOrCreate(NTC);//on redescend dans le nouveau NTC
						this.manager.findOrCreate(metric);//on redescend dans la nvlle metrique
						//le manager pointe a présent sur l'intervalle de la nvlle metrique : on crée donc la feuille
						this.manager.insertLeaf(ref);
						//mise a jour du vieux NTC et de la vielle metrique
						this.lastNTC=NTC;
						this.lastMetric = metric;
					}	
				}else{//dans de cas on remonte jusqu'au MCC pour chercher ou créer le MNC indiqué
					this.manager.getFather();//on remonte au NTC
					this.manager.getFather();//on remonte au MNC
					this.manager.getFather();//on remonte au MCC
					this.manager.findOrCreate(MNC);//on redescend dans le nvx MNC
					this.manager.findOrCreate(NTC);//on redescend dans le nvx NTC
					this.manager.findOrCreate(metric);//on redescend dans la nvlle metrique
					//le manager pointe a présent sur l'intervalle de la nvlle metrique: on crée donc la feuille
					this.manager.insertLeaf(ref);
					//Mise à jour des vieux MNC et NTC et metrique
					this.lastMNC=MNC;
					this.lastNTC=NTC;
					this.lastMetric = metric;
				}	
			}else{//dans ce cas il faut remonter à la racine pour retrouver ou créer le MCC indiqué
				this.manager.getFather();//on remonte au NTC
				this.manager.getFather();//on remonte au MNC
				this.manager.getFather();//on remonte au MCC
				this.manager.getFather();//on remonte à la racine
				this.manager.findOrCreate(MCC);//on redescend dans le nvx MCC
				this.manager.findOrCreate(MNC);//on redescend dans le nvx MNC
				this.manager.findOrCreate(NTC);//on redescend dans le nvx NTC		
				this.manager.findOrCreate(metric);//on redescend dans la nvlle metrique
				//le manager pointe a présent sur l'intervalle de la nvlle metrique: on crée donc la feuille
				this.manager.insertLeaf(ref);
				//mise a jour des vieux MCC, MNC et NTC et metrique
				this.lastMCC=MCC;
				this.lastMNC=MNC;
				this.lastNTC=NTC;
				this.lastMetric = metric;
			}
		}catch(DataBaseException e){
			e.printStackTrace();
		}
	}*/
	
	
	
	
	/*Methode qui insere une mesure dans la table des mesures, elle renvoie l'identifiant de la
	 * ligne insérée ou -1 en cas de probleme*/
	public int insertData(long lat, long lng,String data) throws DataBaseException{
		int id = -1;
		//on prépare la requete d'insertion, la date est générée par SQL
			String insertQuery = "INSERT INTO "+this.dbCreator.getTableMeasure().getName()+" (DATE , LAT , LNG, DATA) VALUES (CURRENT_TIMESTAMP ,"+lat+","+lng+", '"+data+"' );";
			db.execSQL(insertQuery);//Execution de la requete d'insertion
			Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);//on récupère l'ID du dernier élément inséré
			if(c.moveToFirst()){//si on retrouve bien la ligne insérée on retrouve son ID
				id =  c.getInt(0);
			}
			else{
				throw new DataBaseException("SQL CONNECTOR : can't find ID of inserted measure ! ");			
			}

		return id;
	}
	
	
	
	/*
	 *Fonction qui permet l'insertion d'une mesure complete dans le réseau de tables de Recorder
	 *la donnée complete sera présenté sous forme de hasmap de type FielD/Value
	 *sauf pour les parametres destinés aux tables de métrique ou le nom de la table sera indiqué
	 * 
	 * 
	 */
	public void insertMeasure(MeasureContext newContext, HashMap<Integer,String> dataList, long lat, long lng) throws DataBaseException, CollectMeasureException{
	
			//INSERTIONS SUCESSIVES DANS LA TABLE DE MESURES
			//CHAQUE INSERTION DE DATA EST SUIVIE PAR UNE INSERTION ASSOCIEE DANS LA TABLE DE REFERENCE
			int ref;
			for(int dataType : dataList.keySet()){//pour chaque metrique on termine d'initilaliser le nvx Contexte
				newContext.set(newContext.getlength()-1, dataType);//on met la métrique à la derniere case du contexte
				this.oldContext.resetCursor();
				newContext.resetCursor();
				//le contexte est pret
				ref = this.insertData(lat,lng,dataList.get(dataType));//on insert la data dans la table de mesure
				this.insertReference(newContext,ref);//on recupere l'id donnée pour referencer la data dans la table de reference
			}
			
			//TEST
			
			String select = "SELECT * FROM "+this.dbCreator.getTableReference().getName() +" ORDER BY LINE ASC" ;
			Cursor c = this.db.rawQuery(select,null);
			String str = DatabaseUtils.dumpCursorToString(c);
			Log.d("DEBUG REFERENCE",str);
			c.close();
			
			String select2 = "SELECT * FROM "+this.dbCreator.getTableMeasure().getName();
			Cursor c2 = this.db.rawQuery(select2,null);
			String str2 = DatabaseUtils.dumpCursorToString(c2);
			Log.d("DEBUG MEASURE",str2);
			c2.close();

		
		
	}

	
	/*
	//methode qui permet de transformer le contenu de la table en un inputStream sous le format csv
	public InputStream getInputStream(){
		try{
			//on initialise la requete à executer sur la bdd : ici on selectionne tout
			//le résultat est stocké dans un objet de type Cursor
		    Cursor cursor = this.db.rawQuery("SELECT * FROM "+this.dbCreator.getTable("table_ref").getName(),null);
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
		}catch(DataBaseException e){
			e.printStackTrace();
		}
	}*/
	
	/*Fonction  qui permet de retourner les détails d'une feuille précise
	 * */
	public ArrayList<String> getLeafDetails(int ref) throws DataBaseException{
		ArrayList<String> list = new ArrayList<String>();
		String selectQuery = "SELECT DATE , LAT , LNG , DATA FROM "+this.dbCreator.getTableMeasure().getName()+" WHERE ID = ?";
		Cursor c = db.rawQuery(selectQuery, new String[] {Integer.toString(ref)});
		if (c.moveToFirst()) {
			for(int i=0; i<c.getColumnCount();i++){
				list.add(c.getString(i));
			}
			return list;
		}else{
			throw new DataBaseException("can't find leaf ");
		}
	}
	
	/*Fonction qui retrourne un manager calé sur l'arbre : utile pour le file generator qui en a besoin*/
	public DataBaseTreeManager prepareManager() throws DataBaseException{
		DataBaseTreeManager manager = null;
		manager = new DataBaseTreeManager(this.db,this.dbCreator.getTableReference());
		return manager;
	}
	
	/*Fonction qui permet de verifier si des feuilles sont présentes*/
	public boolean hasLeaf(){
		boolean bool;
		//on regarde s'il y a des lignes dans la table de mesure
		String selectQuery = "SELECT ID FROM "+this.dbCreator.getTableMeasure().getName();
		Cursor c = db.rawQuery(selectQuery, null);
		if (c.moveToFirst()) {//s'il y en a on renvoie TRUE
			bool =  true;
		}else{//sinon on renvoie FALSE
			bool =  false;
		}
		c.close();
		return bool;
	}
	
	/*
	public void deleteLeaf(int ref) throws DataBaseException{

			int nb = db.delete(this.dbCreator.getTableMeasure().getName(),"ID = ?",new String[] {Integer.toString(ref) });
			if(nb!=1){
				throw new DataBaseException("unicity not preserved!");
			}

	}*/
	
	/*Reset complet du systeme de stockage apreès envoi
	 * 
	 * flush des tables de stockage à utiliser apres envoi du fichier
	 * La table mesure est vidée completement
	 * la table de reference est vidée completement sauf de la ligne root (ls=1)
	 * 
	 * OldContexte remis à zero
	 * 
	 * Manager replacé sur la racine*/
	public void completeReset() throws DataBaseException{
			db.execSQL("DROP TABLE IF EXISTS '" + this.dbCreator.getTableReference().getName() + "' ");
			db.execSQL("DROP TABLE IF EXISTS '" + this.dbCreator.getTableMeasure().getName() + "'");
			this.dbCreator.onCreate(db);
			
			this.oldContext.reset();
			
		
			this.manager.reset();

	}

}
