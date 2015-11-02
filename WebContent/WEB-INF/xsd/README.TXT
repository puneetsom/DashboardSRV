Brian
1/15/2013
Generate JAXB java files using commands below
 
Go to DashboardSRV/WebContent/WEB-INF/xsd in command prompt and run these commands for each XSD that changed:

   xjc Assets.xsd -b bindings.xjb -d ..\..\..\src -p com.amdocs.dashboard.mobile.assets
   xjc Reports.xsd -b bindings.xjb -d ..\..\..\src -p com.amdocs.dashboard.mobile.reports
   xjc Report.xsd -b bindings.xjb -d ..\..\..\src -p com.amdocs.dashboard.mobile.report
   xjc KpiReport.xsd -b bindings.xjb -d ..\..\..\src -p com.amdocs.dashboard.mobile.kpiReport
   xjc CustomDataGrid.xsd -b bindings.xjb -d ..\..\..\src -p com.amdocs.dashboard.mobile.grid
   xjc XYChart.xsd -b bindings.xjb -d ..\..\..\src -p com.amdocs.dashboard.mobile.xyChart

Generated java files should NOT be modified.

Should recreate java files whenever XSD file changes

TODO: Define process in ant script.
TODO: Remove generated timestamp from java file comments (http://stackoverflow.com/questions/5093514/jaxb-xjc-possible-to-suppress-comment-creation-in-generated-classes)