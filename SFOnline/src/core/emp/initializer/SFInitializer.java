package core.emp.initializer;

import java.util.HashMap;
import java.util.Map;

import module.bean.Param;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.component.xml.XMLDocumentLoader;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.web.servlet.Initializer;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheManagerService;
import core.cache.CacheMap;
import core.communication.util.AccessConstance;
import core.schedule.ContextData;
import core.schedule.ScheduleServer;

/**
 * SFInitializer.java<br>
 * SF Initializer component<br>
 * implements Initializer<br>
 * 
 */
public class SFInitializer implements Initializer {

	public void initialize(EMPFlowComponentFactory factory) throws Exception {
		String fbsSqlDefFile = null, sqlServiceId = null;
		KeyedCollection formatsKColl = null;
		Document fbsSqlDefDoc = null;
		Element element = null;
		NodeList nodeList = null;
		Object service = null;
		Context rootContext = null;
		/**
		 * ����SQL�����ļ�
		 */
		try{
			rootContext = factory.getContextNamed(factory.getRootContextName());
			String bizRootPath = factory.getRootPath();
			formatsKColl = (KeyedCollection)factory.getSettingElement("SF.files.sfSqlDefines");
			if(formatsKColl!=null){
				for (int i = 0; i < formatsKColl.size(); i ++){
					fbsSqlDefFile = (String)((DataField)formatsKColl.getDataElement(i)).getValue();
					fbsSqlDefDoc = loadXMLDocument( bizRootPath + "sqldefine/" + fbsSqlDefFile );
					element = fbsSqlDefDoc.getDocumentElement();
					nodeList = element.getChildNodes();
					for (int j = 0; j < nodeList.getLength(); j++) {
						Node node = nodeList.item(j);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							sqlServiceId = factory.getNodeAttributeValue("id", node);
							service = factory.getService(fbsSqlDefDoc, sqlServiceId);
							rootContext.addService(sqlServiceId, service);
						}
					}
				}
			}
			
			
			// ����root�ڵ��е�����Դ���ƺ�SQLִ�з�������
			rootContext.setDataValue(SFConst.SERVICE_FACTORY, factory.getSettingValue("SF.servicesname.factoryname"));
			rootContext.setDataValue(SFConst.SERVICE_DATASOURCE, factory.getSettingValue("SF.servicesname.datasourcename"));
			rootContext.setDataValue(SFConst.SERVICE_SQL, factory.getSettingValue("SF.servicesname.sqlexecservice"));
			rootContext.setDataValue(SFConst.SERVICE_GENCONTEXTBYSQL, factory.getSettingValue("SF.servicesname.gencontextbysql"));
			rootContext.setDataValue(SFConst.SERVICE_EXPRESSCALC, factory.getSettingValue("SF.servicesname.expresscalculate"));
			rootContext.setDataValue(SFConst.SERVICE_CACHEMANAGER, factory.getSettingValue("SF.servicesname.cacheManagerService"));
			rootContext.setDataValue(SFConst.SERVICE_FTP, factory.getSettingValue("SF.servicesname.ftpFileService"));
			rootContext.setDataValue(SFConst.SERVICE_PUBLICSERVICENAME, factory.getSettingValue("SF.servicesname.publicservice"));
	
			/*
			 * ����Ĭ��ϵͳ��������ֵ
			 */
			if(!rootContext.containsKey(SFConst.PUBLIC_TX_CODE)){
				rootContext.addDataElement(SFUtil.getTemplateKColl(rootContext, "PUBLIC"));
			}
			rootContext.setDataValue(SFConst.PUBLIC_TX_CODE, SFConst.DEFAULT_TRXCODE);
			rootContext.setDataValue(SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
			rootContext.setDataValue(SFConst.PUBLIC_IS_RET_RESP, SFConst.RET_RESP_YES);		
			//rootContext.addDataElement(PBankPublicFunction.getTemplateKColl(rootContext, "SYS_HEAD"));
			
			
			
			
			formatsKColl = (KeyedCollection)factory.getSettingElement("SF.ESBformats");
			XMLDocumentLoader loader = new XMLDocumentLoader();
			Document SFOnlineFormatsCoreDoc = loader.loadXMLDocument(factory.getRootPath() + "ESBFormats.xml");
			for (int i = 0; i < formatsKColl.size(); i ++){
				String formatId = (String)((DataField)formatsKColl.getDataElement(i)).getValue();
				EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.INFO, 0, "loading format [" + formatId + "]...", null);
				rootContext.addFormat(formatId, factory.getFormatElement(SFOnlineFormatsCoreDoc, formatId));
			}
			
			formatsKColl = (KeyedCollection)factory.getSettingElement("SF.Cobankformats");
			Document CobankFormatsCoreDoc = loader.loadXMLDocument(factory.getRootPath() + "CobankFormats.xml");
			for (int i = 0; i < formatsKColl.size(); i ++){
				String formatId = (String)((DataField)formatsKColl.getDataElement(i)).getValue();
				EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.INFO, 0, "loading format [" + formatId + "]...", null);
				rootContext.addFormat(formatId, factory.getFormatElement(CobankFormatsCoreDoc, formatId));
			}
			
			
			formatsKColl = (KeyedCollection)factory.getSettingElement("SF.ZLSecuformats");
			Document ZLSecuFormatsCoreDoc = loader.loadXMLDocument(factory.getRootPath() + "ZLSecuFormats.xml");
			for (int i = 0; i < formatsKColl.size(); i ++){
				String formatId = (String)((DataField)formatsKColl.getDataElement(i)).getValue();
				EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.INFO, 0, "loading format [" + formatId + "]...", null);
				rootContext.addFormat(formatId, factory.getFormatElement(ZLSecuFormatsCoreDoc, formatId));
			}
			
	
			
			formatsKColl = (KeyedCollection)factory.getSettingElement("SF.SZTSecuformats");
			Document STZSecuFormatsCoreDoc = loader.loadXMLDocument(factory.getRootPath() + "SZTSecuFormats.xml");
			for (int i = 0; i < formatsKColl.size(); i ++){
				String formatId = (String)((DataField)formatsKColl.getDataElement(i)).getValue();
				EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.INFO, 0, "loading format [" + formatId + "]...", null);
				rootContext.addFormat(formatId, factory.getFormatElement(STZSecuFormatsCoreDoc, formatId));
			}		
			rootContext.addDataField(SFConst.CTX_ERRCODE, "");
			rootContext.addDataField(SFConst.CTX_ERRMSG, "");
			
			
			//װ�ػ������������
			CacheManagerService  systemCache = ((CacheManagerService) rootContext.getService((String)rootContext.getDataValue(SFConst.SERVICE_CACHEMANAGER)));
			systemCache.init(rootContext);
			/*
			 * ��ʼ��ȫ��·��
			 */
			CacheMap.putCache(SFConst.GLOBEL_ROOT_PATH, factory.getRootPath());
			
			
			// ��������
			try {
				EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.INFO, 0, "begin start schedule..............", null);
				String currentAppName = "";
				currentAppName = SFConst.SYS_SYSNAME; //ReadProperty.getInstance().getProperties("THIS_APP_NAME");
				if(currentAppName != null && !"".equals(currentAppName.trim())){
					ScheduleServer.getInstance().startServer(new ContextData(rootContext));
				}
				
				EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.INFO, 0, "start schedule success..............", null);
			} catch (Exception e1) {
				EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.ERROR, 0, "start schedule error.............."+e1.getMessage(), e1);
			}
			/*
			 * ��ʼ������������
			 */
			CacheMap.putCache(SFConst.SYS_CACHE_TRAD,new HashMap<String,Integer>());//��ʼ��һ���յĽ���������
			Map<String,Map<String,Object>> flowCacheMap=new HashMap<String,Map<String,Object>>();
			flowCacheMap.put("COBANK_REQ_FLOW", new HashMap<String,Object>());
			flowCacheMap.put("COBANK_SEND_FLOW", new HashMap<String,Object>());
			flowCacheMap.put("SZTSECU_REQ_FLOW", new HashMap<String,Object>());
			flowCacheMap.put("ZLSECU_REQ_FLOW", new HashMap<String,Object>());
			CacheMap.putCache(SFConst.FLOW_CACHE_TRAD,flowCacheMap);//��ʼ��������������
		}catch(Exception e){
			EMPLog.log(AccessConstance.PBank_INITIALIZER, EMPLog.INFO, 0, "EMP Initializer error..."+e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * load XML document
	 * inner method��provide just for SFInitializer invoke��
	 * @param fileName
	 * @return Document XML object
	 * @throws Exception
	 */
	protected Document loadXMLDocument(String fileName) throws Exception
	{
		   XMLDocumentLoader loader = new XMLDocumentLoader();
		   Document document = loader.loadXMLDocument(fileName);
		   return document;
	}
}