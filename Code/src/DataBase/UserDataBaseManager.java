package DataBase;
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jdk.management.resource.internal.TotalResourceContext; 


public class UserDataBaseManager extends DataBaseManagerBase
{
	//�����������Գɹ� �� 2017.10.11
	private static final String CreateUserBaseTableSqlStatement = 
			"CREATE TABLE IF NOT EXISTS UserBase"
			+ "("
			+ "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ "Name VARCHAR(50) NOT NULL UNIQUE,"
			+ "Money INTEGER NOT NULL DEFAULT 0,"
			+ "TotalGameTimes INTEGER NOT NULL DEFAULT 0,"
			+ "WinTimes INTEGER NOT NULL DEFAULT 0,"
			+ "LastEnterTime VARCHAR(15) NULL,"
			+ "IconID INTEGER NOT NULL DEFAULT 0,"
			+ "MachineID VARCHAR(40) NOT NULL"
			+ ");";
			
	private static final String GoodsAndUserTableSqlStatement = 
			"CREATE TABLE IF NOT EXISTS GoodsAndUser"
			+ "("
			+ "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ "GoodsID INTEGER NOT NULL,"
			+ "UserID INTEGER NOT NULL,"
			+ "Number INTEGER NOT NULL"
			+ ");" ;
	
	public UserDataBaseManager(String filePath)
	{
		super(filePath, new String[] {CreateUserBaseTableSqlStatement , GoodsAndUserTableSqlStatement}) ;
	}
	
	/**
	 * ���һ���û�������Ϣ��¼
	 * @param record �û�������Ϣ
	 * @return �Ƿ�ɹ�
	 */
	public boolean AddAUserBaseRecord(UserBaseModel record) 
	{
		return super.AddARecord(record) ;
	}
	
	/**
	 * ���һ���û���Ʒ��¼
	 * @param record
	 * @return
	 */
	public boolean AddAUserAndGoodsRecord(UserAndGoodsModel record)
	{
		return super.AddARecord(record) ;
	}
	
	/**
	 * ����һ���û�������Ϣ��¼
	 * @param record �û�������Ϣ
	 * @return �Ƿ�ɹ�
	 */
	public boolean UpDateAUserBaseRecord(UserBaseModel record)
	{
		return super.UpDateARecord(record) ;
	}
	
	/**
	 * ����һ���û������¼
	 * @param record
	 * @return
	 */
	public boolean UpDataAUserAndGoodsRecord(UserAndGoodsModel record)
	{
		return super.UpDateARecord(record) ;
	}
	
	/**
	 * ɾ��һ���û��ļ�¼
	 * @param record
	 * @return
	 */
	public boolean DeleteAUserBaseRecord(UserBaseModel record)
	{
		return super.DeleteARecord(record) ;
	}
	
	/**
	 * ɾ��һ�������û���Ӧ��Ϣ
	 * @param record
	 * @return
	 */
	public boolean DeleteAUerAndGoodsRecord(UserAndGoodsModel record)
	{
		return super.DeleteARecord(record) ;
	}
	
	/**
	 * ʹ�����ֲ�ѯһ���û��Ļ�����Ϣ
	 * test success,2017.10.15
	 * @param name �û�������
	 * @return
	 */
	public UserBaseModel UseNameToFindUser(String name)
	{
		Statement dbStatement = null ;
		ResultSet result = null;
		UserBaseModel user = new UserBaseModel();
		boolean isGet = false ;
		
		try
		{
			dbStatement = DBConnection.createStatement() ;
			
			result = dbStatement.executeQuery("SELECT * FROM UserBase WHERE Name=" + "\'" + name + "\'") ;
			
			while(result.next())
			{
				user.Id = result.getInt("ID") ;
				user.Name = result.getString("Name") ;
				user.Money = result.getInt("Money") ;
				user.TotalGameTimes = result.getInt("TotalGameTimes") ;
				user.WinTimes = result.getInt("WinTimes") ;
				user.LastEnterTime = result.getString("LastEnterTime") ;
				user.IconID = result.getInt("IconID") ;
				user.MachineID = result.getString("MachineID") ;
				
				isGet = true ;
			}
			
			result.close();
			dbStatement.close();
			
			if(isGet)
			{
				return user ;
			}
			else
			{
				return null ;
			}
		}
		catch (Exception e) 
		{
			return null ;
		}
	}
	
	/**
	 * ʹ���û�ID��ѯһ���û��Ļ�����Ϣ
	 * @param id �û���id
	 * @return
	 */
	public UserBaseModel UseIDToFindUser(int id)
	{
		Statement dbStatement = null ;
		ResultSet result = null;
		UserBaseModel user = new UserBaseModel();
		boolean isGet = false ;
		
		try
		{
			dbStatement = DBConnection.createStatement() ;
			
			result = dbStatement.executeQuery("SELECT * FROM UserBase WHERE ID=" + "\'" + id + "\'") ;
			
			while(result.next())
			{
				user.Id = result.getInt("ID") ;
				user.Name = result.getString("Name") ;
				user.Money = result.getInt("Money") ;
				user.TotalGameTimes = result.getInt("TotalGameTimes") ;
				user.WinTimes = result.getInt("WinTimes") ;
				user.LastEnterTime = result.getString("LastEnterTime") ;
				user.IconID = result.getInt("IconID") ;
				user.MachineID = result.getString("MachineID") ;
				
				isGet = true ;
			}
			
			result.close();
			dbStatement.close();
			
			if(isGet)
			{
				return user ;
			}
			else
			{
				return null ;
			}
		}
		catch (Exception e) 
		{
			return null ;
		}
	}
	
	public List<Integer> GetAllUserHadGoods(int userId)
	{
		Statement dbStatement = null ;
		ResultSet result = null;
		List<Integer> total = new ArrayList<>() ;
		
		try
		{
			dbStatement = DBConnection.createStatement() ;
			
			result = dbStatement.executeQuery("SELECT GoodsID FROM GoodsAndUser WHERE UserId=" + userId) ;
			
			while(result.next())
			{
				total.add(result.getInt("GoodsID")) ;
			}
			
			result.close();
			dbStatement.close();
			
			return total ;
		}
		catch (Exception e) 
		{
			return null ;
		}
	}
}
