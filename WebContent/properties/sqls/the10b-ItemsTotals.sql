SELECT /*+ full(m) parallel(m,16) */ COUNT(1) count, SUM(item_nisd_amt) revenue
  FROM item i,
       item_imv iv,
       product p,
       customer cust,
       prd_curr_issue pc
 WHERE iv.customer_id = i.customer_id
   AND iv.product_code = i.product_code
   AND iv.product_issue_num = i.product_issue_num
   AND iv.item_id = i.item_id
   AND iv.item_version = i.item_version
   AND i.udac_code not in ('SRL', 'WRL')
   AND i.last_version_ind = 'Y'
   AND cust.derived_cust_ind = '0'   
   AND cust.customer_id = p.customer_id
   AND cust.customer_id = iv.customer_id
   AND p.customer_id = i.customer_id
   AND p.product_code = i.product_code
   AND p.product_issue_num = i.product_issue_num
   AND pc.product_code = p.product_code
   AND pc.product_issue_num = p.product_issue_num
