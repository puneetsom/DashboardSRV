select BA.CUSTOMER_ID,
       BA.BILLING_ACCOUNT_ID,
       BA.BILLING_NAME,
       substr(C.HBD, 4, 2) as STATE,
       FN.SUPPRESSION_REASON,
       FN.SS_EMAIL_ADD
  from FN_ACCOUNT FN, BILLING_ACCOUNT BA, CUSTOMER C
 where SUPPRESSION_REASON in ('EB', 'PE')
   and FN.ACCOUNT_ID = BA.BILLING_ACCOUNT_ID
   and BA.CUSTOMER_ID = C.CUSTOMER_ID
   and C.DERIVED_CUST_IND = '0'