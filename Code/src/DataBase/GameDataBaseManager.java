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
	 * ����һ����Ϸ�����¼
	 * @param record ��¼
	 * @return
	 */
	public boolean AddAGameResultRecord(GameResultModel record)
	{
		return super.AddARecord(record) ;
	}
	
	/**
	 * ������еĶ�ս��¼��ֻ�ж����û�ID��û�ж����û���Ϣ
	 * @param userId ��Ҫ��ѯ���û�ID
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
