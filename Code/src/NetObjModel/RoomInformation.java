package NetObjModel;

import java.io.Serializable;

public class RoomInformation implements Serializable
{
	/**
	 * �����û���Ϣ
	 */
	public UserBaseInformation CUInformation = null;
	/**
	 * ����HASH ID
	 */
	public String HashID = null;
	/**
	 * �Ƿ��п�λ��
	 */
	public boolean IsEmpty = true ;
	/**
	 * �Ƿ�������Ϸ��
	 */
	public boolean IsInGame = false ;
	
	public RoomInformation(UserBaseInformation cuInformation, String hashID, boolean isEmpty , boolean isInGame) {;
		CUInformation = cuInformation;
		HashID = hashID;
		IsEmpty = isEmpty;
		IsInGame = isInGame ;
	}
	
	
}
