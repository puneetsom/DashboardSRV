select PR.ACCOUNT_ID,
       BA.BILLING_NAME,
       PR.AMOUNT,
       to_char(PR.CREATION_DATE, 'YYYY-MM-DD'),
       to_char(PR.DUE_DATE, 'YYYY-MM-DD')
  from PAYMENT_REPOSITORY PR, BILLING_ACCOUNT BA
 where to_char(PR.CTCR_SYS_CRE_DT, 'YYYY-MM-DD') = to_char(SYSDATE, 'YYYY-MM-DD')
   and PR.FORM_ID is null
   and PR.CTCR_ONLINE_OPER_ID is null
   and PR.CTCR_BATCH_PROG_ID is null
   and PR.AMOUNT <> 0
   and PR.ACCOUNT_ID = BA.BILLING_ACCOUNT_ID