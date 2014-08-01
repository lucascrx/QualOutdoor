package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

/*Classe qui r�pr�sente la liste des noeds non feuille de l'arbre.
 * Elle permet de comparer le contexte courant de rangement avec celui
 * d'une nouvelle mesure afin de minimiser les d�placements du manager
 * pour l'insertion de cette derni�re dans l'arbre.
 * 
 * */
public class MeasureContext {
	
	private int lenghtTree; //longueur de l'arbre sur lequel le contexte est bas�, ici 6: 0_GROUP 1_USER 2_MCC, 3_MNC, 4_NTC, 5_Metric (on ne compte pas root ni la feuille (car on ne va pas comparer la valeur des feuilles))
	private ArrayList<Integer> stages; //liste represente les diff�rents �tages ici : on a valeurMCC,valeursMNC,valeurNTC,valeurMetric
	private int cursor;//curseur qui pointe sur un �tage de la liste.

	
	public MeasureContext(int lenght){
		this.lenghtTree = lenght;
		this.stages = new ArrayList<Integer>();
		for(int i=0;i<this.lenghtTree;i++){
			this.stages.add(0);//on initialise toutes les valeurs du contexte � 0
		}
		this.cursor=0;//on pointe au sommet de l'arbre
	}
	
	
	public int getlength(){
		return this.lenghtTree;
	}
	
	public int getCursor(){
		return this.cursor;
	}
	
	public int getStage(int index){
		return this.stages.get(index);
	}
	
	public void resetCursor(){
		this.cursor=0;
	}
	//permet de fixer les valeurs de la liste des noeuds du contexte
	public void set(int index, int value){
		this.stages.set(index, value);
	}
	
	//permet de savoir si un contexte a une liste valide de Noeuds.
	public boolean isCorrectlySet(){
		int i=0;
		for(int stage : this.stages){
			if(stage==0 && i!=this.lenghtTree-1){//on ne v�rifie pas le dernier noeud car il est fix� au dernier moment
				return false;
			}
			i++;
		}
		return true;
	}
	
/*	//MISE A JOUR DES PARAMETRES DU CONTEXTE
	public void updateMCC(int newMCC){
		this.stages.set(0, newMCC);
	}
	
	public void updateMNC(int newMNC){
		this.stages.set(1, newMNC);
	}
	
	public void updateNTC(int newNTC){
		this.stages.set(2, newNTC);
	}*/
	
	
	public void moveToChild(){//on d�place le curseur du contexte vers l'�l�ment fils
		if(this.cursor<this.stages.size()-1){//on est au bout, on ne bouge pas
			this.cursor++;
		}
	}
	
	public boolean isAtEnd(){//pour savoir si le curseur est au bout de la liste
		return (this.cursor == this.stages.size()-1);
	}
	/*
	public HashMap<String,Number> generateNewContext(long lat, long lng){
		HashMap<String,Number> result = new HashMap<String,Number>();
		result.put("MCC", this.MCC);
		result.put("MNC", this.MNC);
		result.put("NTC", this.NTC);
		result.put("lat", lat);
		result.put("lng", lng);
		
		return result;
	}
	
*/
	public void reset(){
		Log.d("DEBUG CONTEXTE","RESET PROCEEDING");
		for(int i=0;i<this.lenghtTree;i++){
			this.stages.set(i,0);//on reinitialise toutes les valeurs du contexte � 0
		}
		this.cursor=0;//on pointe au sommet de l'arbre
	}
	
}
