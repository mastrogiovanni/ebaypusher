
Lancio
------

Lanciare il comando con:

> java -jar ebaypusher.jar

Documentazione Stati
--------------------


* [Pusher]

<crea batch>			IN_CORSO_DI_INVIO
CREATED

* [Puller]

<upload file>
CREATED				INVIATO_EBAY

<start>
SCHEDULED			INVIATO_EBAY

<update_status>
SCHEDULED
IN_PROCESS			INVIATO_EBAY

<terminato..>
COMPLETED			TERMINATO_CON_SUCCESSO
ABORTED				TERMINATO_CON_ERRORE
FAILED				TERMINATO_CON_ERRORE
						...
				SUPERATO_NUMERO_MASSIMO_INVII

