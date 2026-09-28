select column_name, data_type, character_maximum_length, numeric_precision, numeric_scale, is_nullable
from information_schema.columns
where table_schema = current_schema() and table_name = 'expedicao'
order by ordinal_position;
