package DataBase;
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import NetObjModel.GameResultInformation; 

public class GameDataBaseManager extends DataBaseManagerBase
{
	private static final String CreateGameResultTableSqlStatement=
			"CREATE TABLE IF NOT EXISTS GameResult"
			+ "("
			+ "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ "UserID INTEGER NOT NULL,"
			+ "OpponentID INTEGER NOT NULL,"
			+ "FinishedTime VARCHAR(40) NOT NULL,"
			+ "BeginTime VARCHAR(40) NOT NULL,"
			+ "IsWin INTEGER NOT NULL"
			+ ");" ;
	
	public GameDataBaseManager(String filePath)
	{
		super(filePath, new String[] {CreateGameResultTableSqlStatement}) ;
	}
	
	/**
	 * 增加一个游戏结果记录
	 * @param record 记录
	 * @return
	 */
	public boolean AddAGameResultRecord(GameResultModel record)
	{
		return super.AddARecord(record) ;
	}
	
	/**
	 * 获得所有的对战记录，只有对手用户ID，没有对手用户信息
	 * @param userId 需要查询的用户ID
	 * @return
	 */
	public List<GameResultInformation> GetTotalResult(int userId)
	{
		Statement dbStatement = null ;
		ResultSet result = null;
		List<GameResultInformation> total = new ArrayList<>() ;
		int id  ;
		String finishedTime , beginTime ;
		boolean isWin = false ;
		
		try
		{
			dbStatement = DBConnection.createStatement() ;
			
			result = dbStatement.executeQuery("SELECT OpponentID , FinishedTime , BeginTime , IsWin"
					+ " FROM GameResult WHERE UserID=" + userId) ;
			
			while(result.next())
			{
				id = result.getInt("OpponentID") ;
				finishedTime = result.getString("FinishedTime") ;
				beginTime = result.getString("BeginTime") ;
				
				if(result.getInt("IsWin") == 1)
				{
					isWin = true ;
				}
				else
				{
					isWin = false ;
				}
				
				total.add(new GameResultInformation(id, null, finishedTime, beginTime, isWin)) ;
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
