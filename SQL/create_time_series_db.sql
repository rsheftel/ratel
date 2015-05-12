 --create database TSDB
-- login sim Sim5878 
use TSDB
go
alter table dbo.time_series drop constraint pk_time_series 
go
alter table dbo.time_series drop constraint un_time_series 
go
drop table dbo.time_series
go
CREATE TABLE dbo.time_series
(
    time_series_id             int identity not null,   
    time_series_name           varchar(200) NOT NULL  
);
alter table dbo.time_series add constraint pk_time_series primary key (time_series_id)
go
alter table dbo.time_series add constraint un_time_series unique (time_series_name)
go

alter table data_source drop constraint pk_data_source
go
alter table data_source drop constraint un_data_source
go
drop table data_source
go
CREATE TABLE data_source
(
    data_source_id             int identity not null,
    data_source_name           varchar(200) NOT NULL  
);
alter table data_source add constraint pk_data_source primary key (data_source_id)
go
alter table data_source add constraint un_data_source unique (data_source_name)
go


alter table time_series_data  drop constraint pk_time_series_data 
go
alter table time_series_data drop constraint fk_time_series_data_time_series 
go
alter table time_series_data drop constraint fk_time_series_data_time_data_source 
go
drop table time_series_data
go

CREATE TABLE time_series_data
(
    time_series_id             int, 
    data_source_id             int,
    observation_time           datetime, 
    observation_value          float NOT NULL,

    
);

alter table time_series_data add constraint pk_time_series_data primary key (time_series_id, data_source_id, observation_time)
go

alter table time_series_data add constraint fk_time_series_data_time_series foreign key (time_series_id) references time_series(time_series_id)
go

alter table time_series_data add constraint fk_time_series_data_data_source foreign key (data_source_id) references data_source(data_source_id)
go

alter table attribute drop constraint pk_attribute
go
alter table attribute drop constraint un_attribute
go
drop table attribute
go

CREATE TABLE attribute
(
    attribute_id               int identity not null,
    attribute_name             varchar(200) NOT NULL, 
    table_name                 varchar(200) NOT NULL,
    primary_key_col_name       varchar(200) NOT NULL,
    description_col_name       varchar(200) NOT NULL
);
alter table attribute add constraint pk_attribute primary key (attribute_id)
go
alter table attribute add constraint un_attribute unique (attribute_name)
go

alter table time_series_attribute_map drop constraint pk_time_series_attribute_map 
go
alter table time_series_attribute_map drop constraint un_time_series_attribute_map
go
alter table time_series_attribute_map drop constraint fk_time_series_attribute_map_time_series_id
go
alter table time_series_attribute_map drop constraint fk_time_series_attribute_map_attribute_id
go
drop table time_series_attribute_map
go

CREATE TABLE time_series_attribute_map
(
    time_series_id             int,
    attribute_id               int, 
    attribute_value_id         int NOT NULL,
    
);
alter table time_series_attribute_map add constraint pk_time_series_attribute_map primary key (time_series_id,attribute_id)
go

alter table time_series_attribute_map add constraint fk_time_series_attribute_map_time_series_id foreign key (time_series_id) references time_series(time_series_id)
go

alter table time_series_attribute_map add constraint fk_time_series_attribute_map_attribute_id foreign key (attribute_id) references attribute(attribute_id)
go

alter table general_attribute_value drop constraint pk_general_attribute_value
go
alter table general_attriubte_value drop constraint fk_general_attribute_value
go
drop table general_attribute_value

CREATE TABLE general_attribute_value
(
    attribute_value_id         int identity not null, 
    attribute_value_name       varchar(200)  NOT NULL
);
alter table general_attribute_value add constraint pk_general_attribute_value  primary key (attribute_value_id)
go

alter table ticker drop constraint pk_ticker
go
alter table ticker drop constraint un_ticker
go
drop table ticker
go

CREATE TABLE ticker
(
    ticker_id                  numeric(9,0) identity not null, 
    ticker_name                varchar(200) NOT NULL, 
    ticker_description         varchar(200) NOT NULL
);
alter table ticker add constraint pk_ticker primary key (ticker_id)
go
alter table ticker add constraint un_ticker unique (ticker_name)
go

drop table credit_rating
go

CREATE TABLE credit_rating
(
    value                      float not null, 
    snp                        varchar(32), 
    fitch                      varchar(32),
    moodys                     varchar(32)
);

alter table red_code drop constraint pk_red_code
go
alter table red_code drop constraint un_red_code
go

drop table red_code
go

CREATE TABLE red_code
(
    red_code_id					numeric(9,0) identity not null,
    red_code					varchar(32) not null
)

alter table red_code add constraint pk_red_code primary key (red_code_id)
go
alter table red_code add constraint un_red_code unique (red_code)
go


alter table cds_ticker drop constraint pk_cds_ticker
go

alter table cds_ticker drop constraint fk_cds_ticker_ticker
go

alter table cds_ticker drop constraint fk_cds_ticker_tier
go

alter table cds_ticker drop constraint fk_cds_ticker_ccy
go

alter table cds_ticker drop constraint fk_cds_ticker_doc_clause
go

alter table cds_ticker drop constraint un_cds_ticker_name
go

drop table cds_ticker
go

CREATE TABLE cds_ticker
(
    cds_ticker_id				numeric(9,0) identity not null,
	cds_ticker_name				varchar(200) not null, 
    ticker_id					numeric(9,0) not null,
    tier_id						int not null,
    ccy_id						int not null,
	doc_clause_id				int not null
);

alter table cds_ticker add constraint pk_cds_ticker primary key (cds_ticker_id)
go

alter table cds_ticker add constraint fk_cds_ticker_ticker foreign key (ticker_id) references ticker(ticker_id)
go

alter table cds_ticker add constraint fk_cds_ticker_tier foreign key (tier_id) references general_attribute_value(attribute_value_id)
go

alter table cds_ticker add constraint fk_cds_ticker_ccy foreign key (ccy_id) references general_attribute_value(attribute_value_id)
go

alter table cds_ticker add constraint fk_cds_ticker_doc_clause foreign key (doc_clause_id) references general_attribute_value(attribute_value_id)
go

alter table cds_ticker add constraint un_cds_ticker_name unique (cds_ticker_name)
go

alter table corporate_action drop constraint pk_corporate_action
go

alter table corporate_action drop constraint fk_corporate_action_cds_ticker_id
go

alter table corporate_action drop constraint fk_corporate_action_related_cds_ticker_id
go

drop table corporate_action
go

CREATE TABLE corporate_action
(
    cds_ticker_id				numeric(9,0) not null,
	related_cds_ticker_id		numeric(9,0) not null, 
    series_order				int not null,
    effective_date				datetime,
    comment						varchar(256)
);

alter table corporate_action add constraint pk_corporate_action primary key (cds_ticker_id, series_order, related_cds_ticker_id)
go

alter table corporate_action add constraint fk_corporate_action_cds_ticker_id foreign key (cds_ticker_id) references cds_ticker(cds_ticker_id)
go

alter table corporate_action add constraint fk_corporate_action_related_cds_ticker_id foreign key (related_cds_ticker_id) references cds_ticker(cds_ticker_id)
go

alter table credit_ticker_lookup drop constraint pk_credit_ticker_lookup
go

drop table credit_ticker_lookup
go

CREATE TABLE credit_ticker_lookup
(
    markit						varchar(200) not null,
    bloomberg					varchar(200),
    option_metrics_id			int
);

alter table credit_ticker_lookup add constraint pk_credit_ticker_lookup primary key (markit)
go

alter table ccy drop constraint pk_ccy
go

drop table ccy
go

CREATE TABLE ccy
(
    ccy_id						int identity not null,
    ccy_name					varchar(200) not null,
    precedence					float not null

);

alter table ccy add constraint pk_ccy primary key (ccy_id)
go

alter table ccy_pair drop constraint fk_ccy_pair_ccy_id1
go

alter table ccy_pair drop constraint fk_ccy_pair_ccy_id2
go

alter table ccy_pair drop constraint un_ccy_pair
go

alter table ccy_pair drop constraint pk_ccy_pair
go

drop table ccy_pair
go

CREATE TABLE ccy_pair
(
    ccy_pair_id					int identity not null,
    ccy_pair_name				varchar(200) not null,
    ccy_id1						int not null,
    ccy_id2						int not null
);

alter table ccy_pair add constraint pk_ccy_pair primary key (ccy_pair_id)
go

alter table ccy_pair add constraint fk_ccy_pair_ccy_id1 foreign key (ccy_id1) references ccy(ccy_id)
go

alter table ccy_pair add constraint fk_ccy_pair_ccy_id2 foreign key (ccy_id2) references ccy(ccy_id)
go

alter table ccy_pair add constraint un_ccy_pair unique (ccy_pair_name)
go