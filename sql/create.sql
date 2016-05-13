CREATE TABLE `elaborazioni_ebay` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_name` varchar(45) NOT NULL,
  `stato` varchar(45) NOT NULL,
  `id_job_ebay` varchar(40) DEFAULT NULL,
  `id_file` varchar(50) DEFAULT NULL,
  `avanzamento` decimal(10,0) DEFAULT '0',
  `data_ora_invio` datetime DEFAULT NULL,
  `descrizione_errore` varchar(45) DEFAULT NULL,
  `data_ora_errore` datetime DEFAULT NULL,
  `tentativi_invio` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `file_name_UNIQUE` (`file_name`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1;
