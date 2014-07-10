package com.example.projet_qualoutdoor_client;

import java.io.InputStream;

/*Classe abstraite dont doivent h�riter toutes les solutions d'�changes de fichier
 * elles devront donc implementer la m�thode envoyerFichier r�alisant l'envoi d'information 
 * et toutes les proc�dures auxilliaires selon le protocole correspondant
 */
public interface Sender {
	
	public  String envoyerFichier(String URL,String fileName,InputStream content );
	

}
