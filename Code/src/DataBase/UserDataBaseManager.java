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
	//创建表语句测试成功 ， 2017.10.11
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
	 * 添加一个用户基本信息记录
	 * @param record 用户基本信息
	 * @return 是否成功
	 */
	public boolean AddAUserBaseRecord(UserBaseModel record) 
	{
		return super.AddARecord(record) ;
	}
	
	/**
	 * 添加一个用户物品记录
	 * @param record
	 * @return
	 */
	public boolean AddAUserAndGoodsRecord(UserAndGoodsModel record)
	{
		return super.AddARecord(record) ;
	}
	
	/**
	 * 更新一个用户基本信息记录
	 * @param record 用户基本信息
	 * @return 是否成功
	 */
	public boolean UpDateAUserBaseRecord(UserBaseModel record)
	{
		return super.UpDateARecord(record) ;
	}
	
	/**
	 * 更新一个用户货物记录
	 * @param record
	 * @return
	 */
	public boolean UpDataAUserAndGoodsRecord(UserAndGoodsModel record)
	{
		return super.UpDateARecord(record) ;
	}
	
	/**
	 * 删除一个用户的记录
	 * @param record
	 * @return
	 */
	public boolean DeleteAUserBaseRecord(UserBaseModel record)
	{
		return super.DeleteARecord(record) ;
	}
	
	/**
	 * 删除一个货物用户对应信息
	 * @param record
	 * @return
	 */
	public boolean DeleteAUerAndGoodsRecord(UserAndGoodsModel record)
	{
		return super.DeleteARecord(record) ;
	}
	
	/**
	 * 使用名字查询一个用户的基本信息
	 * test success,2017.10.15
	 * @param name 用户的名字
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
	 * 使用用户ID查询一个用户的基本信息
	 * @param id 用户的id
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
