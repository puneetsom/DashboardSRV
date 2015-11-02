select INITIATOR_NAME, request_status, utl_raw.cast_to_varchar2(REQUEST_CONTENT)
  from PSSDBO1.request
 where request_type = 'CUST'
   and substr(LAST_UPDATE_DATE, 0, 10) = to_char(SYSDATE, 'YYYY-MM-DD')