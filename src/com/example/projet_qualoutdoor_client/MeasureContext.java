package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;
import java.util.HashMap;

/*Classe qui réprésente le contexte de mesure 
 * 
 * */
public class MeasureContext {
	
	private HashMap<String,Integer> context ;
	
	public MeasureContext(){
		this.context = new HashMap<String,Integer>();
		this.context.put("MCC", null);
		this.context.put("MNC",null);
		this.context.put("NTC", null);
	}
	
	public int getMCC(){
		return this.context.get("MCC");
	}
	
	public int getMNC(){
		return this.context.get("MNC");
	}
	
	public int getNTC(){
		return this.context.get("NTC");
	}
	
	//MISE A JOUR DES PARAMETRES DU CONTEXTE
	public void updateMCC(int newMCC){
		this.context.put("MCC", newMCC);
	}
	
	public void updateMNC(int newMNC){
		this.context.put("MNC", newMNC);
	}
	
	public void updateNTC(int newNTC){
		this.context.put("NTC", newNTC);
	}
	
	/*
	 * A chaque enregistrement d'une nouvelle valeur, on construit une hasmap contenant tous les paramètres de la 
	 * mesures et on y ajoute une "photographie" du contexte courant
	 */
	public HashMap<String,Number> getMeasure(ArrayList<String> fields, ArrayList<Number> values) throws CollectMeasureException{
		HashMap<String,Number> completeMeasure = new HashMap<String,Number>();
		//on remplit la hashmap avec le contexte:
		for( String param : this.context.keySet()){
			completeMeasure.put(param, this.context.get(param));
		}
		//on remplit la hashmap avec les metriques fournies
		if(fields.size()==values.size()){
			int i = 0;
			for(String field : fields ){
				completeMeasure.put(field, values.get(i));
				i++;
			}
		}else{
			throw new CollectMeasureException("Metric values and fields mismatch !");
		}
		return completeMeasure;
	}
	
}
