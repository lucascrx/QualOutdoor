package com.example.projet_qualoutdoor_client;

import java.io.InputStream;

public class FileToUpload {

	/* afin de clarifier le code on crée un classe décrivant l'objet que représente un fichier à uploader
	 * en particulier un nom sous lequel on veut qu'il soit stocké et un intputstream qui joue un rôle de 
	 * lecteur du contenu 
	 */
	
	private String fileName;//nom du fichier
	private InputStream content;// lecteur du contenu
	
	
	public FileToUpload(String fileName, InputStream content) {		
		this.fileName = fileName;
		this.content = content;
	}
	
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public InputStream getContent() {
		return content;
	}
	public void setContent(InputStream content) {
		this.content = content;
	}
	
	

}
