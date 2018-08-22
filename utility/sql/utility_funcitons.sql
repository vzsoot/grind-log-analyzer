CREATE OR REPLACE FUNCTION diff_by_logger(failed_id integer, success_id integer, logger_name varchar(128)) returns table(row_num bigint, failed_message text, success_message text)
AS '
    select row_number() over() as row_num, fm as failed_message, sm as success_message from
	(select row_number() over() as rn, * from (select message as fm from log_entry where testcaseid = $1 and logger = $3 order by timestamp, milliseconds) as q) as f
    full outer join
	(select row_number() over() as rn, * from (select message as sm from log_entry where testcaseid = $2 and logger = $3 order by timestamp, milliseconds) as q) as s
    on (f.rn = s.rn)
    '
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION diff_by_logger_count(failed_id integer, success_id integer) returns table(failed_logger varchar(128), fc bigint, success_logger varchar(128), sc bigint)
AS '
    select fail_logger, fc, success_logger, sc from
	(select logger as fail_logger, count(logger) as fc from log_entry where testcaseid = $1 group by logger) as f
    full outer join
	(select logger as success_logger, count(logger) as sc from log_entry where testcaseid = $2 group by logger) as s
    on (f.fail_logger = s.success_logger)
    '
LANGUAGE SQL;
