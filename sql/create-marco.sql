create table SNZH_ELABORAZIONIEBAY
(
   ID_ELABORAZIONE  bigint not null auto_increment,
   JOB_TYPE         varchar(30) not null,
   JOB_ID           varchar(30) not null,
   FILE_REFERENCE_ID varchar(30) not null,
   FASE_JOB         varchar(30) not null,
   FILENAME        varchar(80) not null,
   JOB_STATUS       varchar(15) not null,
   JOB_PERC_COMPL    int not null,
   PATH_FILE_INPUT   text not null,
   PATH_FILE_ESITO   text,
   DATA_INSERIMENTO datetime not null,
   DATA_ELABORAZIONE datetime,
   NUM_TENTATIVI int not null default 0,
   ERRORE_JOB       text,
   primary key (ID_ELABORAZIONE)
);

alter table SNZH_ELABORAZIONIEBAY comment 'Contiene L''anagrafica dei job di elaborazione dei flussi eb';

create index SNZH_INDICE1 on SNZH_ELABORAZIONIEBAY
(
   JOB_TYPE,
   JOB_ID,
   FILE_REFERENCE_ID
);

