package DataBase;

import javax.print.attribute.standard.RequestingUserName;

import org.omg.PortableServer.IdAssignmentPolicy;

public class GameResultModel extends DataBaseObject
{
	/**
	 * ��¼ID
	 */
	public int Id = 0 ;
	/**
	 * �û�ID
	 */
	public int UserId = 0 ;
	/**
	 * ����ID
	 */
	public int OpponentId = 0 ;
	/**
	 * ��Ϸ����ʱ��
	 */
	public String FinishedTime = null ;
	/**
	 * ��Ϸ��ʼʱ��
	 */
	public String BeginTime = null ;
	/**
	 * �Ƿ�ʤ��
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
