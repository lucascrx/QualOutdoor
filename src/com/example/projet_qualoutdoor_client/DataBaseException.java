package com.example.projet_qualoutdoor_client;

/*Exeption lancée si un probleme dans la manipulation de la base de donnée est rencontré*/
public class DataBaseException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public DataBaseException(String message) {
        super(message);
  }

}
