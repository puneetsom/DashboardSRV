DECLARE 
CURSOR inprogress_cur
IS
SELECT /*+full (iv) parallel (iv, 16) */
       i.customer_id,
       (SELECT TRIM (   emp.emp_first_name
                     || ' '
                     || emp.emp_middle_name
                     || ' '
                     || emp.employee_surname
                    )
          FROM employee emp
         WHERE emp.employee_id = asgn.rep_id) sales_rep,
       asgn.rep_id,
       (SELECT TRIM (   emp1.emp_first_name
                     || ' '
                     || emp1.emp_middle_name
                     || ' '
                     || emp1.employee_surname
                    )
          FROM employee emp1
         WHERE emp1.employee_id = asgn.sales_mngr_id) sales_manager,
       asgn.sales_mngr_id,
       (SELECT TRIM (   emp11.emp_first_name
                     || ' '
                     || emp11.emp_middle_name
                     || ' '
                     || emp11.employee_surname
                    )
          FROM employee emp11
         WHERE emp11.employee_id = asgn.general_mngr_id) general_manager,
       asgn.general_mngr_id, p.in_progress_ind,
       (SELECT l.finding_name
          FROM listing l
         WHERE l.listing_id(+) = i.listing_id AND l.last_version_ind(+) =
                                                             'Y')
                                                                 finding_name,
       (SELECT product_name
          FROM product_issue pi
         WHERE pi.product_code = pc.product_code
           AND pi.product_issue_num = pc.product_issue_num) product_name,
       (SELECT to_date(TO_CHAR(issue_date, 'yyyy-mm-dd'),'yyyy-mm-dd')
          FROM product_issue pi
         WHERE pi.product_code = pc.product_code
           AND pi.product_issue_num = pc.product_issue_num) issue_date,
       i.item_id, iv.priceplan_id, i.udac_code,
       (SELECT heading_name
          FROM heading h1
         WHERE heading_code = i.heading_code
           AND effective_from_date =
                                    (SELECT MAX (h2.effective_from_date)
                                       FROM heading h2
                                      WHERE h2.heading_code = h1.heading_code))
                                                                 heading_name,
       (SELECT (   (   '('
                    || NVL (l.atn_npa, ' ')
                    || ')'
                    || NVL (l.atn_cop, ' ')
                    || '-'
                    || NVL (l.atn_line_no, ' ')
                   )
                || DECODE (NVL (l.ali_code, ''), '', '', '/' || l.ali_code)
               )
          FROM listing l
         WHERE l.listing_id(+) = i.listing_id AND l.last_version_ind(+) = 'Y')
                                                                          atn,
       NVL (p.sfa_bots_amt, 0), NVL (p.sfa_nisd_amt, 0),
       (SELECT vertical
          FROM rt_vertical rv
         WHERE rv.heading_code = p.dominant_heading) vertical,
       ruf.udac_family_name CATEGORY, cust.ppc_ind,
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
  FROM item i,
       item_imv iv,
       customer cust,
       product p,
       prd_curr_issue pc,
       assignment asgn,
       udac u,
       rate_udac_family ruf
 WHERE iv.customer_id = i.customer_id
   AND iv.product_code = i.product_code
   AND iv.product_issue_num = i.product_issue_num
   AND iv.item_id = i.item_id
   AND iv.item_version = i.item_version
   AND i.last_version_ind = 'Y'
   AND cust.customer_id = i.customer_id
   AND cust.customer_id = iv.customer_id
   AND p.customer_id = i.customer_id
   AND p.product_code = i.product_code
   AND p.product_issue_num = i.product_issue_num
   AND p.product_code = pc.product_code
   AND p.product_issue_num = pc.product_issue_num
   AND p.customer_id = cust.customer_id
   AND pc.product_code = i.product_code
   AND pc.product_issue_num = i.product_issue_num
   AND pc.product_code = iv.product_code
   AND pc.product_issue_num = iv.product_issue_num
   AND p.regular_assign_id = asgn.assignment_id
   AND p.product_status NOT IN ('E', 'C')
   AND i.closed_contract_ind = 'I'
   AND i.udac_code = u.udac_code
   AND u.rate_udac_family_cod = ruf.rate_udac_fmly_code
   AND i.item_id NOT IN (
          (SELECT item_id
             FROM QUERY, query_ref qr
            WHERE QUERY.customer_id = i.customer_id
              AND QUERY.product_code = i.product_code
              AND QUERY.product_issue_num = i.product_issue_num
              AND QUERY.query_code = qr.query_code
              AND qr.query_level = 'I'
              AND QUERY.query_status IN (1, 2)
              AND QUERY.item_id = i.item_id
              AND QUERY.from_item_version <= i.item_version
              AND (   QUERY.resol_item_version > i.item_version
                   OR QUERY.resol_item_version IS NULL
                  ))
          UNION
          (SELECT i1.item_id
             FROM QUERY, query_ref qr, item i1
            WHERE QUERY.customer_id = i.customer_id
              AND QUERY.product_code = i.product_code
              AND QUERY.product_issue_num = i.product_issue_num
              AND i.customer_id = i1.customer_id
              AND i.product_code = i1.product_code
              AND i.product_issue_num = i1.product_issue_num
              AND QUERY.customer_id = i1.customer_id
              AND QUERY.product_code = i1.product_code
              AND QUERY.product_issue_num = i1.product_issue_num
              AND QUERY.contract_id = i1.contract_id
              AND QUERY.query_code = qr.query_code
              AND qr.query_level = 'C'
              AND qr.hold_publish_ind = 'Y'
              AND QUERY.from_ctr_seq_num <= i1.contract_seq_number
              AND (   QUERY.resol_ctr_seq_num > i1.contract_seq_number
                   OR QUERY.resol_ctr_seq_num IS NULL
                  )));


        
TYPE inprogress_aat IS TABLE OF servicemgrdb_inprogressitems%ROWTYPE
      INDEX BY PLS_INTEGER;

   l_inprogress   inprogress_aat;
BEGIN
   OPEN inprogress_cur;

   LOOP
      FETCH inprogress_cur
      BULK COLLECT INTO l_inprogress LIMIT 1000;

      EXIT WHEN l_inprogress.COUNT = 0;
      FORALL indx IN 1 .. l_inprogress.COUNT
         INSERT INTO servicemgrdb_inprogressitems
              VALUES l_inprogress (indx);
      COMMIT;
      EXIT WHEN inprogress_cur%NOTFOUND;
   END LOOP;

   CLOSE inprogress_cur;
END;
/