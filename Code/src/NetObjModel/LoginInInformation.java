package NetObjModel;

import java.io.Serializable;

public class LoginInInformation implements Serializable
{
	/**
	 * ����
	 */
	public String Name = null ;
	/**
	 * ������
	 */
	public String MachineID = null;
	
	public LoginInInformation(String name, String machineID)
	{
		Name = name;
		MachineID = machineID;
	}
	
	public LoginInInformation()
	{
		this(null, null);
	}
}
