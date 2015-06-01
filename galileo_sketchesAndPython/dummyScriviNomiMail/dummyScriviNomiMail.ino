#include <SD.h>



void setup() {
  Serial.begin(9600);
  Serial.print("Inizializzo la SD card");
  if(!SD.begin())
  {
    Serial.println("Impossibile inizializzare");
    return;
  }
  Serial.println("Inizializzazione effettuata");
  
  File mioFile = SD.open("nomiPersoneToIndirizziMail.txt",FILE_WRITE);
  
  if(mioFile)
  {
    Serial.println("Scrivo sul file...");
    mioFile.println( "DavidePito:molotv@gmail.com\nAntonioLuongo:molotv@gmail.com" );
    mioFile.close();
    Serial.println("Scrittura Effettuata...");  
  }
  else
  {
    Serial.println("Problema apertura file in scrittura"); 
  }
  

}

void loop() {

  
 

}
