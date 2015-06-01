# -*- coding: utf-8 -*-
"""
Created on Tue May 12 19:02:45 2015

@author: davide
"""

import sys
import smtplib

#il primo parametro da arg line è mail destinatario
mailDestinatario = sys.argv[1];
#il secondo è l'oggetto
oggettoMail = sys.argv[2]
#il terzo è il corpo della mail
corpoMail = sys.argv[3]


#info account da cui far partire mail
mailMittente = 'postino4321@gmail.com'
passwordMittente = "p0st1n0p0st1n0"
#componiamo il messaggio da inviare
#il messaggio è fatto da vari header, che devono essere separati dai caratteri di newline
#e ogni header indica esplicitamente mittente, destinatario, oggetto e messaggio

msgToSend = "\n".join([
    "From :"+mailMittente,
    "To: "+mailDestinatario,
    "Subject: "+oggettoMail,
    "",
    corpoMail
])

server = smtplib.SMTP('smtp.gmail.com:587')
server.ehlo()
server.starttls()
server.login(mailMittente,passwordMittente)
server.sendmail(mailMittente,mailDestinatario,corpoMail)
server.quit



