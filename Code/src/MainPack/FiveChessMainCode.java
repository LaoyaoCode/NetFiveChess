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
	 * �趨��Ϣ�������
	 */
	public static AppSetManager SetManager = null ;
	/**
	 * �û����ݿ�������
	 */
	public static UserDataBaseManager UserDBManager = null;
	/**
	 * ��Ϸ���ݿ�������
	 */
	public static GameDataBaseManager GameDBManager = null;
	/**
	 * ��Ϸ������Ϣ������
	 */
	public static ErrorRecordManager ErrorRManager = null;
	/**
	 * �̳߳�
	 */
	public static ThreadPoolExecutor TPExecutor = null;
	/**
	 * ���е������û�
	 * ʹ��link list�ӿ���Ӻ�ɾ���ٶ�
	 */
	public static List<User> TotalActiveUsers = new LinkedList<User>() ;
	/**
	 * ���л�Ծ�ķ���
	 */
	public static List<Room> TotalActiveRooms = new LinkedList<Room>();
	//������׽���
	private static ServerSocket SS = null;
	//�����Ƿ�ر�
	private static boolean IsAppClose = false ;
	
	//�ر�APP���ֻҪ�� C >> CA ����
	private final static String COMMAND_CLOSE_APP = "C>>CA" ;
	//�鿴�����û�����
	private final static String COMMAND_SEE_AU = "C>>SAU" ;
	//�鿴���з�������
	private final static String COMMAND_SEE_AR = "C>>SAR" ;
	
	public static void main(String[] args) throws  ClassNotFoundException  
	{
		Scanner userInput = new Scanner(System.in) ; 
		String enterString = null;
				
		//����jdbc����
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
	
	    //��ʼ����ȡ�趨��Ϣ���Ҽ���ļ��ṹ�Լ�һЩ��ʼ��
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
	    
	    //�����������ʼ��
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
	    
	    //��ʼ�����ݿ�������
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
	    
	    //��ʼ���̳߳أ��ȴ��������޴�
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
						
						//����û�
						TotalActiveUsers.add(new User(connection, iStream, oStream)) ;
						
					}
					catch (Exception e) 
					{
						
					}
					
				}
			}
		});
	    
	    //��ʼ����������
	    System.out.println("--------------------------------------------");
	    
	    //�û�������ѭ��
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
	    	
	    	//�رճ������ֱ������ѭ������������
	    	if(enterString.equals(COMMAND_CLOSE_APP))
	    	{
	    		break;
	    	}
	    	//�鿴�����û�
	    	else if(enterString.equals(COMMAND_SEE_AU))
	    	{
	    		System.out.println("----------All User Information----------------");
	    		
	    		for(int counter = 0 ; counter < TotalActiveUsers.size() ; counter++)
	    		{
	    			System.out.println(TotalActiveUsers.get(counter).GetSDisplayInformation());
	    		}
	    		
	    		System.out.println("----------------------------------------------");
	    	}
	    	//�鿴���з���
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
	    
	    //�رշ������׽���,�ͻ�������ȫ���Զ��ر�
		try 
		{
			SS.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    //��ʶΪ����ر�
	    IsAppClose = true ;
	    
	    //�ر��������ݿ������
	    UserDBManager.Close();
	    GameDBManager.Close();
	    
	    //�ر������û�����
	    for(int counter = 0 ; counter < TotalActiveUsers.size();  counter++)
	    {
	    	TotalActiveUsers.get(counter).Close();
	    }
	    
	    //�ر��̳߳�
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
