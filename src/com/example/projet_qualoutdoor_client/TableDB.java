package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class TableDB {
	

	private String name;
	private HashMap<String,String> columns;
	

	public TableDB(String n, HashMap<String,String> col) {
		this.name = n;
		this.columns = col;
		//on récupère les valeurs des types des colonnes passé dans la hashmap:
		ArrayList<String> SQLtypes = new ArrayList<String>();
		for(String SQLtype : col.values()){
			SQLtypes.add(SQLtype);
		}
	
	}
	
	public TableDB(String n, String[] value, String[] type ) throws DataBaseException {
		if(value.length==type.length){
			this.name = n;
			this.columns = new HashMap<String,String>();
			for(int i=0; i<value.length;i++){
				this.columns.put(value[i], type[i]);
			}
		}else{
			throw new DataBaseException("TableDB creation : number of columns and number of types must be equal");
		}
		

	}
	
	public String getName() {
		return this.name;
	}
	
	public ArrayList<String> getColumsName() {
		ArrayList<String> columnsName = new ArrayList<String>();
		for(int i=0; i< this.columns.size();i++){
			columnsName.add(this.columns.keySet().toArray()[i].toString());
		}
		return columnsName;
	}
	
	
	public String createTableintoDB(){
		String columnsFields= "";
		int i=0;
		//construction des associations colonne/type de la requete de creation
		for(String col : this.getColumsName()){
			if(i!=0){
				columnsFields = columnsFields + ",";
			}
			columnsFields = columnsFields + " "+col+" "+this.columns.get(col)+" ";
			i++;
		}
		String request = "CREATE TABLE "+this.name+" ( "+columnsFields+" );";
		Log.d("DATA BASE DEBUG", request);
		return request;	
	}
	
	
	
	
}
