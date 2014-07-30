package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;
import java.util.HashMap;

/*Classe qui réprésente le contexte de mesure 
 * 
 * */
public class MeasureContext {
	
	private int MCC;
	private int MNC;
	private int NTC;
	private int metric;

	
	public MeasureContext(){
		this.MCC=0;
		this.MNC=0;
		this.NTC=0;
	}
	
	public int getMCC(){
		return this.MCC;
	}
	
	public int getMNC(){
		return this.MNC;
	}
	
	public int getNTC(){
		return this.NTC;
	}
	
	//MISE A JOUR DES PARAMETRES DU CONTEXTE
	public void updateMCC(int newMCC){
		this.MCC = newMCC;
	}
	
	public void updateMNC(int newMNC){
		this.MNC = newMNC;
	}
	
	public void updateNTC(int newNTC){
		this.NTC = newNTC;
	}
	
	public HashMap<String,Number> generateNewContext(long lat, long lng){
		HashMap<String,Number> result = new HashMap<String,Number>();
		result.put("MCC", this.MCC);
		result.put("MNC", this.MNC);
		result.put("NTC", this.NTC);
		result.put("lat", lat);
		result.put("lng", lng);
		
		return result;
	}
	

	
}
