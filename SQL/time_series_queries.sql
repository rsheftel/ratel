select 
    observation_time, 
    observation_value 
from 
    time_series_data tsd, 
    data_source ds 
where 
    tsd.data_source_id = ds.data_source_id and 
    ds.data_source_name = 'yahoo' and 
    tsd.time_series_id in 
    ( 
        select
            tsam1.time_series_id
        from
            time_series_attribute_map tsam1, 
            time_series_attribute_map tsam2 
        where 
            tsam1.time_series_id = tsam2.time_series_id and 
            tsam1.attribute_id = 1 and 
            tsam2.attribute_id = 2 and 
            tsam1.attribute_value = 3 and
            tsam2.attribute_value = 1
    )
;
