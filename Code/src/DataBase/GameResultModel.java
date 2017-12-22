package DataBase;

import javax.print.attribute.standard.RequestingUserName;

import org.omg.PortableServer.IdAssignmentPolicy;

public class GameResultModel extends DataBaseObject
{
	/**
	 * 记录ID
	 */
	public int Id = 0 ;
	/**
	 * 用户ID
	 */
	public int UserId = 0 ;
	/**
	 * 对手ID
	 */
	public int OpponentId = 0 ;
	/**
	 * 游戏结束时间
	 */
	public String FinishedTime = null ;
	/**
	 * 游戏开始时间
	 */
	public String BeginTime = null ;
	/**
	 * 是否胜利
	 */
	public boolean IsWin = false ;
	
	
	public GameResultModel(int id, int userId, int opponentId, String finishedTime, String beginTime,
			boolean isWin) 
	{
		super("GameResult");
		Id = id;
		UserId = userId;
		OpponentId = opponentId;
		FinishedTime = finishedTime;
		BeginTime = beginTime;
		IsWin = isWin;
	}
	
	public GameResultModel(int userId, int opponentId, String finishedTime, String beginTime,
			boolean isWin) 
	{
		super("GameResult");
		UserId = userId;
		OpponentId = opponentId;
		FinishedTime = finishedTime;
		BeginTime = beginTime;
		IsWin = isWin;
	}
	
	public GameResultModel() 
	{
		super("GameResult");
	}
	
	@Override
	public void SetMID(int id) 
	{
		Id = id ;
	}

	@Override
	public String GetMUpdateStatement() 
	{
		return null ;
	}

	@Override
	public String GetMAddStatement() 
	{
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append("INSERT INTO ") ;
		bulider.append(BelongTableName) ;
		bulider.append("(UserID,OpponentID,FinishedTime,BeginTime,IsWin)") ;
		bulider.append(" ") ;
		
		bulider.append("VALUES");
		bulider.append("(");
		bulider.append(UserId + ",") ;
		bulider.append(OpponentId+ ",") ;
		bulider.append("\'" + FinishedTime + "\',") ;
		bulider.append("\'" + BeginTime + "\',") ;
		
		if(IsWin)
		{
			bulider.append(1);
		}
		else
		{
			bulider.append(0);
		}
		
		bulider.append(");");
		
		return bulider.toString();
	}

	@Override
	public String GetMDeleteStatement()
	{
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append("DELETE FROM ") ;
		bulider.append(BelongTableName);
		bulider.append(" ") ;
		
		bulider.append("WHERE ID=");
		bulider.append(Id) ;
		bulider.append(";");
		
		return bulider.toString();
	}
	
}
