INTRODUZIONE
------------

Ebaypusher è un batch che si occupa di effettuare l'upload di files su ebay utilizzando le Merchant API.
Il sistema si poggia su un database mysql, utilizza tre tabelle per accodare file da inviare e annotare
avanzamento e risposte.
I files da inviare vengono gestiti su filesystem utilizzano 4 cartelle: output, report, error, input

SVILUPPO
--------

Il progetto è stato sviluppato con Java 1.6, Maven 3 (gestione degli artefatti), 
Git (come sistema di versionamento).

Per poter utilizzare il progetto, aprirlo in un IDE eclipse che possieda il
plugin per Maven (m2e: Maven Integration for Eclipse). Non è richiesto il plugin
di GIT se si desidera solo compilare il progetto o metterlo su Subversion.

Il progetto va importato in maven utilizzando 
File / Import / Existing Maven project

Questo comando risolve tutte le dipendenze e scarica tutte le librerie nella
loro versione corretta.

TOOLS
-----

Per compilare il progetto lanciare:

> mvn clean install

Il progetto compilato e zippato si trova nella cartella 
target/ebaypusher-0.0.1-SNAPSHOT-bin.zip

LANCIO
------

Per utilizzare il tool è sufficiente scompattare il file .zip
- Customizzare il file di configurazione nella cartella /conf:
	- Puntamento al database
	- Directory di utilizzo
	- Parametri di configurazione
- Eseguire
	> java -jar ebaypusher.jar
