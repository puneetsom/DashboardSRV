DECLARE 
CURSOR contracts_cur
IS
     SELECT /*+ parallel ( p,8) */ cust.customer_id, 0,
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
               WHERE emp11.employee_id = asgn.general_mngr_id)
                                                              general_manager,
             asgn.general_mngr_id, 'UnHandled' contract_status,
             l.finding_name,
             (SELECT product_name
                FROM product_issue pi
               WHERE pi.product_code = pc.product_code
                 AND pi.product_issue_num = pc.product_issue_num)
                                                                 product_name,
             (SELECT to_date( TO_CHAR (issue_date, 'yyyy-mm-dd'),'yyyy-mm-dd')
                FROM product_issue pi
               WHERE pi.product_code = pc.product_code
                 AND pi.product_issue_num = pc.product_issue_num) issue_date,
             (   (   '('
                  || NVL (l.atn_npa, ' ')
                  || ')'
                  || NVL (l.atn_cop, ' ')
                  || '-'
                  || NVL (l.atn_line_no, ' ')
                 )
              || DECODE (NVL (l.ali_code, ''), '', '', '/' || l.ali_code)
             ) atn,
             NVL (p.sfa_bots_amt, 0), NVL (p.sfa_nisd_amt, 0),
             (SELECT NVL (vertical, ' ')
                FROM rt_vertical rv
               WHERE rv.heading_code = p.dominant_heading) vertical,
             (SELECT NVL (ruf.udac_family_name, ' ')
                FROM udac u, rate_udac_family ruf
               WHERE u.rate_udac_family_cod =
                                             ruf.rate_udac_fmly_code
                 AND u.udac_code = p.dominant_udac_code) CATEGORY,
             NVL (cust.ppc_ind, ' '), asgn.office_code
        FROM product p,
             prd_curr_issue pc,
             listing l,
             customer cust,
             assignment asgn
       WHERE p.product_code = pc.product_code
         AND p.product_issue_num = pc.product_issue_num
         AND p.customer_id = cust.customer_id
         AND p.product_status NOT IN ('E', 'C')
         AND p.retirement_status IN ('A', 'N')
         AND cust.main_main_listing_id = l.listing_id(+)
         AND cust.derived_cust_ind = '0'
         AND l.last_version_ind(+) = 'Y'
         AND p.regular_assign_id = asgn.assignment_id(+);

   TYPE contracts_aat IS TABLE OF servicemgrdb_contracts%ROWTYPE
      INDEX BY PLS_INTEGER;

   l_contracts   contracts_aat;
BEGIN
   OPEN contracts_cur;

   LOOP
      FETCH contracts_cur
      BULK COLLECT INTO l_contracts LIMIT 1000;

      EXIT WHEN l_contracts.COUNT = 0;
      FORALL indx IN 1 .. l_contracts.COUNT
         INSERT INTO servicemgrdb_contracts
              VALUES l_contracts (indx);
      COMMIT;
      EXIT WHEN contracts_cur%NOTFOUND;
   END LOOP;

   CLOSE contracts_cur;
END;
/