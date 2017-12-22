package NetObjModel;

import java.io.Serializable;

public class UserBaseInformation implements Serializable
{
	/**
	 * �û��ڱ���Ψһ��ID
	 */
	public int Id = 0 ;
	/**
	 * �û�����
	 */
	public String Name = null;
	/**
	 * �û���Ǯ��
	 */
	public int Money = 0;
	/**
	 * �û��ܹ���Ϸ����
	 */
	public int TotalGameTimes = 0;
	/**
	 * �û�Ӯ�õĴ���
	 */
	public int WinTimes = 0;
	/**
	 * ����¼��ʱ��
	 */
	public String LastEnterTime = null;
	/**
	 * ͷ���ID��Ĭ��Ϊ0
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
