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
	
	//应用程序设置文件路径
	public final static String AppSetFilePath = "AppSetData\\Set.xml" ;
	//chess and icon xml file path
	public final static String AppChessAndIconFilePath = "AppSetData\\ChessAndIcon.xml";
	
	/**
	 * 所有的棋子图片信息 , 只读 ， 无需考虑线程同步
	 */
	public List<ChessInformationM> AllChessImage = new LinkedList<>() ;
	/**
	 * 所有的头像图片信息, 只读 ， 无需考虑线程同步
	 */
	public List<ChessOrIconImage> AllIconImage = new LinkedList<>() ;
	
	//数据库文件存储根目录，附带文件分隔符
	private String DataBaseDir = null ;
	private final static String DataBaseDirNodeName = "DataBaseDir" ;
	
	//线程池核心线程数目
	private int CoreThreadNumber = 0 ;
	private final static String CoreThreadNumberNodeName = "CoreThreadNumber" ;
	//线程池最大线程数目
	private int MaxThreadNumber = 0 ;
	private final static String MaxThreadNumberNodeName = "MaxThreadNumber" ;
	//用户数据库文件名
	private String UserDataBaseFileName = null;
	private final static String UserDataBaseFileNameNodeName = "UserDataBaseFileName" ;
	//游戏数据库文件名
	private String GameDataBaseFileName = null;
	private final static String GameDataBaseFileNameNodeName = "GameDataBaseFileName";
	//程序错误记录文件名
	private String AppErrorRecordFileName = null;
	private final static String AppErrorRecordFileNameNodeName="AppErrorRecordFileName" ;
	//棋盘行列棋子数
	private int ChessTableRC = 0;
	private final static String ChessTableRCNodeName="ChessTableRC";
	//线程能够保持的最大时间
	private int TKeepAliveTimeS = 0;
	private final static String TKeepAliveTimeSNodeName="TKeepAliveTimeS";
	//端口数
	private int PortNumber = 0 ;
	private static final String PortNumberNodeName="PortNumber" ;
	//服务端套接字最大等待数目
	private int MaxSSQueneNumber = 0 ;
	private static final String MaxSSQueneNumberNodeName = "MaxSSQueneNumber" ;
	//一场游戏赢得的金钱数目
	private int AGameWM = 0 ;
	private static final String AGameWMNodeName = "AGameWM" ;
	//总共设置参数数目
	private static final int TOTALSETNUMBER = 11 ;
	
	//是否获取数据并且检查初始化成功
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
	 * 获取 xml 设置文件中的数据
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
				
				//遍历读获取所有设置节点信息
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
							//附带文件分隔符
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
					//正确的获取了所有数据
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
	 * 获取 xml 棋子和头像设定文件中的数据
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
	 * 程序端口数
	 * @return
	 */
	public int getPortNumber() {
		return PortNumber;
	}

	/**
	 * 一次游戏赢得的金钱数
	 * @return
	 */
	public int getAGameWM() {
		return AGameWM;
	}

	/**
	 * 最大套接字等待数
	 * @return
	 */
	public int getMaxSSQueneNumber() {
		return MaxSSQueneNumber;
	}

	/**
	 * 线程能够保持的最大空闲时间（以S为单位）
	 * @return
	 */
	public int getTKeepAliveTimeS() {
		return TKeepAliveTimeS;
	}

	/**
	 * 获取棋盘行列棋子数
	 * @return
	 */
	public int getChessTableRC() {
		return ChessTableRC;
	}

	/**
	 * 获取程序错误记录文件名
	 * @return
	 */
	public String getAppErrorRecordFileName() {
		return RootDir + AppErrorRecordFileName;
	}
	
	/**
	 * 获取用户数据库文件名
	 * @return
	 */
	public String getUserDataBaseFileName() {
		return UserDataBaseFileName;
	}

	/**
	 * 获取游戏数据库文件名
	 * @return
	 */
	public String getGameDataBaseFileName() {
		return GameDataBaseFileName;
	}
	
	/**
	 * 获取数据库文件根目录，最后已经附带了文件分隔符
	 * @return
	 */
	public String getDataBaseDir() {
		return RootDir + DataBaseDir;
	}

	/**
	 * 获取线程池核心线程数
	 * @return
	 */
	public int getCoreThreadNumber() {
		return CoreThreadNumber;
	}

	/**
	 * 获取线程池最大线程数
	 * @return
	 */
	public int getMaxThreadNumber() {
		return MaxThreadNumber;
	}

	/**
	 * 程序初始化是否成功
	 * @return 成功或者失败
	 */
	public boolean isIsSuccess() {
		return IsSuccess;
	}

	//只允许在初始化过程中输出字符到控制台
	private static void PrintALine(String words)
	{
		System.out.println(words);
	}

	public String getRootDir() {
		return RootDir;
	}
	
	

}
