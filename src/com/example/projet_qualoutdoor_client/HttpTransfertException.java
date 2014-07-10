package com.example.projet_qualoutdoor_client;

/*Exception qui sera levée par les méthodes "basiques" de la classe HttpFileSender
 * lorsqu'elle traiteront une exception
 * et qui sera traitée dans la fonction EnvoyerFichier*/

public class HttpTransfertException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HttpTransfertException(String message) {
        super(message);
  }

}
