package DataBase;
import java.sql.Connection;  
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.sql.Statement;

import MainPack.ErrorRecordManager.ErrorRecord;
import MainPack.FiveChessMainCode; 

public class DataBaseManagerBase 
{
	//虽然Sqlite支持多线程连接读取，但是只支持单线程单连接写入
	//但是对同一个对象的读写却是在内部已经同步了
	//故而，本程序一个数据库只使用一个连接对象
	//无需再考虑数据库的线程同步问题
	private String DBConnectCommand = null;
	protected boolean IsSucceed = false ;
	protected Connection DBConnection = null;
	
	/**
	 * 
	 * @param filePath 数据库文件名
	 * @param createInsideTableStatement 当数据库文件不存在的时候创建数据库表的SQL语句
	 */
	public DataBaseManagerBase(String filePath , String[] createInsideTableStatements)
	{		
		//不可设定字符编码，或者我之前设定的字符编码方式是错的
		DBConnectCommand = "jdbc:sqlite:" + filePath;
		
		try
		{
			DBConnection = DriverManager.getConnection(DBConnectCommand) ;
			//创建语句
			Statement dBStatement = DBConnection.createStatement() ;
			
			//检查表是否存在，不存在则创建
			for (String  statement: createInsideTableStatements) 
			{
				//执行表创建语句，(if not exists)
				dBStatement.execute(statement);
			}
			
			//关闭语句连接
			dBStatement.close();

			IsSucceed = true ;
		}
		catch (Exception e) 
		{
			//初始化阶段直接输出错误
			System.out.println("Error Message:" + e.getMessage());
		}
	}
	
	/**
	 * 是否已经成功通过连接测试
	 * @return
	 */
	public boolean isIsSucceed() {
		return IsSucceed;
	}
		
	/**
	 * 增加一个记录
	 * 测试成功，2017.10.12，laoyao
	 * @param record 记录借口
	 * @return 是否添加成功
	 */
	protected boolean AddARecord(IDataBase record)
	{
		PreparedStatement dbStatement = null ;
		ResultSet resultSet = null ;
		
		try 
		{
			DBConnection = DriverManager.getConnection(DBConnectCommand) ;
			dbStatement = DBConnection.prepareStatement(record.GetMAddStatement() , 
					PreparedStatement.RETURN_GENERATED_KEYS) ;
			dbStatement.executeUpdate() ;
			resultSet = dbStatement.getGeneratedKeys();
			
			//如果有数据
			if(resultSet.next())
			{
				//设置记录的唯一ID
				record.SetMID(resultSet.getInt(1));
				
				resultSet.close();
				dbStatement.close();
				
				return true ;
			}
			else
			{
				resultSet.close();
				dbStatement.close();

				return false;
			}
		} 
		catch (Exception e) 
		{
			ErrorRecord errorRecord = new ErrorRecord(e.getMessage()) ;
			errorRecord.SetCodePostion(getClass().getName(), "AddARecord");
			
			FiveChessMainCode.ErrorRManager.AddRecord(errorRecord);
			
			return false ;
		}
	}
	
	/**
	 * 更新一个记录
	 * 测试成功，但是中文字符编码问题有待解决，2017.10,12，laoyao
	 * @param record 记录接口
	 * @return
	 */
	protected boolean UpDateARecord(IDataBase record)
	{
		Statement dbStatement = null ;
		int disposeCount = 0 ;
		
		try
		{
			dbStatement = DBConnection.createStatement();
			//执行SQL语句
			disposeCount = dbStatement.executeUpdate(record.GetMUpdateStatement()) ;
			
			//关闭连接
			dbStatement.close();
			
			if(disposeCount == 0)
			{
				return false ;
			}
			else
			{
				return true ;
			}
		}
		catch(Exception e)
		{
			ErrorRecord errorRecord = new ErrorRecord(e.getMessage()) ;
			errorRecord.SetCodePostion(getClass().getName(), "UpdataARecord");
			
			FiveChessMainCode.ErrorRManager.AddRecord(errorRecord);
			return false ;
		}
	}

	/**
	 * 删除一个记录
	 * @param record 记录借口
	 * @return
	 */
	protected boolean DeleteARecord(IDataBase record)
	{
		Statement dbStatement = null ;
		int disposeCount = 0 ;
		
		try
		{
			dbStatement = DBConnection.createStatement();
			//执行SQL语句
			disposeCount = dbStatement.executeUpdate(record.GetMDeleteStatement()) ;
			
			//关闭连接
			dbStatement.close();
			
			if(disposeCount == 0)
			{
				return false ;
			}
			else
			{
				return true ;
			}
		}
		catch(Exception e)
		{
			ErrorRecord errorRecord = new ErrorRecord(e.getMessage()) ;
			errorRecord.SetCodePostion(getClass().getName(), "DeleteARecord");
			
			FiveChessMainCode.ErrorRManager.AddRecord(errorRecord);
			return false ;
		}
	}
	
	public void Close()
	{
		try 
		{
			DBConnection.close();
		}
		catch (Exception e) 
		{
			// TODO: handle exception
		}
		
	}
}
