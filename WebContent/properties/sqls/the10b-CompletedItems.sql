SELECT /*+ full(m) parallel(m,8) */ i.customer_id,
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
       product p,
       customer cust,
       assignment asgn,
       prd_curr_issue pc,
       listing l,
       op_logical_date o,
       udac u,
       rate_udac_family ruf
 WHERE iv.customer_id = i.customer_id
   AND iv.product_code = i.product_code
   AND iv.product_issue_num = i.product_issue_num
   AND iv.item_id = i.item_id
   AND iv.item_version = i.item_version
   AND iv.fulfillment_status = 'S'
   AND p.customer_id = i.customer_id
   AND p.product_code = i.product_code
   AND p.product_issue_num = i.product_issue_num
   AND p.customer_id = iv.customer_id
   AND p.product_code = iv.product_code
   AND p.product_issue_num = iv.product_issue_num
   AND cust.customer_id = i.customer_id
   AND cust.derived_cust_ind = '0'   
   AND cust.customer_id = p.customer_id
   AND cust.customer_id = iv.customer_id
   AND p.regular_assign_id = asgn.assignment_id
   AND pc.product_code = p.product_code
   AND pc.product_issue_num = p.product_issue_num
   AND i.listing_id = l.listing_id(+)
   AND l.last_version_ind(+) = 'Y'
   AND i.closed_contract_ind = 'I'
   AND i.udac_code = u.udac_code
   AND u.rate_udac_family_cod = ruf.rate_udac_fmly_code
   AND i.last_version_ind = 'Y'

--   AND iv.customer_id IN (SELECT customer_id
--                           FROM customer cust1
--                          WHERE ROWNUM < 1000000)

--   AND o.logical_date_type = 'O'
--   AND iv.live_date >= (o.logical_date - 2)
