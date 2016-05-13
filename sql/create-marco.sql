create table SNZH_ELABORAZIONIEBAY
(
   SNZH_IDELABORAZIONE  bigint not null auto_increment,
   SNZH_JOBTYPE         varchar(30) not null,
   SNZH_JOBID           varchar(30) not null,
   SNZH_FILEREFERENCEID varchar(30) not null,
   SNZH_FASEJOB         varchar(30) not null,
   SNZH_FILENAME        varchar(80) not null,
   SNZH_JOBSTATUS       varchar(15) not null,
   SNZH_JOBPERCCOMPL    int not null,
   SNZH_PATHFILEINPUT   text not null,
   SNZH_PATHFILEESITO   text,
   SNZH_DATAINSERIMENTO datetime not null,
   SNZH_DATAELABORAZIONE datetime,
   SNZH_STATOJOB        varchar(30) not null,
   SNZH_ERROREJOB       text,
   primary key (SNZH_IDELABORAZIONE)
);

alter table SNZH_ELABORAZIONIEBAY comment 'Contiene L''anagrafica dei job di elaborazione dei flussi eb';

create index SNZH_INDICE1 on SNZH_ELABORAZIONIEBAY
(
   SNZH_JOBTYPE,
   SNZH_JOBID,
   SNZH_FILEREFERENCEID
);

