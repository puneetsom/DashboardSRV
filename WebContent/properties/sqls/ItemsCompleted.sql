DECLARE 
CURSOR completed_cur
IS
SELECT /*+ full(iv) parallel(iv,16) */ i.customer_id,
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
       NULL change_requests, 
       NVL (p.sfa_bots_amt, 0),
       NVL (p.sfa_nisd_amt, 0), 
       cust.home_data_base region,
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
  FROM item i,
       item_imv iv,
       customer cust,
       product p,
       prd_curr_issue pc,
       assignment asgn,
       op_logical_date op,
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
   AND iv.fulfillment_status = 'S'
   AND i.udac_code = u.udac_code
   AND u.rate_udac_family_cod = ruf.rate_udac_fmly_code
   and iv.actual_end_date > op.logical_date -1 
   and op.logical_date_type ='O';


        
TYPE completed_aat IS TABLE OF servicemgrdb_completeditems%ROWTYPE
      INDEX BY PLS_INTEGER;

   l_completed   completed_aat;
BEGIN
   OPEN completed_cur;

   LOOP
      FETCH completed_cur
      BULK COLLECT INTO l_completed LIMIT 1000;

      EXIT WHEN l_completed.COUNT = 0;
      FORALL indx IN 1 .. l_completed.COUNT
         INSERT INTO servicemgrdb_completeditems
              VALUES l_completed (indx);
      COMMIT;
      EXIT WHEN completed_cur%NOTFOUND;
   END LOOP;

   CLOSE completed_cur;
END;
/