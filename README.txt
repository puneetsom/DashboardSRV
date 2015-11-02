
(1)  Init param in web.xml points to quartz.properties file:

    <init-param>
    	<param-name>config-file</param-name>
    	<param-value>c:\Matt\Projects\DashboardSRV\WebContent\properties\quartz.properties</param-value>
    </init-param>



(2)  Added the properties dir to the CLASSPATH for loading jobs.xml file.


(3)  Defined property amdocs.dir for properties dir.

	-Damdocs.dir=c:\Matt\Projects\DashboardSRV\WebContent\properties


    TODO: should probabl change it to:   amdocs.dashboard.dir
    

(4)  jobs.xml - set location of all the Output XML files.


(5)  server.xml - remove limit on POST size.

     <Connector> element, add an attribute "maxPostSize" and set to 0 to disable the size check.


(6)  There are Business Objects processes that copy files to the "data" directory on the server. They use the mech id:

     Username: itsdo\m65958
     Password: $Dash135
 
 
    Where is the DATA directory?  Create an application property?
 
 
 (7) Create usage tables in the EA database. Sql's are in the setup directory.
 
 
 
 NOTE: Having problems with classes12.jar on the Production server - maybe because it's 64-bit??
 
 
  