INSERT INTO time_series (time_series_name) VALUES ('aapl open');
INSERT INTO time_series (time_series_name) VALUES ('aapl high');
INSERT INTO time_series (time_series_name) VALUES ('aapl low');
INSERT INTO time_series (time_series_name) VALUES ('aapl close');
INSERT INTO time_series (time_series_name) VALUES ('aapl volume');
INSERT INTO time_series (time_series_name) VALUES ('s&p500 open');
INSERT INTO time_series (time_series_name) VALUES ('s&p500 high');
INSERT INTO time_series (time_series_name) VALUES ('s&p500 low');
INSERT INTO time_series (time_series_name) VALUES ('s&p500 close');
INSERT INTO time_series (time_series_name) VALUES ('s&p500 volume');

INSERT INTO data_source (data_source_name) VALUES ('yahoo');
INSERT INTO data_source (data_source_name) VALUES ('bogus');

INSERT INTO attribute (attribute_name, table_name, primary_key_col_name, description_col_name) VALUES ('quote_type', 'general_attribute_value', 'attribute_value_id', 'attribute_value_name');
INSERT INTO attribute (attribute_name, table_name, primary_key_col_name, description_col_name) VALUES ('ticker', 'ticker', 'ticker_id', 'ticker_name');

INSERT INTO ticker (ticker_name, ticker_description) VALUES ('aapl', 'Apple, Inc.');
INSERT INTO ticker (ticker_name, ticker_description) VALUES ('s&p500', 'S&P 500 Index');

INSERT INTO general_attribute_value (attribute_value_name, attribute_id) VALUES ('open', 1);
INSERT INTO general_attribute_value (attribute_value_name, attribute_id) VALUES ('high', 1);
INSERT INTO general_attribute_value (attribute_value_name, attribute_id) VALUES ('low', 1);
INSERT INTO general_attribute_value (attribute_value_name, attribute_id) VALUES ('close', 1);
INSERT INTO general_attribute_value (attribute_value_name, attribute_id) VALUES ('volume', 1);

INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (1, 1, 1);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (1, 2, 1);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (2, 1, 2);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (2, 2, 1);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (3, 1, 3);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (3, 2, 1);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (4, 1, 4);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (4, 2, 1);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (5, 1, 5);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (5, 2, 1);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (6, 1, 1);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (6, 2, 2);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (7, 1, 2);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (7, 2, 2);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (8, 1, 3);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (8, 2, 2);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (9, 1, 4);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (9, 2, 2);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (10, 1, 5);
INSERT INTO time_series_attribute_map (time_series_id, attribute_id, attribute_value_id) VALUES (10, 2, 2);

INSERT INTO time_series_data (time_series_id, data_source_id, observation_time, observation_value) VALUES (4, 2, '1949-10-22', 12.58);
INSERT INTO time_series_data (time_series_id, data_source_id, observation_time, observation_value) VALUES (4, 2, '1953-10-02', 29.91);
INSERT INTO time_series_data (time_series_id, data_source_id, observation_time, observation_value) VALUES (4, 2, '1978-05-08', 123.45);
INSERT INTO time_series_data (time_series_id, data_source_id, observation_time, observation_value) VALUES (4, 2, '1978-05-25', 129.97);
INSERT INTO time_series_data (time_series_id, data_source_id, observation_time, observation_value) VALUES (4, 2, '1998-06-19', 56.97);
INSERT INTO time_series_data (time_series_id, data_source_id, observation_time, observation_value) VALUES (4, 2, '2006-07-05', 91.23);
INSERT INTO time_series_data (time_series_id, data_source_id, observation_time, observation_value) VALUES (4, 2, '2006-07-06', 87.97);

-- COPY time_series_data FROM './test_time_series_data.txt';
