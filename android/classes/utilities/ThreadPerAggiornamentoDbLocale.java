package utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.example.database.Helper;
import com.example.testbluetooth.StartingActivity;
import com.example.testbluetooth.StartingActivity.MioHandler;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

//classe usata per mandare richiesta al web server, e ricevere risposta, tutto sottoforma di post request
//i parametri, nelle richieste post, sono mandati come corpo del messaggio (non nell'url come per le richieste get)
//E devono avere quindi la forma "par1=ciao&par2=comeva&....parn=we"

public class ThreadPerAggiornamentoDbLocale extends Thread
{
	String msgToSend;
	String strUrl;
	Activity callingActivity;
	MioHandler handler;
	
	public ThreadPerAggiornamentoDbLocale(Activity callingActivity, String urlServer, String msg, MioHandler handler)
	{
		this.msgToSend = msg;
		this.strUrl = urlServer;
		this.callingActivity = callingActivity;
		this.handler = handler;
	}
	@Override
	public void run() {
		
		//lanciamo http request di tipo post al web service 
		HttpURLConnection con = null;
		SQLiteDatabase istanzaDB = null ;
		
		
		
		try
		{
			
			//MANDO RICHIESTA GET
			//apro connessione richiesta http di tipo post
			strUrl += "?" ;
			strUrl += msgToSend; //i parametri richiesta get vanno in coda all'url
			Log.e("CHIAVE",strUrl);
			URL urlObj = new URL(strUrl);//"http://192.168.1.7:8081/consegne_giornata?id_postino=A01");
			con = (HttpURLConnection) urlObj.openConnection();
			con.setFollowRedirects(false);
			con.setRequestMethod("GET");
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			
			
			
			
//			OTTENGO LA RISPOSTA ALLA MIA RICHIESTA GET
			BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String s;
			while((s = rd.readLine())!= null)
			{
				sb.append(s);
			}
			Log.w("CHIAVE",sb.toString());
			//RICEVUTA RISPOSTA
			//A questo punto in sb abbiamo la rappresentazione json che il web server ci ha inviato dei dati che ci serve
			//scaricare sul db locale
			//Per prima cosa creo istanza dell' SQLiteOpenHelper, che ha cablato al suo interno un nome db, in questo modo
			//se il db era stato già creato, non viene ricreato, altrimenti vengono create le tabelle per come previsto dal suo metodo onCreate
			Helper dbLocale= new Helper(callingActivity);
			istanzaDB = dbLocale.getWritableDatabase(); //ne ottengo istanza
			
			//lo svuoto dai dati precedenti
//			istanzaDB.rawQuery("DELETE FROM INDIRIZZI_CONSEGNA",null);
//			istanzaDB.rawQuery("DELETE FROM DESTINATARI" , null);
			istanzaDB.delete("INDIRIZZI_CONSEGNA",null,null);
			istanzaDB.delete("DESTINATARI",null,null);
			
			//per convenzione riceviamo una cosa di questo tipo dal server
			// {"SECONDA TRAVERSA": [{"nome_cognome": "DavideSito", "tipo_consegna": "RACCOMANDATA"}, {"nome_cognome": "AntonioLuongo", "tipo_consegna": "BOLLETTA"}]}
			//ovvero un dizionario, dove ogni chiave è un indirizzo, al quale è associato una list, di elementi che sono hash
			JSONObject a = (JSONObject) JSONValue.parse(sb.toString());
			for(String indirizzo : (Set<String>)a.keySet() )
			{
				JSONArray destinatari = (JSONArray) a.get(indirizzo);
				
				//POPOLAMENTO DEL DB LOCALE
				//INSERISCO ENTRY NELLA TABELLA DELLE CONSEGNE ASSOCIATE A NOI (POSTINO)
				istanzaDB.beginTransaction();
				String[] argQuery ={ indirizzo , Costanti.ID_POSTINO };
				istanzaDB.execSQL("INSERT INTO INDIRIZZI_CONSEGNA VALUES (?,?)", argQuery);
				istanzaDB.setTransactionSuccessful();
				istanzaDB.endTransaction();
				
				
				for( JSONObject destinatario : (ArrayList<JSONObject>) destinatari ) //ogni elemento di questo array json (lista) è a sua volta un jsonobject (dictionary)
				{
					
					
					//Log.w("CHIAVE",destinatario.get("nome_cognome")+"");
					String nomeCognomeDest = (String) destinatario.get("nome_cognome");
					String tipoConsegna = (String) destinatario.get("tipo_consegna");
					
					//POPOLO TABELLA DEI DESTINATARI ASSOCIATI A GLI INDIRIZZI INSERITI SOPRA
					istanzaDB.beginTransaction();
					String[] argQuery2 = {nomeCognomeDest,tipoConsegna,indirizzo};
					istanzaDB.execSQL("INSERT INTO DESTINATARI VALUES (?,?,?)" , argQuery2);
					istanzaDB.setTransactionSuccessful();
					istanzaDB.endTransaction();
					
				}
			}
			
//			((StartingActivity)callingActivity).canSwitch = true;
			

			//avvisiamo la starting activity (affinchè possa far apparire un toast)
			Message msg1 = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("test", "tsst");
			msg1.setData(b);
			handler.sendMessage(msg1);
			
		}
		catch(Exception ex)
		{

			ex.printStackTrace();
		}
		finally{
			if (istanzaDB != null )
				istanzaDB.close();
		}
	}
}