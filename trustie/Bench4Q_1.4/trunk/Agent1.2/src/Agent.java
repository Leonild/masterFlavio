package src;

import java.io.*;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.jface.dialogs.DialogSettings;

import src.EBClosed;
import src.EB;
import src.storage.*;
import src.communication.Args;

public class Agent extends Thread {

	/*
	 * ���ԵĿ�ʼʱ��
	 */
	public long m_startTime;
	/*
	 * ���ԵĽ���ʱ��
	 */
	public long m_endTime;
	/*
	 * ����ʱ��
	 */
	public long m_stdyTime;
	/*
	 * ���μ��ص�EB����
	 */
	public int m_baseLoad;
	/*
	 * �⻧����
	 */
	public String m_tenant;
	/*
	 * ׼��ʱ��
	 */
	public int m_prepairTime;
	/*
	 * ��ȴʱ��
	 */
	public int m_cooldown;
	/*
	 * ���Ӧ�õĸ���ַ
	 */
	public String m_baseURL;
	/*
	 * mix���ͣ�shopping��browsing�ȣ�
	 */
	public String m_mix;
	/*
	 * �Ƿ�ʹ�ýű�������
	 */
	public boolean m_script;
	/*
	 * �ű��ļ���·��
	 */
	public String m_scriptPath;
	
	/*
	 * ��Ź��ò�����args���ʵ��
	 */
	public Args m_args;
	/*
	 * ������ɵ�EB
	 */
	private ArrayList<EB> ebs;
	
	/*
	 * ��Ŵ洢����
	 */
	private long m_stoPERIOD=60000;
	/*
	 * ��Ÿ����ӳ�
	 */
	private long m_stoDELAY=30000;
	/*
	 * think time
	 */
	private double m_thinkTime;
	/**
	 * ���캯��
	 * @param configpath �����ļ���·��
	 * @param scriptpath �ű��ļ���·��
	 */
	public Agent(String configpath,String scriptpath) throws Exception
	{
		m_args=new Args();
		
		/*
		 * ���������ļ����ݽ��г�ʼ��
		 */
		
		DialogSettings settings=new DialogSettings(null);
		try
		{
			settings.load(configpath);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		m_stdyTime=Long.valueOf(settings.get("stdyTime")).longValue();
		m_baseLoad=Integer.valueOf(settings.get("baseLoad")).intValue();
		m_tenant=settings.get("tenant");
		m_prepairTime = Integer.valueOf(settings.get("prepairTime")).intValue();
		m_cooldown = Integer.valueOf(settings.get("cooldown")).intValue();
		m_mix=settings.get("mix");
		m_stoPERIOD = Long.valueOf(settings.get("STOperiod")).longValue();
		m_stoDELAY = Long.valueOf(settings.get("STOdelay")).longValue();
		if(settings.get("isScript").equals("true"))
		{
			m_script=true;
		}
		else
		{
			m_script=false;
			m_baseURL=settings.get("baseURL");
			m_thinkTime=Double.valueOf(settings.get("thinkTime")).doubleValue();
		}
		m_scriptPath=scriptpath;
		
		Init();
	}
	
	public void Init() throws Exception
	{
		
		/*
		 * Init Args
		 */
		m_args.setMix(m_mix);
		m_args.setBaseURL(m_baseURL);
		m_args.setPrepair(m_prepairTime);
		m_args.setCooldown(m_cooldown);
		m_args.setTenant(m_tenant);
		m_args.setScript(m_script);
		m_args.setThinktime(m_thinkTime);
		
		if(m_args.isScript()){
			InitScript(m_scriptPath);
		}
		HttpClientFactory.setRetryCount(m_args.getRetry());
		
		/*
		 * Init EBs
		 */
		ArrayList<Integer> trace=new ArrayList<Integer>();
		
		ebs = new ArrayList<EB>();
		
		//m_baseLoad: define the number of thread
		for(int i=0;i<m_baseLoad;i++)
		{
			if(m_args.isScript()==true)
			{
				EBClosed eb=new EBClosed(m_args,m_scriptPath,i);
				eb.start();
				ebs.add(eb);
			}
			else
			{
				EBClosed eb=new EBClosed(m_args,trace);
				eb.start();
				ebs.add(eb);
			}
			
		}
	}
	/**
	 * ���ű�������Ϣ����m_args
	 * @param scriptpath �ű��ļ���·��
	 * @throws Exception 
	 */
	public void InitScript(String scriptpath) throws Exception
	{
		ArrayList<String> scriptURL=new ArrayList<String>();
		ArrayList<String> scriptPage=new ArrayList<String>();
		ArrayList<String> scriptParam=new ArrayList<String>();
		//scriptState: �±��ʾ��ǰ������script�е���ţ�ֵ��ʾ����������ҳ����script�е����
		ArrayList<Integer> scriptState=new ArrayList<Integer>();
		ArrayList<String> scriptUser=new ArrayList<String>();
		ArrayList<String> scriptPass=new ArrayList<String>();
		ArrayList<String> scriptType=new ArrayList<String>();
		//added at 20110901, for recorded sleep time from script
		ArrayList<String> scriptSleep=new ArrayList<String>();
		
		File xmlFile = new File(scriptpath);   
        try {   
        	FileInputStream fis = null;   
            fis = new FileInputStream(xmlFile);   
            fis.close();
        } catch (FileNotFoundException e) {   
            e.printStackTrace();
            System.err.println("File is not exsit!");   
        }   
		SAXReader xmlReader = new SAXReader();
		xmlReader.setEncoding("GBK");
		try {
		    Document doc = xmlReader.read(xmlFile);
		    Element root = doc.getRootElement();
		    Element behaviors=root.element("behaviors");
		    List behaviorList = behaviors.elements("behavior" );
		    for (Iterator it_behavior = behaviorList.iterator(); it_behavior.hasNext();) {
		        Element behavior = (Element) it_behavior.next();
		        List sampleList = behavior.elements("sample" );
		        List sleepList = behavior.elements("timer");
		        //consider that the first request of script haven't duration time, add one here for consistency
		        scriptSleep.add("1");
			    for(Iterator it_sample = sampleList.iterator(),
			    		it_sleep = sleepList.iterator(); it_sample.hasNext();) {
			    	String parameter="";
			    	String username="";
			    	String password="";
			    	String url="";
			    	String sleepTime="";
			    	
			    	Element sample = (Element) it_sample.next();
			    	if(it_sample.hasNext()) {
			    		Element sleep = (Element) it_sleep.next();
			    		Element sParams = sleep.element("params");
			    		Element sParam = sParams.element("param");
			    		if(sParam.attribute("name").getValue().equals("duration_arg")) {
			    			Attribute param_value = sParam.attribute("value");
			    			sleepTime = param_value.getValue();
			    		} else {
			    			System.out.println("FATAL ERROR[101]: Script format error.");
			    			System.exit(-1);
			    		}
			    	}
			        Element params=sample.element("params");
			        List paramList = params.elements("param" );
			        for(Iterator it_param=paramList.iterator();it_param.hasNext();) {
			        	Element param = (Element) it_param.next();
			        	Attribute param_name=param.attribute("name" ); 
			        	if(param_name.getValue().equals("parameters"))
			        	{
			        		Attribute param_value=param.attribute("value" ); 
			        		parameter=param_value.getValue();
			        	}
			        	if(param_name.getValue().equals("redirect"))
			        	{
			        		Attribute param_value=param.attribute("value" ); 
			        		username=param_value.getValue();
			        	}
			        	if(param_name.getValue().equals("password"))
			        	{
			        		Attribute param_value=param.attribute("value" ); 
			        		password=param_value.getValue();
			        	}
			        	if(param_name.getValue().equals("uri")){
			        		Attribute param_value=param.attribute("value" ); 
			        		url=param_value.getValue();
			        		//����ַ��׺��һ��4���е�һ�֣���scriptType��Ӧλ�ô�"others",������ͨurl��ַ,��"page"
			        		if(url.substring(url.length()-3).equalsIgnoreCase(".js")
			        				||url.substring(url.length()-4).equalsIgnoreCase(".css")
			        				||url.substring(url.length()-4).equalsIgnoreCase(".jpg")
			        				||url.substring(url.length()-4).equalsIgnoreCase(".gif")
			        				||url.substring(url.length()-4).equalsIgnoreCase(".png"))
			        		{
			        			scriptType.add("others");
			        		}			        			
			        		else
			        		{			        			
			        			scriptType.add("page");
			        			//temp: ��scriptPage���Ѿ����˸�url��ַ�������
			        			if(!scriptPage.contains(url))
			        				scriptPage.add(url);
			        		}
			        		scriptURL.add(url);
			        		scriptParam.add(parameter);
					        scriptUser.add(username);
					        scriptPass.add(password);
					        scriptSleep.add(sleepTime);
			        		//System.out.println(url);
			        	}
			        }
			   }  
		    }
		}
		catch(Exception e){
		  	e.printStackTrace();
		  	throw e;
		}
		int prevstate=0;
		for(int i=0;i<scriptURL.size();i++)
		{
			if(scriptType.get(i).equals("page"))
			{
				int j=scriptPage.indexOf(scriptURL.get(i));
				scriptState.add(j);
				prevstate=j;
			}
			if(scriptType.get(i).equals("others"))
			{
				//����.js, .jpg, .css ��β���󣬾����Ϊ������ҳ���state����prevstate.
				scriptState.add(prevstate);
			}
		}
		//����scriptPage��scriptState��ʼ��������תģ��
		m_args.getStatJump().init(scriptPage, scriptState);
		
		m_args.setScriptURL(scriptURL);
		m_args.setscriptParam(scriptParam);
		m_args.setscriptState(scriptState);
		m_args.setscriptUser(scriptUser);
		m_args.setscriptPass(scriptPass);
		m_args.setscriptType(scriptType);
		m_args.setscriptPage(scriptPage);
		m_args.setscriptSleep(scriptSleep);
	}
	public void InitStorage(){
		long startTime = System.currentTimeMillis();
		m_startTime=startTime;
		if(m_args.isScript()){
			int scriptSize = m_args.getscriptURL().size();
			int scriptSize_SS = m_args.getscriptPage().size();
			StorageThread.initSto(m_stoPERIOD,m_stoDELAY,scriptSize, scriptSize_SS, m_startTime, m_stdyTime,m_baseLoad);
		}else{
			StorageThread.initSto(m_stoPERIOD,m_stoDELAY,15, 15,m_startTime, m_stdyTime,m_baseLoad);
		}
	}
	public static void main(String[] args) throws Exception{
		//Agent agent=new Agent(args[0],args[1]);
		Agent agent=new Agent("config.xml","Script.xml");
		agent.InitStorage();
		StorageThread.getUniq().start();
		agent.start();
		System.out.println("started...");
	}
	
	public void run()
	{
		long beginTime = System.currentTimeMillis();
		long endTime = beginTime + m_stdyTime * 1000L;

		for (EB eb : ebs) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			((EBClosed) eb).setTest(true);
		}

		while ((System.currentTimeMillis() - endTime) < 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		for (EB eb : ebs) {
			((EBClosed) eb).setTerminate(true);
			((EBClosed) eb).interrupt();
		}
		ebs = null;
	}
	

}

