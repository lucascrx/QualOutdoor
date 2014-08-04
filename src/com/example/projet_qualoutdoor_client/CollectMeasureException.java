package com.example.projet_qualoutdoor_client;

/*
 * Exception lancée si un problème est rencontré lors de la collecte des données 
 * à inserer dans la base de données
 * */
public class CollectMeasureException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public CollectMeasureException(String message) {
        super(message);
  }

}
