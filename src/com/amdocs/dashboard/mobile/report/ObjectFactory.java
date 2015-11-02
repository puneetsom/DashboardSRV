//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.05.30 at 03:53:34 PM CDT 
//


package com.amdocs.dashboard.mobile.report;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.amdocs.dashboard.mobile.report package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Report_QNAME = new QName("http://www.amdocs.com/dashboard/mobile/report", "Report");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.amdocs.dashboard.mobile.report
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ServiceParam }
     * 
     */
    public ServiceParam createServiceParam() {
        return new ServiceParam();
    }

    /**
     * Create an instance of {@link ServiceElement }
     * 
     */
    public ServiceElement createServiceElement() {
        return new ServiceElement();
    }

    /**
     * Create an instance of {@link MetadataElement }
     * 
     */
    public MetadataElement createMetadataElement() {
        return new MetadataElement();
    }

    /**
     * Create an instance of {@link ReportElement }
     * 
     */
    public ReportElement createReportElement() {
        return new ReportElement();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReportElement }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.amdocs.com/dashboard/mobile/report", name = "Report")
    public JAXBElement<ReportElement> createReport(ReportElement value) {
        return new JAXBElement<ReportElement>(_Report_QNAME, ReportElement.class, null, value);
    }

}
