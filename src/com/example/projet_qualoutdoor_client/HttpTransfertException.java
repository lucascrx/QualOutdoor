package com.example.projet_qualoutdoor_client;

/*Exception qui sera lev�e par les m�thodes "basiques" de la classe HttpFileSender
 * lorsqu'elle traiteront une exception
 * et qui sera trait�e dans la fonction EnvoyerFichier*/

public class HttpTransfertException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HttpTransfertException(String message) {
        super(message);
  }

}
