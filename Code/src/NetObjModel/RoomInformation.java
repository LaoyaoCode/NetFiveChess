package NetObjModel;

import java.io.Serializable;

public class RoomInformation implements Serializable
{
	/**
	 * 房主用户信息
	 */
	public UserBaseInformation CUInformation = null;
	/**
	 * 房间HASH ID
	 */
	public String HashID = null;
	/**
	 * 是否还有空位置
	 */
	public boolean IsEmpty = true ;
	/**
	 * 是否正在游戏中
	 */
	public boolean IsInGame = false ;
	
	public RoomInformation(UserBaseInformation cuInformation, String hashID, boolean isEmpty , boolean isInGame) {;
		CUInformation = cuInformation;
		HashID = hashID;
		IsEmpty = isEmpty;
		IsInGame = isInGame ;
	}
	
	
}
