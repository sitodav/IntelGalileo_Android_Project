package com.example.testbluetooth;

//questa è l'applicazione di start, e permette di scaricare informazioni sul db relativamente alle consegne da fare
//Poi si passerà all'activity principale, che prenderà tali informazioni dallo stesso db

import utilities.Costanti;
import utilities.ThreadPerAggiornamentoDbLocale;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class StartingActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starting);
		
		Button downloadBut = (Button)findViewById(R.id.btnDownload);
		downloadBut.setOnClickListener(new OnClickListener(){

			//...quando clicco sul bottone download devo far partire un thread che richiede dal web server centrale la list adegli indirizzi ai quali questo postino
			//deve fare consegna, e per ogni indirizzo la lista destinatari
			@Override
			public void onClick(View v) {
				//devo creare un oggetto per inviare richiesta get al web server, dal quale ottenere la lista 
				//degli indirizzi ai quali questo postino (Costanti.ID_POSTINO) deve consegnare
				//e insieme agli indirizzi la lista delle persone
				//questo lo salvo su un db locale allo smartphone (dal quale poi leggera l'app principale)
				
				//Per fare questo utilizzo la classe thread per network, che prende in input al costruttore
				//url del webserver, e messaggio (che nel nostro caso contiene l'id del postino) da inviare al server
				//tiriamo fuori quindi per prima cosa ip e porta del web server
				String ipServer = ((EditText) findViewById(R.id.editText1) ).getText().toString();
				String portaServer = ((EditText) findViewById(R.id.editText2) ).getText().toString();
				
				String msgToServer = "id_postino="+Costanti.ID_POSTINO;//sarebbero i parametri get, che vengono messi in append all'url web server
				
				String urlServer = "http://"+ipServer+":"+portaServer+"/consegne_giornata";
				
				//avviamo il thread (che quando avrà ricevuto risultato, lo salverà sul db locale)
				ThreadPerAggiornamentoDbLocale t1 = new ThreadPerAggiornamentoDbLocale(StartingActivity.this,urlServer,msgToServer); //messaggio vuoto
				t1.start();
				
			}
			
		});
		
		
		Button btnDone = (Button)findViewById(R.id.buttonDone);
		btnDone.setOnClickListener(new OnClickListener(){

			//quando clicco su questo bottone, devo avviare l'activity principale
			//SI PRESUPPONE CHE SI SIA GIA' PORTATA A TERMINE LA RICHIESTA DI AGGIORNAMENTO DEL DB LOCALE CON I DATI INVIATI DAL WEB SERVER CENTRALE
			@Override
			public void onClick(View v) {
				Intent newAct = new Intent(StartingActivity.this,MainActivity.class);
				startActivity(newAct);
				
			}
			
		});
		
	}
}
