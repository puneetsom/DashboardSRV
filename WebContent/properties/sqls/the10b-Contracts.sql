SELECT cust.customer_id, cc.contract_id,
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
             asgn.general_mngr_id,
             (CASE
                 WHEN (    (cc.contract_type = 'B' OR cc.contract_type = 'P'
                           )
                       AND (   p.retirement_status = 'C'
                            OR p.retirement_status = 'R'
                           )
                      )
                    THEN 'Handled(Boost)'
                 WHEN (    NOT (   cc.contract_type = 'B'
                                OR cc.contract_type = 'P'
                               )
                       AND (   p.retirement_status = 'C'
                            OR p.retirement_status = 'R'
                           )
                      )
                    THEN 'Handled(Closed)'
                 WHEN p.retirement_status = 'A'
                    THEN 'UnHandled'
                 WHEN p.retirement_status = 'N'
                    THEN 'UnHandled'
                 WHEN p.retirement_status = 'M'
                    THEN 'Must-Be-ReHandled'
                 WHEN p.retirement_status = 'U'
                    THEN 'Uploaded'
              END
             ) contract_status,
             l.finding_name,
             (SELECT product_name
                FROM product_issue pi
               WHERE pi.product_code = pc.product_code
                 AND pi.product_issue_num = pc.product_issue_num)
                                                                product_name,
             (SELECT TO_DATE (issue_date, 'yyyy-mm-dd')
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
        FROM contract cc,
             prd_curr_issue pc,
             product p,
             listing l,
             customer cust,
             assignment asgn
       WHERE cc.product_code = pc.product_code
         AND cc.product_issue_num = pc.product_issue_num
         AND cc.customer_id = p.customer_id
         AND cc.product_code = p.product_code
         AND cc.product_issue_num = p.product_issue_num
         AND p.product_code = pc.product_code
         AND p.product_issue_num = pc.product_issue_num
         AND p.product_status NOT IN ('E', 'C')
         AND cc.last_version_ind = 'Y'
         AND cc.customer_id = cust.customer_id
         AND cust.customer_id = p.customer_id
         AND cust.main_main_listing_id = l.listing_id(+)
         AND cust.derived_cust_ind = '0'
         AND l.last_version_ind(+) = 'Y'
         AND cc.assignment_id = asgn.assignment_id
      UNION ALL
      SELECT cust.customer_id, 0,
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
             (SELECT TO_DATE (issue_date, 'yyyy-mm-dd')
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
         AND p.regular_assign_id = asgn.assignment_id(+)