select /*+ full(m) parallel(m,8) */ i.customer_id,
       (SELECT TRIM(emp.emp_first_name
                 || ' '
                 || emp.emp_middle_name
                 || ' '
                 || emp.employee_surname)
          FROM employee emp
         WHERE emp.employee_id = asgn.rep_id) sales_rep,
        asgn.rep_id,
       (SELECT TRIM(emp1.emp_first_name
                 || ' '
                 || emp1.emp_middle_name
                 || ' '
                 || emp1.employee_surname)
          FROM employee emp1
         WHERE emp1.employee_id = asgn.sales_mngr_id) sales_manager,
       asgn.sales_mngr_id,
       (SELECT TRIM(emp11.emp_first_name
                 || ' '
                 || emp11.emp_middle_name
                 || ' '
                 || emp11.employee_surname)
          FROM employee emp11
         WHERE emp11.employee_id = asgn.general_mngr_id) general_manager,
       asgn.general_mngr_id,
       P.in_progress_ind,
       l.finding_name,
       (SELECT product_name
          FROM product_issue pi
         WHERE pi.product_code = pc.product_code
           AND pi.product_issue_num = pc.product_issue_num) product_name,
       (SELECT TO_CHAR(issue_date, 'yyyy-mm-dd')
          FROM product_issue pi
         WHERE pi.product_code = pc.product_code
           AND pi.product_issue_num = pc.product_issue_num) issue_date,
       i.item_id,
       iv.priceplan_id, 
       i.udac_code,
       (SELECT heading_name
          FROM heading h1
         WHERE heading_code = i.heading_code
           AND effective_from_date =
                                    (SELECT MAX (h2.effective_from_date)
                                       FROM heading h2
                                      WHERE h2.heading_code = h1.heading_code))
                                                                 heading_name,
       (   '('
        || NVL (l.atn_npa, ' ')
        || ')-'
        || NVL (l.atn_cop, ' ')
        || '-'
        || NVL (l.atn_line_no, ' ')
       ) atn,
       NVL (l.ali_code, ' '),
       NVL (p.sfa_bots_amt, 0), 
       NVL (p.sfa_nisd_amt, 0), 
       (SELECT vertical
          FROM rt_vertical rv
         WHERE rv.heading_code = p.dominant_heading) vertical,
       ruf.udac_family_name CATEGORY,
       cust.ppc_ind,
       (CASE
           WHEN (   ruf.rate_udac_fmly_code = 'L'
                 OR ruf.rate_udac_fmly_code = 'ML'
                 OR ruf.rate_udac_fmly_code = 'PL'
                 OR ruf.rate_udac_fmly_code = 'U'
                 OR ruf.rate_udac_fmly_code = 'W'
                 OR ruf.rate_udac_fmly_code = 'RB'
                )
              THEN 'Listings'
           WHEN (ruf.rate_udac_fmly_code = 'VV')
              THEN 'Video'
           WHEN (ruf.rate_udac_fmly_code = 'PA')
              THEN 'Print-Ad'
           WHEN (   ruf.rate_udac_fmly_code = 'SE'
                 OR ruf.rate_udac_fmly_code = 'SC'
                )
              THEN 'Search'
           WHEN (   ruf.rate_udac_fmly_code = 'WS'
                 OR ruf.rate_udac_fmly_code = 'WA'
                )
              THEN 'Web Site'
           ELSE 'Others'
        END
       ) udactype,
       asgn.office_code
  from ITEM I,
       item_imv iv,
       LISTING L,
       PRODUCT P,
       CUSTOMER Cust,
       PRD_CURR_ISSUE PC,
       ASSIGNMENT ASGN,
       udac u,
       rate_udac_family ruf
 WHERE iv.customer_id = i.customer_id
   AND iv.product_code = i.product_code
   AND iv.product_issue_num = i.product_issue_num
   AND iv.item_id = i.item_id
   AND iv.item_version = i.item_version 
   AND I.CUSTOMER_ID = P.CUSTOMER_ID
   AND I.CUSTOMER_ID = Cust.CUSTOMER_ID
   AND Cust.CUSTOMER_ID = P.CUSTOMER_ID
   AND Cust.derived_cust_ind = '0'
   AND I.PRODUCT_CODE = P.PRODUCT_CODE
   AND I.PRODUCT_ISSUE_NUM = P.PRODUCT_ISSUE_NUM
   AND P.PRODUCT_CODE = PC.PRODUCT_CODE
   AND P.PRODUCT_ISSUE_NUM = PC.PRODUCT_ISSUE_NUM
   AND I.PRODUCT_CODE = pc.product_code
   AND I.PRODUCT_ISSUE_NUM = PC.PRODUCT_ISSUE_NUM
   AND I.LISTING_ID = L.LISTING_ID (+)
   AND i.udac_code = u.udac_code
   AND u.rate_udac_family_cod = ruf.rate_udac_fmly_code 
   AND L.LAST_VERSION_IND(+) = 'Y'
   AND I.CLOSED_CONTRACT_IND ='I'
   AND I.LAST_VERSION_IND = 'Y' 
   AND I.ITEM_ID NOT IN  (
     (SELECT ITEM_ID FROM query , query_ref QR
      WHERE QUERY.CUSTOMER_ID = I.CUSTOMER_ID
      AND QUERY.QUERY_CODE = QR.QUERY_CODE
      AND QR.QUERY_LEVEL ='I'
      AND QUERY.QUERY_STATUS IN ('1','2')
      AND QUERY.PRODUCT_CODE = I.PRODUCT_CODE
      AND QUERY.PRODUCT_ISSUE_NUM = I.PRODUCT_ISSUE_NUM
      AND QUERY.ITEM_ID = I.ITEM_ID
      AND QUERY.FROM_ITEM_VERSION <= I.ITEM_VERSION
      AND (QUERY.RESOL_ITEM_VERSION > I.ITEM_VERSION
           OR QUERY.RESOL_ITEM_VERSION IS NULL ))
       UNION
       (
         SELECT I1.ITEM_ID FROM QUERY , QUERY_REF QR, ITEM I1
         WHERE QUERY.CUSTOMER_ID = I.CUSTOMER_ID
           AND QUERY.PRODUCT_CODE = I.PRODUCT_CODE
           AND QUERY.PRODUCT_ISSUE_NUM = I.PRODUCT_ISSUE_NUM
           AND I.CUSTOMER_ID = I1.CUSTOMER_ID
           AND I.PRODUCT_CODE = I1.PRODUCT_CODE
           AND I.PRODUCT_ISSUE_NUM = I1.PRODUCT_ISSUE_NUM
           AND QUERY.CUSTOMER_ID = I1.CUSTOMER_ID
           AND QUERY.PRODUCT_CODE = I1.PRODUCT_CODE
           AND QUERY.PRODUCT_ISSUE_NUM = I1.PRODUCT_ISSUE_NUM
           AND QUERY.CONTRACT_ID = I1.CONTRACT_ID
           AND QUERY.QUERY_CODE = QR.QUERY_CODE
           AND QR.QUERY_LEVEL = 'C'
          And qr.hold_publish_ind='Y'
           AND query.from_ctr_seq_num <= I1.contract_seq_number
           AND ( query.resol_ctr_seq_num > I1.contract_seq_number
              or query.resol_ctr_seq_num is null )
            )   
       )

-- AND P.REGULAR_ASSIGN_ID =  ASGN.ASSIGNMENT_ID       

