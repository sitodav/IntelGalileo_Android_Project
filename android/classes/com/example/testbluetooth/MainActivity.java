package com.example.testbluetooth;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import utilities.Costanti;

import com.example.database.Helper;

import android.R.color;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	
	Helper dbHelper; //db usato per supporto alla simulazione
	
	MenuItem sceltaDispoBtn;
	MenuItem connectMenuBtn;
	MenuItem disconnectMenuBtn;
	MenuItem connectionStateBtn;
	MenuItem sendToGalileoBtn;
	
	BluetoothAdapter BA ;
	ArrayList<BluetoothDevice> alDevicesDisponibili = new ArrayList<BluetoothDevice>();
	BluetoothDevice deviceScelto ;
	BluetoothSocket btSocket ;
	OutputStream outStream;
	InputStream inStream;
	boolean validPeer = false;
	
	ArrayList<String> alIndirizziConsegne;
	ArrayList<HashMap<String,String>> alDestinatariETipi;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //creo helper e ottengo istanza db
        
        dbHelper = new Helper(this);
        
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //devo popolare lo spinner superiore con la lista degli indirizzi per i quali questo postino (cioè
        //quello associato all'id cablato nell'app) deve effettuare consegna
        
        //prendo gli indirizzi per i quali fare le consegne dal db locale (che si suppone sia stato precedentemente popolato dalla
        //starting application che si connette al web server)
        
        alIndirizziConsegne = ottieniIndirizziConsegne();
        //popolo spinner e vi aggiungo handler per il click
        ArrayAdapter ad = new ArrayAdapter(this, R.layout.layout_per_spinner, alIndirizziConsegne);
        ((Spinner)findViewById(R.id.spinner1)).setAdapter(ad);
        ((Spinner)findViewById(R.id.spinner1)).setOnItemSelectedListener(new OnItemSelectedListener(){

        	
			@Override
			//QUANDO SELEZIONO UN ELEMENTO DELLO SPINNER SUPERIORE
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) 
			{
				//DEVO POPOLARE LA PARTE INFERIORE A SECONDA DELL'INDIRIZZO SCELTO	
				String indirizzoScelto = alIndirizziConsegne.get(position);
				//lancio metodo che accede al db, e per quest'indirizzo scelto si salva in un array list le persone destinatarie con i 
				//tipi di consegna
				alDestinatariETipi = ottieniDestinatariETipiComeMessaggi(indirizzoScelto);
				//e con questi ci popolo la tabella inferiore
				popolaTabellaInferiore();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
        	
        });
        
        
        //attivo bluetooth
        attivazioneBluetooth();
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        sceltaDispoBtn = menu.findItem(R.id.btnListDevices);
        
        disconnectMenuBtn = menu.findItem(R.id.btnCloseSocket);
        disconnectMenuBtn.setEnabled(false); // di default il tasto disconnetti è disabilitato
        
        connectMenuBtn = menu.findItem(R.id.btnConnectSocket);
        
        connectionStateBtn = menu.findItem(R.id.btnStateConnection);
        connectionStateBtn.setIcon(R.drawable.disconnected);
        
        
        
        sendToGalileoBtn = menu.findItem(R.id.btnSendToGalileo);
        sendToGalileoBtn.setIcon(R.drawable.send);
        sendToGalileoBtn.setEnabled(false); //si attiva solo quando siamo connessi col socket
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      
        int idButt = item.getItemId();
        switch(idButt)
        {
        	case R.id.btnListDevices:
        		scegliDevice();
        		return true;
        	case R.id.btnCloseSocket:
        		closeSocket();
        		return true;
        	case R.id.btnConnectSocket:
        		openSocket();
        		return true;
        	case R.id.btnSendToGalileo:
        		if(sendDataToGalileo())
        		{
        			Toast.makeText(this, "Dati inviati con successo", Toast.LENGTH_LONG).show();
        		}
        		else
        		{
        			Toast.makeText(this, "Errore!", Toast.LENGTH_LONG).show();
        				
        		}
        		return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    
    public boolean sendDataToGalileo()
    {
    	//preparo i dati da inviare secondo lo standard nomeDestinatario:TipoCOnsegna che si aspetta lo script sull'arduino
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outStream));
    	
    	
    	//poi preparo i dati per come devono essere 
    	//CONSEGNA_POSTINO?nomepersona:tipoconsegna?nomepersona:tipoconsegna?nomepersona:tipoconsegna?
    	try
	    {
    		String msgToSend ="POSTINO_CONSEGNA?";
    		
    		for (HashMap<String,String> el : alDestinatariETipi)
	    	{
	    		String nomeDestinatario = el.get("NOME_DESTINATARIO");
	    		String tipoConsegna = el.get("TIPO_CONSEGNA");
	    		msgToSend += nomeDestinatario + ":" + tipoConsegna + "?" ;
	    		
	    	}
    		msgToSend += '\n'; //va necessariamente terminato altrimenti arduino non lo processa
    		bw.write(msgToSend);
    		bw.flush();
    		
    		return true;
    	}
    	catch(IOException ex)
    	{
    		ex.printStackTrace();
    		return false;
    	}
    	
    	
    }
    
    
    public ArrayList<String> ottieniIndirizziConsegne()
    {
    	//metodo che utilizzando il db di supporto, ottiene tutti gli indirizzi ai quali deve consegnare questo postino
    	ArrayList<String> alRes = new ArrayList<String>();
    	
    	String querySql = "SELECT INDIRIZZI_CONSEGNA.INDIRIZZO " +
    			"FROM INDIRIZZI_CONSEGNA WHERE INDIRIZZI_CONSEGNA.ID_POSTINO = ?";
    	
//    	String querySql = "SELECT NOME_COGNOME, TIPO_CONSEGNA " +
//    			"FROM (POSTINI JOIN INDIRIZZI_CONSEGNA ON POSTINI.ID_POSTINO = INDIRIZZI_CONSEGNA.ID_POSTINO) " +
//    				"JOIN DESTINATARI ON INDIRIZZI_CONSEGNA.INDIRIZZO = DESTINATARI.INDIRIZZO " +
//    			"WHERE POSTINI.ID_POSTINO = ?";
    	
    	String[] queryArgs = {Costanti.ID_POSTINO}; //uso la costante cablata nell'app che indica il postino
    	
    	Cursor risQuery = dbHelper.getReadableDatabase().rawQuery(querySql, queryArgs);
    	while(risQuery.moveToNext())
    	{ //aggiungo il risultato all'array list da ritornare
    		alRes.add(risQuery.getString(0));
    	}
    	
    	
    	return alRes;
    }
    
    public ArrayList<HashMap<String,String>> ottieniDestinatariETipiComeMessaggi(String indirizzoScelto)
    {//metodo che ottiene la lista di messaggi e tipo di consegna, per un indirizzo scelto, incapsulandoli come messaggi da inviare
    	ArrayList<HashMap<String,String>> alRes = new ArrayList<HashMap<String,String>>();
    	
    	String querySql = "SELECT NOME_COGNOME, TIPO_CONSEGNA " +
		"FROM INDIRIZZI_CONSEGNA JOIN DESTINATARI ON INDIRIZZI_CONSEGNA.INDIRIZZO = DESTINATARI.INDIRIZZO " +
		"WHERE INDIRIZZI_CONSEGNA.ID_POSTINO = ? AND INDIRIZZI_CONSEGNA.INDIRIZZO = ?";
    	
    	String[] queryArgs = {Costanti.ID_POSTINO , indirizzoScelto};
    	
    	Cursor res = dbHelper.getReadableDatabase().rawQuery(querySql, queryArgs);
    	
    	while(res.moveToNext())
    	{//la stringa è costruita come DavideSito:Raccomandata
    		HashMap<String,String> hashToInsert = new HashMap<String,String>();
    		hashToInsert.put("NOME_DESTINATARIO", res.getString(0));
    		hashToInsert.put("TIPO_CONSEGNA",res.getString(1));
    		alRes.add(hashToInsert);
    	}
    	
    	return alRes;
    }
    
    public boolean attivazioneBluetooth()
    {
    	//ottengo adapter
    	BA = BluetoothAdapter.getDefaultAdapter();
        if(BA != null && !BA.isEnabled()) //se il bluetooth non è attivato allora lanciamo la richiesta per attivarlo
        {
        	//intent esplicito per attivazione bluetooth
        	Intent intToActivate = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(intToActivate, 0);
        }
        Toast.makeText(this,"Bluetooth attivato",Toast.LENGTH_SHORT).show();
        return true;
    }
    
    public void scegliDevice()
    {
    	//controllo se il bluetooth adapter è correttamente inizializzato (valido e attivo)
    	if(BA == null || !BA.isEnabled())
    	{
    		Toast.makeText(this, "Bluetooth disabilitato ! Impossibile continuare", Toast.LENGTH_SHORT);
    		return;
    	}
    	//ottengo il set dei devices scoperti
    	Set<BluetoothDevice> devicesDisponibili = BA.getBondedDevices();
    	//li salvo sull'al associato
    	alDevicesDisponibili.clear();
    	ArrayList<String> nomiPerModello = new ArrayList<String>();
    	
    	for(BluetoothDevice dev : devicesDisponibili)
    	{
    		alDevicesDisponibili.add(dev);
    		nomiPerModello.add(dev.getName());
    	}
    	//creo una dialog
    	final Dialog dialog = new Dialog(this);
    	dialog.setCancelable(true);
    	dialog.setTitle("Scegli device");
    	//gli setto il layout
    	dialog.setContentView(R.layout.layout_per_dialog);
    	//tiro fuori la list view
    	ListView lv = (ListView) dialog.findViewById(R.id.lv);
    	//creo il modello per la list
    	ArrayAdapter modelloPerLista = new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,nomiPerModello);
    	//setto il modello
    	lv.setAdapter(modelloPerLista);
    	
    	//setto l'handler per click su elemento
    	lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				//uso indice dell'elemento della list view cliccato, per tirare fuori il bluetooth device dall'array list corrispettivo
				deviceScelto = alDevicesDisponibili.get(position);
				Toast.makeText(MainActivity.this, "Hai scelto device con nome "+deviceScelto.getName(), Toast.LENGTH_LONG).show();
				//il dispositivo scelto da il nome al tasto di scelta elementi sul menu
				sceltaDispoBtn.setTitle(deviceScelto.getName());
				dialog.dismiss();
			}
    		
    	});
    	dialog.show();
    }
    
    public void openSocket()
    {
    	try
    	{
    		if(deviceScelto == null)
    		{

    	        Toast.makeText(this,"Scegliere prima un device",Toast.LENGTH_SHORT).show();
    			throw new Exception();
	    	}
//    		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard 
	        ParcelUuid[] uuidsDevice = deviceScelto.getUuids(); //ottengo uid del device bluetooth target
	        //Toast.makeText(this,uuidsDevice[0].getUuid().toString(),Toast.LENGTH_LONG).show();
	        btSocket = deviceScelto.createRfcommSocketToServiceRecord(uuidsDevice[0].getUuid());
	        
	        
	        BA.cancelDiscovery();
	        btSocket.connect();
	        outStream = btSocket.getOutputStream();
	        inStream = btSocket.getInputStream();
	        validPeer = true;
	        Toast.makeText(MainActivity.this,"Socket bluetooth creato",Toast.LENGTH_LONG).show();
	        connectMenuBtn.setEnabled(false);
	        disconnectMenuBtn.setEnabled(true);
	        
	        //cambio l'icona ed il titolo dell'elemento del menu che indica lo stato
	        connectionStateBtn.setIcon(R.drawable.connected);
	        connectionStateBtn.setTitle("connected to "+deviceScelto.getName());
	        
	        //abilito il bottone per inviare alla galileo
	        sendToGalileoBtn.setEnabled(true);
	        
	        
    	}
    	
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    		Toast.makeText(MainActivity.this,"Problema connessione socket",Toast.LENGTH_LONG).show();
    		
    		validPeer = false;
    		connectMenuBtn.setEnabled(true);
    		disconnectMenuBtn.setEnabled(false);
    		connectionStateBtn.setIcon(R.drawable.disconnected);
			connectionStateBtn.setTitle("disconnected");
	        sendToGalileoBtn.setEnabled(false);
    	}
        
        
    }
    
    public void closeSocket()
    {
    	if(btSocket != null)
    	{	
    		try
    		{
    			inStream.close();
    			outStream.close();
    			btSocket.close();
    		}
    		catch(IOException ex)
    		{
    			
    		}
    		finally
    		{
    			validPeer = false;
    			disconnectMenuBtn.setEnabled(false);
    			connectMenuBtn.setEnabled(true);
    			
    			connectionStateBtn.setIcon(R.drawable.disconnected);
    			connectionStateBtn.setTitle("disconnected");

    	        sendToGalileoBtn.setEnabled(false);
    			
    			Toast.makeText(this,"Socket disconnesso",Toast.LENGTH_SHORT);
    		}
    	}
    	
    }
    
    public void popolaTabellaInferiore()
    {
    	TableLayout tbl = (TableLayout) findViewById(R.id.tblInferiore);
    	tbl.removeAllViews();
    	
    	int i = 0;
    	
    	for( HashMap el : alDestinatariETipi)
    	{
    		
    		TableRow rowToInsert = new TableRow(this);
    		int color = (i % 2 == 0) ? getResources().getColor(R.color.giallo) : getResources().getColor(R.color.verde);
    		rowToInsert.setBackgroundColor(color);
    		TextView tvToInsert = new TextView(this);
    		tvToInsert.setTextColor(getResources().getColor(R.color.black));
    		//creo entry per il nome
    		tvToInsert.setText(el.get("NOME_DESTINATARIO")+"");
    		tvToInsert.setPadding(0, 0, 40, 0);
    		rowToInsert.addView(tvToInsert);
    		//creo entry per il tipo di consegna
    		tvToInsert = new TextView(this);
    		tvToInsert.setText(el.get("TIPO_CONSEGNA")+"");
    		tvToInsert.setTextColor(getResources().getColor(R.color.black));
    		rowToInsert.addView(tvToInsert);
    		tbl.addView(rowToInsert);
    		
    		
    		i++;
    	}
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	closeSocket(); //e questo si occupa di aggiornare i bottoni etc
    	
    }
    
    
}
