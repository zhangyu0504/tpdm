package core.communication.access.webservice;

import java.util.Map;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.datatype.DataType;



/**
 * SOAP包描述类。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-26
 * @lastmodified 2008-6-11
 *
 */
public class PBankSOAPPackage {

	/**
	 * EMP数据类型定义
	 */
	private Map dataTypeDefs;
	
	/**
	 * 从SOAPMessage中取得Session id。
	 * 
	 * @param msg SOAPMessage
	 * @param sessionIdField 存放SessionId的域的名称
	 * @return Session id
	 * @deprecated 方法名和实际作用不相符
	 * @throws Exception
	 */
	public String getSessionId(SOAPMessage msg, String sessionIdField) throws Exception	{
		
		return getFieldValue(msg, sessionIdField);
	}

	/**
	 * 从SOAPMessage中取得某个请求域的值。
	 * 
	 * @param msg SOAPMessage
	 * @param field 请求域的名称
	 * @return 请求域的值
	 * @throws Exception
	 */
	public String getFieldValue(SOAPMessage msg, String field) throws Exception	{
		
		SOAPBody body = msg.getSOAPPart().getEnvelope().getBody();
		SOAPElement element = (SOAPElement)body.getChildElements().next();
		element =(SOAPElement)element.getElementsByTagName(field).item(0);
		return element.getValue();
	}
	
	/**
	 * 从SOAPMessage中取得请求的Operation id。
	 * 
	 * @param msg SOAPMessage
	 * @return 请求的Operation id
	 * @throws Exception
	 */
	public String getOperationId(SOAPMessage msg)throws Exception {
		
		SOAPBody body = msg.getSOAPPart().getEnvelope().getBody();
		SOAPElement element = (SOAPElement)body.getChildElements().next();
		String opId = element.getNodeName();
		int idx = opId.indexOf(':');
		if( idx != -1 )
			opId = opId.substring( idx+1);
		return opId;
	}
	
	/**
	 * 将请求数据更新到Context。
	 * <p>
	 * 从SOAPMessage中取得请求数据，然后更新到Context中，
	 * 同时自动根据数据类型定义检验数据。
	 * 
	 * @param msg SOAPMessage
	 * @param context Context
	 * @param dataDef input定义
	 * @param operationId Operation id
	 * @throws Exception
	 */
	public void updateContext(SOAPMessage msg, Context context,
			KeyedCollection dataDef, String operationId) throws Exception {
		
		SOAPBody body = msg.getSOAPPart().getEnvelope().getBody();
		
		//the operation element
		SOAPElement element = (SOAPElement)body.getChildElements().next();
		
//		//the operation body element
//		element = (SOAPElement)element.getElementsByTagName("body").item(0);
		
		//the input element
		element = (SOAPElement)element.getChildElements().next();
		context.setDataValue("REQUEST", element.getTextContent());
		
		/*for( int i=0; i<dataDef.size(); i++ )
		{
			DataElement dataElement = dataDef.getDataElement( i );
			if( dataElement instanceof DataField )
			{
				DataField dataField = (DataField)dataElement;
				String name = dataElement.getName();
				NodeList childs = element.getElementsByTagName(name );
				if( childs == null || childs.getLength() == 0 )
				{
					if( !dataField.isRequired() )
						continue;
					else
						throw new EMPException("Required field [" + name + "] not set!");
				}
				
				Node aElement = childs.item(0);
				String value = aElement.getChildNodes().item(0).getNodeValue();
				
				String dataType = ((DataField)dataElement).getDataType();
				if( dataType != null )
				{
					DataType dataTypeDef = (DataType)this.dataTypeDefs.get( dataType );
					Object objValue = dataTypeDef.convertFromString(value, null );
					context.setDataValue(name, objValue );
				}
				else
					context.setDataValue(name, value );
			}
			else if( dataElement instanceof IndexedCollection )
			{
				String name = dataElement.getName();
				Element aElement = (Element)element.getElementsByTagName( operationId+name).item( 0 );
				IndexedCollection iColl = (IndexedCollection)context.getDataElement( name );
				DataElement iCollElement = iColl.getDataElement();
				String itemName = operationId+name + "ItemRef";
				NodeList nodes = aElement.getElementsByTagName( itemName );
				for( int k=0; k<nodes.getLength(); k++)
				{
					KeyedCollection dst = (KeyedCollection)iCollElement.clone();
					iColl.addDataElement( dst );
					IndexedCollection defIColl = (IndexedCollection)dataElement;
					updateKeyedCollection( (Element)nodes.item( k ), dst, (KeyedCollection)defIColl.getDataElement(),operationId);
				}
				
			}
		}*/
	}
	
	/**
	 * 将请求数据更新到KeyedCollection(递归方法)。
	 * <p>
	 * 从SOAPMessage的指定数据节点取得请求数据，然后更新到KeyedCollection中，
	 * 同时自动根据数据类型定义检验数据。
	 * 
	 * @param element 请求数据XML节点
	 * @param dst 目标KeyedCollection
	 * @param def input定义
	 * @param operationId Operation id
	 * @throws Exception
	 */
	public void updateKeyedCollection(Element element, KeyedCollection dst,
			KeyedCollection def, String operationId) throws Exception {
		
		for( int i=0; i<def.size(); i++ )
		{
			DataElement dataElement = def.getDataElement( i );
			if( dataElement instanceof DataField )
			{
				DataField dataField = (DataField)dataElement;
				String name = dataElement.getName();
				NodeList childs = element.getElementsByTagName(name );
				if( childs == null || childs.getLength() == 0 )
				{
					if( !dataField.isRequired() )
						continue;
					else
						throw new EMPException("Required field [" + name + "] not set!");
				}
				
				Node aElement = childs.item(0);
//				String name = dataElement.getName();
//				Node aElement = element.getElementsByTagName(name ).item(0);
				String value = aElement.getChildNodes().item(0).getNodeValue();

				String dataType = ((DataField)dataElement).getDataType();
				if( dataType != null )
				{
					DataType dataTypeDef = (DataType)this.dataTypeDefs.get( dataType );
					Object objValue = dataTypeDef.convertFromString(value, null );
					dst.setDataValue(name, objValue );
				}
				else
					dst.setDataValue(name, value );
				
			//	dst.setDataValue(name, value );
			}
			else if( dataElement instanceof IndexedCollection )
			{
				String name = dataElement.getName();
				Element aElement = (Element)element.getElementsByTagName( operationId+name).item( 0 );
				IndexedCollection iColl = (IndexedCollection)dst.getDataElement( name );
				DataElement iCollElement = iColl.getDataElement();
				String itemName = operationId+name + "ItemRef";
				NodeList nodes = aElement.getElementsByTagName( itemName );
				for( int k=0; k<nodes.getLength(); k++)
				{
					KeyedCollection adst = (KeyedCollection)iCollElement.clone();
					iColl.addDataElement( adst );
					IndexedCollection defIColl = (IndexedCollection)dataElement;
					updateKeyedCollection( (Element)nodes.item( k ), adst, (KeyedCollection)defIColl.getDataElement(),operationId);
				}
				
			}
		}
	}
	
	/**
	 * 将Context中的数据更新到响应数据。
	 * <p>
	 * 从Context中取得数据，然后更新到SOAPMessage中，
	 * 同时自动根据数据类型定义转换数据，以组织SOAP报文
	 * 
	 * @param msg SOAPMessage
	 * @param context Context
	 * @param dataDef output定义
	 * @param operationId Operation id
	 * @throws Exception
	 */
	public void updateSoapMessage(SOAPMessage msg, Context context,
			KeyedCollection dataDef, String operationId) throws Exception {
		
//		msg.getSOAPPart().getEnvelope().addNamespaceDeclaration("q0", "testEMPLogic.xsd");
		SOAPBody body = msg.getSOAPPart().getEnvelope().getBody();

		//operation output element
//		body.addNamespaceDeclaration("ns", "http://service.fbs.sdb.com");
		SOAPElement element = (SOAPElement)body.addChildElement(operationId + "Response", "ns", "http://service.fbs.sdb.com");
//		soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"

//		xsi:type="q0:signOnInputType"

//		element.setAttribute("xsi:type", "q0:" + operationId + "OutputType" );

//		element.setAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/" );
			
		element = element.addChildElement("return", "ns");
		element.setTextContent((String)context.getDataValue("RESPONSE"));
		/*for( int i=0; i<dataDef.size(); i++ )
		{
			DataElement dataElement = dataDef.getDataElement( i );
			if( dataElement instanceof DataField )
			{
				String name = dataElement.getName();
				Object value = context.getDataValue(name);
				if( value == null )
					continue;
				
				SOAPElement aElement = element.addChildElement( name, "");
				aElement.setValue( value.toString() );
				
			}
			else if( dataElement instanceof IndexedCollection )
			{
				String name = dataElement.getName();
				IndexedCollection iColl = (IndexedCollection)context.getDataElement( name );

				DataElement iCollElement = iColl.getDataElement();
				String itemName = operationId+name + "Item";

				SOAPElement aElement = element.addChildElement( operationId+name, "");
				
				for( int k=0; k<iColl.size(); k++)
				{
					SOAPElement kElement = aElement.addChildElement( itemName+"Ref" , "");
				
					kElement.setAttribute("xsi:type", "q0:" + itemName );
					KeyedCollection kColl = (KeyedCollection)iColl.get( k );
					updateSoapMessage(kElement, kColl, (KeyedCollection)iCollElement ,operationId);
				}
				
			}
		}		*/
	}

	/**
	 * 将KeyedCollection数据更新到响应数据(递归方法)。
	 * <p>
	 * 从KeyedCollection中取得数据，然后更新到SOAPMessage的指定数据节点中，
	 * 同时自动根据数据类型定义转换数据，以组织SOAP报文
	 * 
	 * @param element 响应数据定义XML节点
	 * @param kColl 来源KeyedCollection
	 * @param dataDef output定义
	 * @param operationId Operation id
	 * @throws Exception
	 */
	public void updateSoapMessage(SOAPElement element, KeyedCollection kColl,
			KeyedCollection dataDef, String operationId) throws Exception {
		
		for( int i=0; i<dataDef.size(); i++ )
		{
			DataElement dataElement = dataDef.getDataElement( i );
			if( dataElement instanceof DataField )
			{
				String name = dataElement.getName();
				
				String value = "";
				try{
					value = kColl.getDataValue(name).toString();
				}catch(Exception ee){}
				SOAPElement aElement = element.addChildElement( name, "" );
				aElement.setValue( value );
				
			}
			else if( dataElement instanceof IndexedCollection )
			{
				String name = dataElement.getName();
				IndexedCollection iColl = (IndexedCollection)kColl.getDataElement( name );

				DataElement iCollElement = iColl.getDataElement();
				String itemName =operationId+ name + "Item";

				SOAPElement aElement = element.addChildElement( operationId+name, "" );

				for( int k=0; k<iColl.size(); k++)
				{
					SOAPElement kElement = aElement.addChildElement( itemName+"Ref", "" );
					
					kElement.setAttribute("xsi:type", "q0:" + itemName );
					KeyedCollection akColl = (KeyedCollection)iColl.get( k );
					updateSoapMessage(kElement, akColl, (KeyedCollection)iCollElement,operationId );
				}
				
			}
		}		
	}

	/**
	 * 注入EMP数据类型定义。
	 * 
	 * @param dataTypeDefs 数据类型定义
	 */
	public void setDataTypeDefs(Map dataTypeDefs) {
		this.dataTypeDefs = dataTypeDefs;
	}
}
