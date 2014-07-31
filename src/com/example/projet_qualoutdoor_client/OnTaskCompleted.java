package com.example.projet_qualoutdoor_client;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

/*Classe qui permet un call back des taches asynchrones vers l'activité principale
 * main activity implementera la methode onTaskCompleted, les DataSendingManager
 * auront donc comme paramètre en plus un objet de type OnTaskListener et appelleront
 * la methode onTaskCompleted dans leur méthode OnPostExecute */

public interface OnTaskCompleted {
	/*le parametre int va permettre de distinguer la provenance du callback
	 * le parametre de type HashMap va indiquer les fichier que le tache à envoyé
	 * 
	 * "JE SUIS LA TACHE XX ET J'AI FINI D'ENVOYER LES FICHIERS SUIVANTS XXXX"
	 * */
	
	void onTaskCompleted(String protocole,HashMap<String,FileToUpload> filesSended);

	
	/*Callback pour le générateur de fichier*/
	void onFileReady(ByteArrayOutputStream file);
}
