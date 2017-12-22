package DataBase;

import java.io.Serializable;
import NetObjModel.*;


public class UserBaseModel extends DataBaseObject implements Serializable
{
	/**
	 * 用户在表中唯一的ID
	 */
	public int Id = 0 ;
	/**
	 * 用户名字
	 */
	public String Name = null;
	/**
	 * 用户金钱数
	 */
	public int Money = 0;
	/**
	 * 用户总共游戏次数
	 */
	public int TotalGameTimes = 0;
	/**
	 * 用户赢得的次数
	 */
	public int WinTimes = 0;
	/**
	 * 最后登录的时间
	 */
	public String LastEnterTime = null;
	/**
	 * 头像的ID，默认为0
	 */
	public int IconID = 0;
	/**
	 * 用户的机器ID
	 */
	public String MachineID = null;
	public UserBaseModel(int id, String name, int money, 
			int totalGameTimes, int winTimes,
			String lastEnterTime,int iconID , String machineID) 
	{
		super("UserBase");
		Id = id;
		Name = name;
		Money = money;
		TotalGameTimes = totalGameTimes;
		WinTimes = winTimes;
		LastEnterTime = lastEnterTime;
		IconID = iconID;
		MachineID = machineID ;
	}
	
	public UserBaseModel(String name, int money, 
			int totalGameTimes, int winTimes,
			String lastEnterTime , int iconID, String machineID) 
	{
		super("UserBase");
		Name = name;
		Money = money;
		TotalGameTimes = totalGameTimes;
		WinTimes = winTimes;
		LastEnterTime = lastEnterTime;
		IconID = iconID;
		MachineID = machineID ;
	}

	public UserBaseModel() 
	{
		//设置从属的表名
		super("UserBase") ;
	}

	@Override
	public void SetMID(int id) 
	{
		Id = id ;
	}

	@Override
	public String GetMUpdateStatement() 
	{
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append("UPDATE ");
		bulider.append(BelongTableName) ;
		bulider.append(" ") ;
		
		bulider.append("SET ");
		bulider.append("Name=" + "\'" + Name + "\'"+ ",") ;
		bulider.append("Money=" + Money + ",") ;
		bulider.append("TotalGameTimes=" + TotalGameTimes + ",") ;
		bulider.append("WinTimes=" + WinTimes + ",");
		bulider.append("LastEnterTime=" + "\'" + LastEnterTime + "\',") ;
		bulider.append("IconID=" + IconID + ",");
		bulider.append("MachineID=" + "\'" + MachineID + "\'") ;
		bulider.append(" ") ;
		
		bulider.append("WHERE ID=" + Id + ";") ;
		return bulider.toString();
	}

	@Override
	public String GetMAddStatement() 
	{
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append("INSERT INTO ") ;
		bulider.append(BelongTableName) ;
		bulider.append("(Name,Money,TotalGameTimes,WinTimes,LastEnterTime,IconID,MachineID)") ;
		bulider.append(" ") ;
		
		bulider.append("VALUES");
		bulider.append("(");
		bulider.append("\'" + Name + "\'" + ",") ;
		bulider.append(Money+ ",") ;
		bulider.append(TotalGameTimes+ ",") ;
		bulider.append(WinTimes+ ",") ;
		bulider.append("\'" + LastEnterTime + "\',");
		bulider.append(IconID + ",");
		bulider.append("\'" + MachineID + "\'");
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

	@Override
	public String toString()
	{
		StringBuilder bulider = new StringBuilder();
		
		bulider.append(String.format("%-15s = %-10s\n", "Id" , Id)) ;
		bulider.append(String.format("%-15s = %-10s\n", "Name" , Name)) ;
		bulider.append(String.format("%-15s = %-10s\n", "Money" , Money)) ;
		bulider.append(String.format("%-15s = %-10s\n", "TotalGameTimes" , TotalGameTimes)) ;
		bulider.append(String.format("%-15s = %-10s\n", "WinTimes" , WinTimes)) ;
		bulider.append(String.format("%-15s = %-10s\n", "LastEnterTime" , LastEnterTime)) ;
		bulider.append(String.format("%-15s = %-10s\n", "IconID" , IconID)) ;
		bulider.append(String.format("%-15s = %-40s\n", "MachineID" , MachineID)) ;
		return bulider.toString();
	}
	
	public static UserBaseInformation GetSendInstance(UserBaseModel model)
	{
		return new UserBaseInformation(model.Id ,model.Name , model.Money, 
				model.TotalGameTimes , model.WinTimes , model.LastEnterTime , model.IconID) ;
	}
}
