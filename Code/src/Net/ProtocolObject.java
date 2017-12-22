package Net;

import java.io.Serializable;

import Net.ProtocolObject.CommandTypeEnum;
import Net.ProtocolObject.ResultEnum;

/**
 * Э�����
 * @author Laoyao
 *
 */
public class ProtocolObject implements Serializable
{
	/**
	 * DisposeFailed ΪĬ��ֵ
	 * @author Laoyao
	 *
	 */
	public enum ResultEnum
	{
		DisposeFailed ,
		DisposeSucceed
	}
	
	public enum CommandTypeEnum
	{
		//-----�ͻ��˷�����������˷��ص�ʱ�򸽴��Ϳͻ���һ����COMMAND-----------------
		LoginIn,
		SeeMineInformation,
		ChangeUserIcon,
		BuyGoods,
		ChooseChess,
		CreateRoom,
		SeeRoom,
		JoinRoom,
		CancelRoom,
		GetReady,
		CancelReady,
		StartGame,
		GiveChessPosition,
		SendMessage,
		SeeAllIcons,
		SeeAllChesss,
		EnterSelectRoomW ,
		CancelSelectRoomW,
		ICloseMRoom,
		IPlayPosition,
		GMAllResult,
		SeeAllMChess,
		//-----------------------
		GiveRightToPlay,
		OpponentPlayPosition,
		GameStarted,
		OpponentReadyState,
		RoomGameStart,
		RoomEmptyStateChange,
		MJRoomClosed,
		ARoomClosed,
		NewRoomCreated,
		OpponentJoinRoom,
		OpponentCancelRoom,
		OpponentOutOfConnection,
		YouWin,
		YouDefeat,
		ARoomGameOver,
		NOTHING
	}
	
	/**
	 * ���ͻ��߷��ص�����
	 */
	public Object Data = null;
	/**
	 * ������ö����
	 */
	public ResultEnum Result = ResultEnum.DisposeFailed;
	/**
	 * �������ͽṹ��
	 */
	public CommandTypeEnum CommandType = CommandTypeEnum.NOTHING;
	
	public ProtocolObject(Object data, ResultEnum result  , CommandTypeEnum command)
	{
		Data = data;
		Result = result;
		CommandType = command ;
	}
	
	public ProtocolObject()
	{
		this(null , ResultEnum.DisposeFailed , CommandTypeEnum.NOTHING) ;
	}
}
