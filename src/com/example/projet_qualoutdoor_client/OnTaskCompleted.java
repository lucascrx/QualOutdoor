package com.example.projet_qualoutdoor_client;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

/*Classe qui permet un call back des taches asynchrones vers l'activit� principale
 * main activity implementera la methode onTaskCompleted, les DataSendingManager
 * auront donc comme param�tre en plus un objet de type OnTaskListener et appelleront
 * la methode onTaskCompleted dans leur m�thode OnPostExecute */

public interface OnTaskCompleted {
	/*le parametre int va permettre de distinguer la provenance du callback
	 * le parametre de type HashMap va indiquer les fichier que le tache � envoy�
	 * 
	 * "JE SUIS LA TACHE XX ET J'AI FINI D'ENVOYER LES FICHIERS SUIVANTS XXXX"
	 * */
	
	void onTaskCompleted(String protocole,HashMap<String,FileToUpload> filesSended);

	
	/*Callback pour le g�n�rateur de fichier*/
	void onFileReady(ByteArrayOutputStream file);
}
