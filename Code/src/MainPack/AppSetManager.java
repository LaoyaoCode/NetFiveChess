package MainPack;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.ParserConfigurationException; 
import javax.xml.transform.OutputKeys; 
import javax.xml.transform.Transformer; 
import javax.xml.transform.TransformerConfigurationException; 
import javax.xml.transform.TransformerException; 
import javax.xml.transform.TransformerFactory; 
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import org.w3c.dom.Document; 
import org.w3c.dom.Element; 
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList;
import NetObjModel.*;

public class AppSetManager 
{
	private String RootDir = null;
	
	//Ӧ�ó��������ļ�·��
	public final static String AppSetFilePath = "AppSetData\\Set.xml" ;
	//chess and icon xml file path
	public final static String AppChessAndIconFilePath = "AppSetData\\ChessAndIcon.xml";
	
	/**
	 * ���е�����ͼƬ��Ϣ , ֻ�� �� ���迼���߳�ͬ��
	 */
	public List<ChessInformationM> AllChessImage = new LinkedList<>() ;
	/**
	 * ���е�ͷ��ͼƬ��Ϣ, ֻ�� �� ���迼���߳�ͬ��
	 */
	public List<ChessOrIconImage> AllIconImage = new LinkedList<>() ;
	
	//���ݿ��ļ��洢��Ŀ¼�������ļ��ָ���
	private String DataBaseDir = null ;
	private final static String DataBaseDirNodeName = "DataBaseDir" ;
	
	//�̳߳غ����߳���Ŀ
	private int CoreThreadNumber = 0 ;
	private final static String CoreThreadNumberNodeName = "CoreThreadNumber" ;
	//�̳߳�����߳���Ŀ
	private int MaxThreadNumber = 0 ;
	private final static String MaxThreadNumberNodeName = "MaxThreadNumber" ;
	//�û����ݿ��ļ���
	private String UserDataBaseFileName = null;
	private final static String UserDataBaseFileNameNodeName = "UserDataBaseFileName" ;
	//��Ϸ���ݿ��ļ���
	private String GameDataBaseFileName = null;
	private final static String GameDataBaseFileNameNodeName = "GameDataBaseFileName";
	//��������¼�ļ���
	private String AppErrorRecordFileName = null;
	private final static String AppErrorRecordFileNameNodeName="AppErrorRecordFileName" ;
	//��������������
	private int ChessTableRC = 0;
	private final static String ChessTableRCNodeName="ChessTableRC";
	//�߳��ܹ����ֵ����ʱ��
	private int TKeepAliveTimeS = 0;
	private final static String TKeepAliveTimeSNodeName="TKeepAliveTimeS";
	//�˿���
	private int PortNumber = 0 ;
	private static final String PortNumberNodeName="PortNumber" ;
	//������׽������ȴ���Ŀ
	private int MaxSSQueneNumber = 0 ;
	private static final String MaxSSQueneNumberNodeName = "MaxSSQueneNumber" ;
	//һ����ϷӮ�õĽ�Ǯ��Ŀ
	private int AGameWM = 0 ;
	private static final String AGameWMNodeName = "AGameWM" ;
	//�ܹ����ò�����Ŀ
	private static final int TOTALSETNUMBER = 11 ;
	
	//�Ƿ��ȡ���ݲ��Ҽ���ʼ���ɹ�
	private boolean IsSuccess = true ;
	
	public static String GetRootDir()
	{
		return Thread.currentThread().getContextClassLoader().getResource("").getPath();
	}
	
	public  AppSetManager() 
	{
		RootDir = GetRootDir() ;
		
		if(!ReadSetXmlData())
		{
			IsSuccess = false ;
			return ;
		}
		
		if(!ReadChessAndIconXMLData())
		{
			IsSuccess = false ;
			return ;
		}
		if(!CheckTheDir())
		{
			IsSuccess = false ;
			return ;
		}
		
	}
	
	/*
	 * ��ȡ xml �����ļ��е�����
	 * test success , 2017.10.10
	 */
	private boolean ReadSetXmlData()
	{
		int getSetNumber = 0 ;
		
		try 
		{ 
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document document = db.parse(RootDir + AppSetFilePath); 
			NodeList rootNode = document.getChildNodes(); 
			
			if(rootNode.getLength() != 1)
			{
				PrintALine(AppSetFilePath + "--Data Broken Or Lost");
				return false;
			}
			else
			{
				NodeList sets = rootNode.item(0).getChildNodes() ;
				
				//��������ȡ�������ýڵ���Ϣ
				for(int counter = 0 ; counter < sets.getLength() ; counter++)
				{
					if(sets.item(counter).getNodeName().equals(CoreThreadNumberNodeName))
					{
						try
						{
							CoreThreadNumber = Integer.parseInt(sets.item(counter).getTextContent()) ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(MaxThreadNumberNodeName))
					{
						try
						{
							MaxThreadNumber = Integer.parseInt(sets.item(counter).getTextContent()) ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(DataBaseDirNodeName))
					{
						try
						{
							//�����ļ��ָ���
							DataBaseDir = sets.item(counter).getTextContent() + "\\";
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(UserDataBaseFileNameNodeName))
					{
						try
						{
							UserDataBaseFileName = sets.item(counter).getTextContent() ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(GameDataBaseFileNameNodeName))
					{
						try
						{
							GameDataBaseFileName = sets.item(counter).getTextContent() ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(AppErrorRecordFileNameNodeName))
					{
						try
						{
							AppErrorRecordFileName = sets.item(counter).getTextContent() ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(ChessTableRCNodeName))
					{
						try
						{
							ChessTableRC = Integer.parseInt(sets.item(counter).getTextContent()) ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(TKeepAliveTimeSNodeName))
					{
						try
						{
							TKeepAliveTimeS = Integer.parseInt(sets.item(counter).getTextContent()) ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(PortNumberNodeName))
					{
						try
						{
							PortNumber = Integer.parseInt(sets.item(counter).getTextContent()) ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(MaxSSQueneNumberNodeName))
					{
						try
						{
							MaxSSQueneNumber = Integer.parseInt(sets.item(counter).getTextContent()) ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
					else if(sets.item(counter).getNodeName().equals(AGameWMNodeName))
					{
						try
						{
							AGameWM = Integer.parseInt(sets.item(counter).getTextContent()) ;
							getSetNumber++;
						}
						catch(Exception e)
						{
							PrintALine(AppSetFilePath + "--Data Broken");
							return false;
						}
					}
				}
				
				if(getSetNumber == TOTALSETNUMBER)
				{
					//��ȷ�Ļ�ȡ����������
					return true ;
				}
				else
				{
					PrintALine("Set Data Lost Or Broken!") ;
					return false ;
				}
				
			}
		}
		catch(Exception e)
		{
			PrintALine(e.getMessage());
			return false ;
		}
	}

	/*
	 * ��ȡ xml ���Ӻ�ͷ���趨�ļ��е�����
	 * test success , 2017.10.15
	 */
	private boolean ReadChessAndIconXMLData()
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document document = db.parse(RootDir + AppChessAndIconFilePath); 
			NodeList rootNode = document.getChildNodes(); 
			
			if(rootNode.getLength() != 1)
			{
				PrintALine(AppChessAndIconFilePath + "--Data Broken Or Lost");
				return false;
			}
			else
			{
				NodeList chessAndIcon = rootNode.item(0).getChildNodes() ;
				
				for(int counterO = 0 ; counterO < chessAndIcon.getLength() ; counterO++)
				{
					if(chessAndIcon.item(counterO).getNodeName().equals("Chess")) 
					{
						NodeList chesss = chessAndIcon.item(counterO).getChildNodes();
						
						for(int counter = 0 ; counter < chesss.getLength();counter++)
						{
							if(chesss.item(counter).getNodeName().equals("Data"))
							{
								NodeList datas = chesss.item(counter).getChildNodes() ;
								int getDatasNumber = 0 ;
								int id = 0 ; 
								String name = null ;
								String des = null ;
								int price = 0 ;
								
								for(int counterI = 0 ; counterI < datas.getLength() ; counterI++)
								{
									if(datas.item(counterI).getNodeName().equals("ID"))
									{
										try
										{
											id = Integer.parseInt(datas.item(counterI).getTextContent()) ;
											getDatasNumber++ ;
										}
										catch (Exception e) 
										{
											e.printStackTrace();
											return false ;
										}
									}
									else if(datas.item(counterI).getNodeName().equals("Name"))
									{
										try 
										{
											name = datas.item(counterI).getTextContent() ;
											getDatasNumber++;
										} catch (Exception e) 
										{
											e.printStackTrace();
											return false ;
										}
									}
									else if(datas.item(counterI).getNodeName().equals("Des"))
									{
										try 
										{
											des = datas.item(counterI).getTextContent() ;
											getDatasNumber++;
										} catch (Exception e) 
										{
											e.printStackTrace();
											return false ;
										}
									}
									else if(datas.item(counterI).getNodeName().equals("Price"))
									{
										try 
										{
											price =Integer.parseInt(datas.item(counterI).getTextContent())  ;
											getDatasNumber++;
										} catch (Exception e) 
										{
											e.printStackTrace();
											return false ;
										}
									}
								}
								
								if(getDatasNumber != 4)
								{
									return false ;
								}
								
								AllChessImage.add(new ChessInformationM(id, name, des , price)) ;
							}
						}
					}
					else if(chessAndIcon.item(counterO).getNodeName().equals("Icon")) 
					{
						NodeList icons = chessAndIcon.item(counterO).getChildNodes();
						
						for(int counter = 0 ; counter < icons.getLength();counter++)
						{
							if(icons.item(counter).getNodeName().equals("Data"))
							{
								NodeList datas = icons.item(counter).getChildNodes() ;
								int getDatasNumber = 0 ;
								int id = 0 ; 
								String name = null ;
								String des = null ;
								
								for(int counterI = 0 ; counterI < datas.getLength() ; counterI++)
								{
									if(datas.item(counterI).getNodeName().equals("ID"))
									{
										try
										{
											id = Integer.parseInt(datas.item(counterI).getTextContent()) ;
											getDatasNumber++ ;
										}
										catch (Exception e) 
										{
											e.printStackTrace();
											return false ;
										}
									}
									else if(datas.item(counterI).getNodeName().equals("Name"))
									{
										try 
										{
											name = datas.item(counterI).getTextContent() ;
											getDatasNumber++;
										} catch (Exception e) 
										{
											e.printStackTrace();
											return false ;
										}
									}
									else if(datas.item(counterI).getNodeName().equals("Des"))
									{
										try 
										{
											des = datas.item(counterI).getTextContent() ;
											getDatasNumber++;
										} catch (Exception e) 
										{
											e.printStackTrace();
											return false ;
										}
									}
								}
								
								if(getDatasNumber != 3)
								{
									return false ;
								}
								
								AllIconImage.add(new ChessOrIconImage(id, name, des)) ;
							}
						}
					}
				}
				
				
			}
			
			return true ;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return false ;
		}
	}
	
	private boolean CheckTheDir()
	{
		File dataBaseDir = new File(RootDir + DataBaseDir) ;
		
		if(!dataBaseDir.exists())
		{
			if(!dataBaseDir.mkdirs())
			{
				return false ;
			}
		}
		
		
		return true ;
	}
	
	/**
	 * ����˿���
	 * @return
	 */
	public int getPortNumber() {
		return PortNumber;
	}

	/**
	 * һ����ϷӮ�õĽ�Ǯ��
	 * @return
	 */
	public int getAGameWM() {
		return AGameWM;
	}

	/**
	 * ����׽��ֵȴ���
	 * @return
	 */
	public int getMaxSSQueneNumber() {
		return MaxSSQueneNumber;
	}

	/**
	 * �߳��ܹ����ֵ�������ʱ�䣨��SΪ��λ��
	 * @return
	 */
	public int getTKeepAliveTimeS() {
		return TKeepAliveTimeS;
	}

	/**
	 * ��ȡ��������������
	 * @return
	 */
	public int getChessTableRC() {
		return ChessTableRC;
	}

	/**
	 * ��ȡ��������¼�ļ���
	 * @return
	 */
	public String getAppErrorRecordFileName() {
		return RootDir + AppErrorRecordFileName;
	}
	
	/**
	 * ��ȡ�û����ݿ��ļ���
	 * @return
	 */
	public String getUserDataBaseFileName() {
		return UserDataBaseFileName;
	}

	/**
	 * ��ȡ��Ϸ���ݿ��ļ���
	 * @return
	 */
	public String getGameDataBaseFileName() {
		return GameDataBaseFileName;
	}
	
	/**
	 * ��ȡ���ݿ��ļ���Ŀ¼������Ѿ��������ļ��ָ���
	 * @return
	 */
	public String getDataBaseDir() {
		return RootDir + DataBaseDir;
	}

	/**
	 * ��ȡ�̳߳غ����߳���
	 * @return
	 */
	public int getCoreThreadNumber() {
		return CoreThreadNumber;
	}

	/**
	 * ��ȡ�̳߳�����߳���
	 * @return
	 */
	public int getMaxThreadNumber() {
		return MaxThreadNumber;
	}

	/**
	 * �����ʼ���Ƿ�ɹ�
	 * @return �ɹ�����ʧ��
	 */
	public boolean isIsSuccess() {
		return IsSuccess;
	}

	//ֻ�����ڳ�ʼ������������ַ�������̨
	private static void PrintALine(String words)
	{
		System.out.println(words);
	}

	public String getRootDir() {
		return RootDir;
	}
	
	

}
