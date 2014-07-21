package com.example.projet_qualoutdoor_client;

import java.util.ArrayList;
/*Classe qui illustre le type d'objet manipulé dans la base données*/

public class SQLLine {
	
	private int val0;
	private int val1;
	private int val2;
	private int val3;
	
	public int getVal0() {
		return val0;
	}
	public void setVal0(int val0) {
		this.val0 = val0;
	}
	public int getVal1() {
		return val1;
	}
	public void setVal1(int val1) {
		this.val1 = val1;
	}
	public int getVal2() {
		return val2;
	}
	public void setVal2(int val2) {
		this.val2 = val2;
	}
	public int getVal3() {
		return val3;
	}
	public void setVal3(int val3) {
		this.val3 = val3;
	}
	
	public ArrayList<Integer> getVal(){
		ArrayList<Integer> al = new ArrayList<Integer>();
		al.add(getVal0());
		al.add(getVal1());
		al.add(getVal2());
		al.add(getVal3());
		
		return al;
	}
	
	

}
