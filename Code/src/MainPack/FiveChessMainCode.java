package MainPack;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import DataBase.*;
import Net.Room;
import Net.User;
import jdk.internal.org.objectweb.asm.commons.StaticInitMerger;

public class FiveChessMainCode 
{
	/**
	 * 设定信息管理对象
	 */
	public static AppSetManager SetManager = null ;
	/**
	 * 用户数据库管理对象
	 */
	public static UserDataBaseManager UserDBManager = null;
	/**
	 * 游戏数据库管理对象
	 */
	public static GameDataBaseManager GameDBManager = null;
	/**
	 * 游戏错误信息管理器
	 */
	public static ErrorRecordManager ErrorRManager = null;
	/**
	 * 线程池
	 */
	public static ThreadPoolExecutor TPExecutor = null;
	/**
	 * 所有的在线用户
	 * 使用link list加快添加和删除速度
	 */
	public static List<User> TotalActiveUsers = new LinkedList<User>() ;
	/**
	 * 所有活跃的房间
	 */
	public static List<Room> TotalActiveRooms = new LinkedList<Room>();
	//服务端套接字
	private static ServerSocket SS = null;
	//程序是否关闭
	private static boolean IsAppClose = false ;
	
	//关闭APP命令，只要有 C >> CA 即可
	private final static String COMMAND_CLOSE_APP = "C>>CA" ;
	//查看所有用户命令
	private final static String COMMAND_SEE_AU = "C>>SAU" ;
	//查看所有房间命令
	private final static String COMMAND_SEE_AR = "C>>SAR" ;
	
	public static void main(String[] args) throws  ClassNotFoundException  
	{
		Scanner userInput = new Scanner(System.in) ; 
		String enterString = null;
				
		//加载jdbc驱动
		try 
		{
			// Load the sqlite-JDBC driver using the current class loader  
			Class.forName("org.sqlite.JDBC");
		}
		catch(ClassNotFoundException ex) 
		{
			System.out.println("Error: unable to load driver class!");
			WaitToCloseProgram(userInput) ;
		}
	
	    //初始化读取设定信息并且检查文件结构以及一些初始化
	    SetManager = new AppSetManager() ;
	    if(!SetManager.isIsSuccess())
	    {
	    	System.out.println("Error: Read App Init XML Data Failed!");
			WaitToCloseProgram(userInput) ;
	    }
	    else
	    {
	    	System.out.println("Read App Set Data Succeed!");
	    }
	    
	    //错误管理器初始化
	    ErrorRManager = new ErrorRecordManager(SetManager.getAppErrorRecordFileName()) ;
	    if(!ErrorRManager.isSucceed())
	    {
	    	System.out.println("Error: Init Error Reocrd File Failed!");
			WaitToCloseProgram(userInput) ;
	    }
	    else
	    {
	    	System.out.println("Init Error Reocrd File Succeed!");
	    }
	    
	    //初始化数据库管理对象
	    UserDBManager = new UserDataBaseManager(SetManager.getDataBaseDir() + SetManager.getUserDataBaseFileName());
	    if(!UserDBManager.isIsSucceed())
	    {
	    	System.out.println("Error: Test Connect To UserBase DataBase Failed!");
			WaitToCloseProgram(userInput) ;
	    }
	    else
	    {
	    	System.out.println("Test Connect To UserBase DataBase Succeed!");
	    }
	    
	    GameDBManager = new GameDataBaseManager(SetManager.getDataBaseDir() + SetManager.getGameDataBaseFileName());
	    if(!GameDBManager.isIsSucceed())
	    {
	    	System.out.println("Error: Test Connect To Game DataBase Failed!");
			WaitToCloseProgram(userInput) ;
	    }
	    else
	    {
	    	System.out.println("Test Connect To Game DataBase Succeed!");
	    }
	    
	    //初始化线程池，等待队列无限大
	    TPExecutor = new ThreadPoolExecutor(SetManager.getCoreThreadNumber() , SetManager.getMaxThreadNumber() , 
	    		SetManager.getTKeepAliveTimeS() , TimeUnit.SECONDS , new LinkedBlockingQueue<Runnable>());
	    
	    try
	    {
	    	SS = new ServerSocket(SetManager.getPortNumber(), SetManager.getMaxSSQueneNumber()) ;
	    }
	    catch (Exception e) 
	    {
	    	System.out.println("Error Create Server Socket Failed!");
			WaitToCloseProgram(userInput) ;
		}
	    
	    TPExecutor.execute(new Runnable() {
			
			@Override
			public void run()
			{
				while(!IsAppClose)
				{
					try
					{					
						Socket connection = SS.accept() ;

						
						ObjectOutputStream oStream = new ObjectOutputStream(connection.getOutputStream()) ;
						ObjectInputStream iStream = new ObjectInputStream(connection.getInputStream()) ;
						
						//添加用户
						TotalActiveUsers.add(new User(connection, iStream, oStream)) ;
						
					}
					catch (Exception e) 
					{
						
					}
					
				}
			}
		});
	    
	    //初始化工作结束
	    System.out.println("--------------------------------------------");
	    
	    //用户界面死循环
	    while(true)
	    {
	    	String[] modifyToStruct = null;
	    	
	    	enterString = userInput.nextLine() ;
	    	
	    	modifyToStruct = enterString.split(">>") ;
	    	
	    	if(modifyToStruct.length != 2)
	    	{
	    		System.out.println("Warning : Can Not Analysis The Input!");
	    		continue;
	    	}
	    	
	    	for(int counter = 0 ; counter < modifyToStruct.length ; counter++)
	    	{
	    		modifyToStruct[counter] = modifyToStruct[counter].trim();
	    	}
	    	
	    	enterString = modifyToStruct[0] + ">>" + modifyToStruct[1] ;
	    	
	    	//关闭程序命令，直接跳出循环，结束程序
	    	if(enterString.equals(COMMAND_CLOSE_APP))
	    	{
	    		break;
	    	}
	    	//查看所有用户
	    	else if(enterString.equals(COMMAND_SEE_AU))
	    	{
	    		System.out.println("----------All User Information----------------");
	    		
	    		for(int counter = 0 ; counter < TotalActiveUsers.size() ; counter++)
	    		{
	    			System.out.println(TotalActiveUsers.get(counter).GetSDisplayInformation());
	    		}
	    		
	    		System.out.println("----------------------------------------------");
	    	}
	    	//查看所有房间
	    	else if(enterString.equals(COMMAND_SEE_AR))
	    	{
	    		System.out.println("----------All Room Information----------------");
	    		
	    		for(int counter = 0 ; counter < TotalActiveRooms.size() ; counter++)
	    		{
	    			System.out.println(TotalActiveRooms.get(counter).GetSDisplayInformation());
	    		}
	    		
	    		System.out.println("----------------------------------------------");
	    	}
	    }
	    
	    //关闭服务器套接字,客户端连接全部自动关闭
		try 
		{
			SS.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    //标识为程序关闭
	    IsAppClose = true ;
	    
	    //关闭两个数据库的连接
	    UserDBManager.Close();
	    GameDBManager.Close();
	    
	    //关闭所有用户连接
	    for(int counter = 0 ; counter < TotalActiveUsers.size();  counter++)
	    {
	    	TotalActiveUsers.get(counter).Close();
	    }
	    
	    //关闭线程池
	    TPExecutor.shutdown();
	}

	private static void WaitToCloseProgram(Scanner input)
	{
		System.out.println("Enter Any Key To Close");
		input.nextLine() ;
		input.close();
		System.exit(1);
	}
}
