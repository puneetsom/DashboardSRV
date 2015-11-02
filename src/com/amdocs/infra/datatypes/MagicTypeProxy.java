/**
 * MagicTypeProxy - Copyright (C) Amdocs 2008-09
 * 
 * This object works in conjunction with MagicType to describe single property of the "Magical" object
 * 
 * @author Adi Rabinovich
 *
 */
package com.amdocs.infra.datatypes;

import java.util.List;

import org.omg.CORBA.PRIVATE_MEMBER;

import flex.messaging.io.PropertyProxy;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationDescriptor;

/**
 * @author bhryxx2
 *
 */
public class MagicTypeProxy implements PropertyProxy {

	MagicTypeProxy() {
		// Default constructor
	}
	
	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#createInstance(java.lang.String)
	 */
	public Object createInstance(String className) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getAlias()
	 */
	public String getAlias() {
		System.out.println("Fix BlazeDS!!! Can't know Alias without given Instance!");
		return null;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getAlias(java.lang.Object)
	 */
	public String getAlias(Object instance) {
		return ((MagicType) instance).getAlias();
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getDefaultInstance()
	 */
	public Object getDefaultInstance() {
		System.out.println("Fix BlazeDS!!! Can't Create Default Instance!!! No Such Thing!!!");
		return null;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getDescriptor()
	 */
	public SerializationDescriptor getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getIncludeReadOnly()
	 */
	public boolean getIncludeReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getInstanceToSerialize(java.lang.Object)
	 */
	public Object getInstanceToSerialize(Object instance) {
		return instance;
	}

	/**
	 * @see flex.messaging.io.PropertyProxy#getPropertyNames()
	 */
	@SuppressWarnings("unchecked")
	public List getPropertyNames() {
		System.out.println("Fix BlazeDS!!! Can't know propertyName without given Instance!");
		return null;
	}

	/**
	 * @see flex.messaging.io.PropertyProxy#getPropertyNames(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public List getPropertyNames(Object instance) 
	{
		return ((MagicType) instance).getPropertyNames();
	}

	/**
	 * @see flex.messaging.io.PropertyProxy#getSerializationContext()
	 */
	public SerializationContext getSerializationContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see flex.messaging.io.PropertyProxy#getType(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Class getType(String propertyName) {
		// We don't know what to do here, without instance we are clueless!!!
		System.out.println("Fix BlazeDS!!! Can't know propertyName without given Instance!");
		return null;
	}

	/**
	 * @see flex.messaging.io.PropertyProxy#getType(java.lang.Object, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Class getType(Object instance, String propertyName) {
		Object propValue = ((MagicType) instance).getPropertyValue(propertyName);
		return (propValue!=null) ? propValue.getClass() : String.class;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getValue(java.lang.String)
	 */
	public Object getValue(String propertyName) {
		// We don't know what to do here, without instance we are clueless!!!
		System.out.println("Fix BlazeDS!!! Can't know value of property without given Instance!");
		return null;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object instance, String propertyName) {
		return ((MagicType) instance).getPropertyValue(propertyName);
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#instanceComplete(java.lang.Object)
	 */
	public Object instanceComplete(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#isDynamic()
	 */
	public boolean isDynamic() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#isExternalizable()
	 */
	public boolean isExternalizable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#isExternalizable(java.lang.Object)
	 */
	public boolean isExternalizable(Object instance) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setAlias(java.lang.String)
	 */
	public void setAlias(String value) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setDefaultInstance(java.lang.Object)
	 */
	public void setDefaultInstance(Object defaultInstance) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setDescriptor(flex.messaging.io.SerializationDescriptor)
	 */
	public void setDescriptor(SerializationDescriptor descriptor) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setDynamic(boolean)
	 */
	public void setDynamic(boolean value) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setExternalizable(boolean)
	 */
	public void setExternalizable(boolean value) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setIncludeReadOnly(boolean)
	 */
	public void setIncludeReadOnly(boolean value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setSerializationContext(flex.messaging.io.SerializationContext)
	 */
	public void setSerializationContext(SerializationContext value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setValue(java.lang.String, java.lang.Object)
	 */
	public void setValue(String propertyName, Object value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see flex.messaging.io.PropertyProxy#setValue(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void setValue(Object instance, String propertyName, Object value) {
		// TODO Auto-generated method stub

	}
 
	public Object clone() {
		// TODO Auto-generated method stub
		return new MagicTypeProxy();		// Nothing to copy - this thing is a "template"
	}
}
