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
       substr(external_id, 6, 10) CUSTOMER_ID, NAME, USER_ID
  from PSSDBO1.USER_HIERARCHIES
 where EXTERNAL_ID not in ('10000', '100004', '20000', '201', '202', '204', '10878769')
   and EXTERNAL_ID not like '-%'
   and USER_ID not in ('system', 'guest', 'root', 'selfReg', 'coreDefUser', 'Unauthenticated', 'RepSA')
   and USER_ID not like 'qay%'
   and USER_ID not like 'yp_amss_%'
   and USER_ID not like 'yp_supp_%'
   and USER_ID not like 'YP_SUPP_%'
   and USER_ID not like 'yp_admin_%'
   and USER_ID not like 'ap_csr%'
   and USER_ID not like 'ap_fcr%'
   and (substr(external_id, INSTR(external_id,'_')+1, INSTR(external_id,'_',6)-INSTR(external_id,'_')-1) >= 1000010000 
   		or substr(external_id, 6, 1) = '-')