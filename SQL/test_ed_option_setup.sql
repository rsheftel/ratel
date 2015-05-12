insert into data_source (data_source_name) values ('goldman')

insert into time_series (time_series_name) values('edk07 str 94.625 underlying_price')
insert into time_series (time_series_name) values('edk07 str 94.625 price')
insert into time_series (time_series_name) values('edk07 str 94.625 vol_bp_daily')
insert into time_series (time_series_name) values('edk07 str 94.625 vol_ln')

insert into attribute (attribute_name, table_name, primary_key_col_name, description_col_name) values ('option_type', 'general_attribute_value', 'attribute_value_id', 'attribute_value_name')
insert into attribute (attribute_name, table_name, primary_key_col_name, description_col_name) values ('strike', 'general_attribute_value', 'attribute_value_id', 'attribute_value_name')

insert into ticker values ('edk07', 'CME ED Future May 2007')

insert into general_attribute_value (attribute_value_name, attribute_id) values ('straddle', 3)
insert into general_attribute_value (attribute_value_name, attribute_id) values ('call', 3)
insert into general_attribute_value (attribute_value_name, attribute_id) values ('put', 3)

-- Yuck
insert into general_attribute_value (attribute_value_name, attribute_id) values ('94.625', 4)
insert into general_attribute_value (attribute_value_name, attribute_id) values ('underlying_price', 1)
insert into general_attribute_value (attribute_value_name, attribute_id) values ('price', 1)
insert into general_attribute_value (attribute_value_name, attribute_id) values ('vol_bp_daily', 1)
insert into general_attribute_value (attribute_value_name, attribute_id) values ('vol_ln', 1)

INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (12, 1, 10); --quote type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (12, 2, 3); -- ticker
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (12, 3, 6); -- option type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (12, 4, 9); -- strike

INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (13, 1, 11); --quote type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (13, 2, 3); -- ticker
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (13, 3, 6); -- option type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (13, 4, 9); -- strike

INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (14, 1, 12); --quote type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (14, 2, 3); -- ticker
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (14, 3, 6); -- option type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (14, 4, 9); -- strike

INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (15, 1, 13); --quote type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (15, 2, 3); -- ticker
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (15, 3, 6); -- option type
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (15, 4, 9); -- strike

