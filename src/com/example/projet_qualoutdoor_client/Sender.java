package com.example.projet_qualoutdoor_client;

import java.io.InputStream;

/*Classe abstraite dont doivent hériter toutes les solutions d'échanges de fichier
 * elles devront donc implementer la méthode envoyerFichier réalisant l'envoi d'information 
 * et toutes les procédures auxilliaires selon le protocole correspondant
 */
public interface Sender {
	
	public  String envoyerFichier(String URL,String fileName,InputStream content );
	

}
