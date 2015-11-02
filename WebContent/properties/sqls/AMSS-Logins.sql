select case substr(EXTERNAL_ID, 6, 1)
         when '1' then 'Southwest'
         when '2' then 'Midwest'
         when '3' then 'Southwest'
         when '4' then 'Midwest'
         when '5' then 'West'
         when '6' then 'Midwest'
         when '7' then 'Midwest'
         when '8' then 'West'
         when '9' then 'Southeast'
         else 'Other' end AREA,
       substr(external_id, INSTR(external_id,'_')+1, INSTR(external_id,'_',6)-INSTR(external_id,'_')-1) CUSTOMER_ID, NAME, USER_HIERARCHIES.USER_ID
  from PSSDBO1.UAMS_LOG LEFT OUTER JOIN PSSDBO1.USER_HIERARCHIES
    on UAMS_LOG.USER_ID = USER_HIERARCHIES.USER_ID
 where UAMS_LOG.USER_ID not in ('system', 'guest', 'root', 'selfReg', 'coreDefUser', 'Unauthenticated', 'RepSA', 'N/A')
   and UAMS_LOG.USER_ID not like 'qay%'
   and UAMS_LOG.USER_ID not like 'yp_amss_%'
   and UAMS_LOG.USER_ID not like 'yp_supp_%'
   and UAMS_LOG.USER_ID not like 'YP_SUPP_%'
   and UAMS_LOG.USER_ID not like 'yp_admin_%'
   and UAMS_LOG.USER_ID not like 'ap_csr%'
   and UAMS_LOG.USER_ID not like 'ap_fcr%'
   and to_char(EVENT_DATE, 'YYYY.MM.DD') = to_char(SYSDATE, 'YYYY.MM.DD')