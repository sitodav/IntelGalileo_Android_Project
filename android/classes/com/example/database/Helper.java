package com.example.database;

import utilities.Costanti;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Helper extends SQLiteOpenHelper {

	private static final int NUM_VERSIONE = 1;
	
	
	public Helper(Context context) 
	{
		super(context, Costanti.NOME_DB, null, NUM_VERSIONE);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
			//inizializzazione di un db fittizio utilizzato per simulare la nostra applicazione
		
			//DDL:---------------------------------
		    //QUESTO VA FATTO INDIPENDENTEMENTE DAI DATI CHE ARRIVANO DAL WEB SERVER DELLA POSTA
			//CREO TABELLA POSTINI
//			db.execSQL("CREATE TABLE POSTINI("+
//			"ID_POSTINO TEXT PRIMARY KEY," +
//			"NOME TEXT," +
//			"COGNOME TEXT" +
//			")");
			//tabella che associa i posti in cui consegnare, ai postini che devono fare la consegna
			db.execSQL("CREATE TABLE INDIRIZZI_CONSEGNA(" +
					"INDIRIZZO TEXT PRIMARY KEY," +
					"ID_POSTINO TEXT REFERENCES POSTINI(ID_POSTINO))");
			
			//tabella che associa, per un dato punto di consegna, i nomi dei destinatari a cui consegnare, e i tipi di posta da consegnargli
			db.execSQL("CREATE TABLE DESTINATARI(" +
					"NOME_COGNOME TEXT PRIMARY KEY," +
					"TIPO_CONSEGNA TEXT," +
					"INDIRIZZO TEXT)");
//			//DML:------------------------------------
			//QUESTO VIENE FATTO QUANDO ARRIVANO I DATI DAL SERVER DELLA POSTA , NON PIU CABLATO
//			//inserisco il postino giovanni esposito, con id A01
//			ContentValues values = new ContentValues();
//			values.put("ID_POSTINO", "A01"); //questo id postino deve essere cablato nell'applicazione smartphone del postino
//			values.put("NOME","GIOVANNI");
//			values.put("COGNOME", "ESPOSITO");
//			db.insert("POSTINI", null, values);
//			//inserisco 2 indirizzi associati a questo postino
//			values = new ContentValues();
//			values.put("INDIRIZZO","II TRAV. STRETTOLA, 5");
//			values.put("ID_POSTINO","A01");
//			db.insert("INDIRIZZI_CONSEGNA", null, values);
//			values = new ContentValues();
//			values.put("INDIRIZZO","VIA DELLE MOSCHE, 10");
//			values.put("ID_POSTINO","A01");
//			db.insert("INDIRIZZI_CONSEGNA", null, values);
//			//inserisco destinatari per il primo degli indirizzi
//			values = new ContentValues();
//			values.put("NOME_COGNOME", "DavideSito");
//			values.put("TIPO_CONSEGNA","RACCOMANDATA");
//			values.put("INDIRIZZO","II TRAV. STRETTOLA, 5");
//			db.insert("DESTINATARI",null,values);
//			values= new ContentValues();
//			values.put("NOME_COGNOME", "AntonioLuongo");
//			values.put("TIPO_CONSEGNA","BOLLETTA");
//			values.put("INDIRIZZO","II TRAV. STRETTOLA, 5");
//			db.insert("DESTINATARI", null, values);
//			
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
