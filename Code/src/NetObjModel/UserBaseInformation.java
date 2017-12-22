package NetObjModel;

import java.io.Serializable;

public class UserBaseInformation implements Serializable
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
	
	
	public UserBaseInformation(int id, String name, int money, int totalGameTimes, int winTimes, String lastEnterTime,
			int iconID) {
		super();
		Id = id;
		Name = name;
		Money = money;
		TotalGameTimes = totalGameTimes;
		WinTimes = winTimes;
		LastEnterTime = lastEnterTime;
		IconID = iconID;
	}
}
