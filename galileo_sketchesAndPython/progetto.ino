#include <SD.h>
#define PATH_FILE_NOMI_MAILS  "nomiPersoneToIndirizziMail.txt"



void setup() {
    Serial.begin(9600);
    Serial.print("Inizializzo la SD card");
    if(!SD.begin())
    {
      Serial.println("Impossibile inizializzare");
      return;
    }
    Serial.println("Inizializzazione SD effettuata");
    
    
    delay(2000);
    
    //spostare da qui--------------------------------------------------------------------------------
    String nomePersonaDestinatario="DavideSito";
    String messaggio="Ciao_Davide"; //i messaggi non vanno separati o li prende come diversi input
    
    String mailDestinatario = ottieniEmailDaNome(nomePersonaDestinatario);
    inviaMailTo(mailDestinatario,messaggio,"consegna_postino");
    //-----------------------------------------------------------------------------------------------
    
}


void loop() {
  
  

}


void inviaMailTo(String mailTo,String msg, String oggetto)
{
  //il primo argomento che passiamo allo script è la mail destinatario, il secondo è il corpo mail ed il terzo l'oggetto
  String argToSystemCall = "python /media/realroot/sendEMail.py " + mailTo + " " + msg + " " + oggetto+"\n";
  Serial.println(argToSystemCall);
  char temp[300];
  
  argToSystemCall.toCharArray(temp,argToSystemCall.length());
  Serial.println(temp);
  system( temp );
  
}


String ottieniEmailDaNome(String nome)
{
    
    File mioFile = SD.open(PATH_FILE_NOMI_MAILS);
    if(mioFile)
    {
      String s = "";
      while(mioFile.available())
      {
        char t =(char) mioFile.read();
        if(t != '\n'){
          s+=t;
          continue;
        }
        //Serial.println(s);
        int indiceSeparatore = s.indexOf(":");
        if(s.substring(0,indiceSeparatore).equalsIgnoreCase(nome))
        {
          return s.substring(indiceSeparatore+1);
        }
        
        s = "";
        
      }
    }
    else
    {
      Serial.println("Problema apertura file per cercare mail dato nome");  
    }
}
