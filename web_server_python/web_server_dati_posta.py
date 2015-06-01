# -*- coding: utf-8 -*-
"""
Created on Wed Apr 22 16:48:39 2015

@author: davide
"""

#questo script lancia web server che si mette in attesa di richieste in post
#contenenti come parametro id postino
#quando la riceve, cerca tutti gli indirizzi per i quali quel postino deve fare consegna
#e tutte le persone per quegli indirizzi, e li manda al richiedente
#IL RICHIEDENTE è l'app android del postino


from bottle import request, run, get , post
import json


#------------fase di ddl e dml database
import sqlite3

conn = sqlite3.connect('db_centrale_poste');
curs = conn.cursor();

try:
    curs.execute("DROP TABLE POSTINI");
except: #perchè da eccezione se non esiste ancora
    pass;
try:
    curs.execute("DROP TABLE INDIRIZZI_CONSEGNA");
except: #perchè da eccezione se non esiste ancora
    pass;
try:
    curs.execute("DROP TABLE DESTINATARI");
except: #perchè da eccezione se non esiste ancora
    pass;

curs.execute('''CREATE TABLE POSTINI(
             ID_POSTINO TEXT PRIMARY KEY,
             NOME TEXT,
             COGNOME TEXT)''');

curs.execute('''CREATE TABLE INDIRIZZI_CONSEGNA 
            (INDIRIZZO TEXT PRIMARY KEY,
             ID_POSTINO TEXT
            )''');
            


curs.execute('''CREATE TABLE DESTINATARI
             (NOME_COGNOME TEXT PRIMARY KEY,
              TIPO_CONSEGNA TEXT,
              INDIRIZZO TEXT
             )''');
conn.commit();   

curs.execute("INSERT INTO POSTINI VALUES('A01','GIOVANNI','ESPOSITO')");
curs.execute("INSERT INTO INDIRIZZI_CONSEGNA VALUES('SECONDA TRAVERSA','A01')");
curs.execute("INSERT INTO INDIRIZZI_CONSEGNA VALUES('PIAZZA NAZIONALE','A01')");
curs.execute("INSERT INTO DESTINATARI VALUES('DavidePito','STIPENDIO','SECONDA TRAV')");
curs.execute("INSERT INTO DESTINATARI VALUES('AntonioLuongo','MULTA','SECONDA TRAV')");
conn.commit();
conn.close();
#---------------------------------------------------------------------------------------


@get('/consegne_giornata')
def funz():
    
    id_postino = request.query.get("id_postino");
    print(id_postino);
    #faccio query per id postino
    conn = sqlite3.connect('db_centrale_poste');
    curs = conn.cursor();
   
    dizToSend = {}  #dizionario dove per ogni indirizzo (chiave) ci sarà una list di dizionari, dove ogni dizionario sarà un destinatario, e le chiavi del dizionario saranno nome_cognome e tipo_consegna
    
    for righe in curs.execute("SELECT * FROM INDIRIZZI_CONSEGNA WHERE INDIRIZZI_CONSEGNA.ID_POSTINO = '%s'" %id_postino):
        tempList = []
        for righe2 in curs.execute("SELECT * FROM DESTINATARI WHERE INDIRIZZO = '%s'" %righe[0]):
            tempDiz = {}            
            tempDiz["nome_cognome"]=righe2[0]; 
            tempDiz["tipo_consegna"]=righe2[1];
            tempList.append(tempDiz);
        dizToSend[righe[0]] = tempList;
   
   #ottengo la rappresentazione json
    stringaJSON = json.dumps(dizToSend);
    print stringaJSON;
    
    #chiudo connessione al db
    conn.close();
    #e al client ritorno la stringa json sopra ottenuta
    return stringaJSON;
   



run(host="0.0.0.0",port=8081) #su tutte interfacce