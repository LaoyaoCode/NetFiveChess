package Net;

import java.io.Serializable;

import Net.ProtocolObject.CommandTypeEnum;
import Net.ProtocolObject.ResultEnum;

/**
 * 协议对象
 * @author Laoyao
 *
 */
public class ProtocolObject implements Serializable
{
	/**
	 * DisposeFailed 为默认值
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
		//-----客户端发起的命令，服务端返回的时候附带和客户端一样的COMMAND-----------------
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
	 * 发送或者返回的数据
	 */
	public Object Data = null;
	/**
	 * 处理结果枚举体
	 */
	public ResultEnum Result = ResultEnum.DisposeFailed;
	/**
	 * 命令类型结构体
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
