DECLARE 
CURSOR query_cur
IS
SELECT  /*+parallel( i,8 ) */ i.customer_id,
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
       QUERY.query_code,
       qr.query_group,
       qr.query_level,
      ( select l.finding_name from listing l where l.listing_id(+) = i.listing_id and l.last_version_ind(+)='Y') finding_name,
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
    (  select   (   (   '('
                  || NVL (l.atn_npa, ' ')
                  || ')'
                  || NVL (l.atn_cop, ' ')
                  || '-'
                  || NVL (l.atn_line_no, ' ')
                 )
              || DECODE (NVL (l.ali_code, ''), '', '', '/' || l.ali_code)
             ) 
         from listing l where l.listing_id(+) = i.listing_id and l.last_version_ind(+)='Y' ) atn  ,
 --      NVL (l.ali_code, ' '),
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
  FROM item i,
       item_imv iv,
       product p,
       prd_curr_issue pc,
       customer cust,
       assignment asgn,
       QUERY,
       query_ref qr,
       udac u,
       rate_udac_family ruf
 WHERE iv.customer_id = i.customer_id
   AND iv.product_code = i.product_code
   AND iv.product_issue_num = i.product_issue_num
   AND iv.item_id = i.item_id
   AND iv.item_version = i.item_version
   AND i.customer_id = p.customer_id
   AND i.product_code = p.product_code
   AND i.product_issue_num = p.product_issue_num
   AND i.product_code = pc.product_code
   AND i.product_issue_num = pc.product_issue_num
   AND p.product_code = pc.product_code
   AND p.product_issue_num = pc.product_issue_num
   AND iv.customer_id = p.customer_id
   AND iv.product_code = p.product_code
   AND iv.product_issue_num = p.product_issue_num
   and iv.product_code=pc.product_code
   and iv.product_issue_num = pc.product_issue_num
   and i.customer_id = cust.customer_id
   and iv.customer_id  = cust.customer_id
   and p.customer_id = cust.customer_id
    AND p.regular_assign_id = asgn.assignment_id
    AND QUERY.customer_id = i.customer_id
   AND QUERY.product_code = i.product_code
   AND QUERY.product_issue_num = i.product_issue_num
   AND QUERY.item_id = i.item_id
      AND QUERY.customer_id = p.customer_id
   AND QUERY.product_code = p.product_code
   AND QUERY.product_issue_num = p.product_issue_num
      AND QUERY.customer_id = iv.customer_id
   AND QUERY.product_code = iv.product_code
   AND QUERY.product_issue_num = iv.product_issue_num
    AND QUERY.product_code = pc.product_code
   AND QUERY.product_issue_num = pc.product_issue_num
   AND QUERY.query_code = qr.query_code
    AND i.udac_code = u.udac_code
   AND u.rate_udac_family_cod = ruf.rate_udac_fmly_code
   and p.product_status not in ('E','C')
    AND cust.derived_cust_ind = '0'
    and i.last_version_ind='Y'
   AND qr.query_level = 'I'
   AND QUERY.query_status IN (1, 2)
   AND QUERY.from_item_version <= i.item_version
   AND (   QUERY.resol_item_version > i.item_version
        OR QUERY.resol_item_version IS NULL)
UNION
SELECT  /*+parallel( i,8 ) */ i.customer_id,
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
       QUERY.query_code,
       qr.query_group,
       qr.query_level,
      ( select l.finding_name from listing l where l.listing_id(+) = i.listing_id and l.last_version_ind(+)='Y') finding_name,
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
    (  select   (   (   '('
                  || NVL (l.atn_npa, ' ')
                  || ')'
                  || NVL (l.atn_cop, ' ')
                  || '-'
                  || NVL (l.atn_line_no, ' ')
                 )
              || DECODE (NVL (l.ali_code, ''), '', '', '/' || l.ali_code)
             ) 
         from listing l where l.listing_id(+) = i.listing_id and l.last_version_ind(+)='Y' ) atn  ,
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
  FROM item i,
       item_imv iv,
       product p,
       prd_curr_issue pc,
       customer cust,
       assignment asgn,
       QUERY,
       query_ref qr,
       udac u,
       rate_udac_family ruf
 WHERE iv.customer_id = i.customer_id
   AND iv.product_code = i.product_code
   AND iv.product_issue_num = i.product_issue_num
   AND iv.item_id = i.item_id
   AND iv.item_version = i.item_version
   AND i.customer_id = p.customer_id
   AND i.product_code = p.product_code
   AND i.product_issue_num = p.product_issue_num
   AND i.product_code = pc.product_code
   AND i.product_issue_num = pc.product_issue_num
   AND p.product_code = pc.product_code
   AND p.product_issue_num = pc.product_issue_num
   AND iv.customer_id = p.customer_id
   AND iv.product_code = p.product_code
   AND iv.product_issue_num = p.product_issue_num
   and i.customer_id = cust.customer_id
   and iv.customer_id  = cust.customer_id
   and p.customer_id = cust.customer_id
    AND p.regular_assign_id = asgn.assignment_id
    AND QUERY.customer_id = i.customer_id
   AND QUERY.product_code = i.product_code
   AND QUERY.product_issue_num = i.product_issue_num
   AND QUERY.contract_id = i.contract_id
      AND QUERY.customer_id = p.customer_id
   AND QUERY.product_code = p.product_code
   AND QUERY.product_issue_num = p.product_issue_num
      AND QUERY.customer_id = iv.customer_id
   AND QUERY.product_code = iv.product_code
   AND QUERY.product_issue_num = iv.product_issue_num
    AND QUERY.product_code = pc.product_code
   AND QUERY.product_issue_num = pc.product_issue_num
   AND QUERY.query_code = qr.query_code
    AND i.udac_code = u.udac_code
   AND u.rate_udac_family_cod = ruf.rate_udac_fmly_code
   and p.product_status not in ('E','C')
    AND cust.derived_cust_ind = '0'
    and i.last_version_ind='Y'
   AND qr.query_level = 'C'
   AND qr.hold_publish_ind = 'Y'
   AND QUERY.query_status IN (1, 2)
   AND QUERY.from_ctr_seq_num <= i.contract_seq_number
   AND (   QUERY.resol_ctr_seq_num > i.contract_seq_number
        OR QUERY.resol_ctr_seq_num IS NULL);
        
TYPE query_aat IS TABLE OF servicemgrdb_querieditems%ROWTYPE
      INDEX BY PLS_INTEGER;

   l_query   query_aat;
BEGIN
   OPEN query_cur;

   LOOP
      FETCH query_cur
      BULK COLLECT INTO l_query LIMIT 1000;

      EXIT WHEN l_query.COUNT = 0;
      FORALL indx IN 1 .. l_query.COUNT
         INSERT INTO servicemgrdb_querieditems
              VALUES l_query (indx);
      COMMIT;
      EXIT WHEN query_cur%NOTFOUND;
   END LOOP;

   CLOSE query_cur;
END;
/