package Net;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.ietf.jgss.Oid;

import com.sun.prism.j2d.paint.MultipleGradientPaint.CycleMethod;

import DataBase.GameResultModel;
import DataBase.UserBaseModel;
import MainPack.FiveChessMainCode;
import NetObjModel.RoomInformation;
import NetObjModel.UserBaseInformation;
import SomeTool.*;

public class Room 
{
	private User Creater = null;
	private User Opponent = null;
	private String HashID = null;
	
	/**
	 * 在某个位置的棋子类型
	 * @author Laoyao
	 *
	 */
	private enum InPositionChessE
	{
		/**
		 * creater 的棋子在这个位置
		 */
		C ,
		/**
		 * opponent 的棋子在这个位置
		 */
		O ,
		/**
		 * 这个位置为空
		 */
		N
	}
	
	/**
	 * 棋盘棋子
	 * 第一维度为行，第二维度为列
	 */
	private InPositionChessE[][] TableChess = null;
	//棋盘的长宽
	private int TableWAH = 0;
	//游戏者的数目
	private int GamerNumber = 0 ;
	//游戏是否开始
	private boolean IsGameStart = false ;
	//现在活跃着的用户，也就是现在可以下棋的用户
	private User NowActiveUser = null;
	//是否是创建者活跃
	private boolean IsCreaterActive = false ;
	//游戏开始的时间
	private String GameStartTime = null;
	
	public Room(User creater , int tableWAH) 
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		Creater = creater;
		TableWAH = tableWAH ;
		GamerNumber++ ;
		
		TableChess = new InPositionChessE[TableWAH][TableWAH] ;
		
		//初始化所有棋盘位置为空
		for(int counterR = 0 ; counterR < TableWAH ; counterR++)
		{
			for(int counterC = 0 ; counterC < TableWAH ; counterC++ )
			{
				TableChess[counterR][counterC] = InPositionChessE.N;
			}
		}
		
		//创建唯一的ROOM ID
		HashID = StringDispose.GetMD5(creater.GetUserInformation().Id +
				creater.GetUserInformation().IconID + df.format(new Date()) +
				(new Random()).nextDouble()) ;
		
		//通知所有正在查看的用户有新房间被创建
		for (User user : FiveChessMainCode.TotalActiveUsers) 
		{
			if(user.isIsSeeingRoom())
			{
				user.NewRoomCreated(this.GetInformation());
			}
		}
	}
	
	/**
	 * 通知房间的创建者对手的准备状态发生了改变
	 * @param isReady
	 */
	public void OpponentReadyStateChange(boolean isReady)
	{
		Creater.OpponentReadyState(isReady);
	}
	
	/**
	 * 参与房间对战,仅限于参与者
	 * @param opponent
	 */
	public synchronized boolean Join(User opponent)
	{
		//人数已满却还要强行加入，直接返回错误
		if(GamerNumber >= 2)
		{
			return false ;
		}
		
		Opponent = opponent ;
		
		//通知创建者对手已经找到
		Creater.OpponentJoinRoom(opponent.GetUserInformation());
		//增加游戏人数
		GamerNumber++ ;
		
		//通知所有正在查看的用户房间已满
		for (User user : FiveChessMainCode.TotalActiveUsers) 
		{
			if(user.isIsSeeingRoom())
			{
				user.RoomEmptyStateChange(HashID, false);
			}
		}
		
		return true ;
	}
	
	/**
	 * 对手离开，仅限于参与者
	 * 如果还没有开始游戏通知正在查看房间列表的人房间已空
	 * 如果已经开始了游戏，则直接关闭房间
	 */
	public void Cancel()
	{
		//如果游戏已经开始，则通知创建者对手失去了连接
		//房间自动关闭
		if(IsGameStart)
		{
			Opponent = null ;
			//通知创建者对手已经失去了连接，房间关闭
			Creater.OpponentOutOfConnection();
			
			//关闭房间
			CloseTheRoom() ;
		}
		else
		{
			Opponent = null ;
			
			//通知创建者对手已经离开房间
			Creater.OpponentCancelRoom();
			
			//游戏开始
			GamerNumber-- ;
			
			//通知所有正在查看的用户房间已空
			for (User user : FiveChessMainCode.TotalActiveUsers) 
			{
				if(user.isIsSeeingRoom())
				{
					user.RoomEmptyStateChange(HashID, true);
				}
			}
		}
	}
	
	/**
	 * 获得房间唯一HASH ID值
	 * @return
	 */
	public String GetHashID()
	{
		return HashID;
	}
	
	/**
	 * 游戏开始
	 * @param commander 
	 * @return
	 */
	public boolean GameStart(User commander)
	{
		//确定是有创建者发起
		//确认有对手
		//确认对手已经准备好
		if(commander.GetUserInformation().Id == Creater.GetUserInformation().Id
				&& GamerNumber == 2 && Opponent.isIsReady())
		{
			IsGameStart = true ;
			int opponetChessId = Opponent.getUseChessID()  , createrChessId = Creater.getUseChessID();
			String opponentPath = null , createrPath = null ;
			
			//寻找对手的棋子文件名
			for(int counter = 0 ; counter < FiveChessMainCode.SetManager.AllChessImage.size() ; counter++)
			{
				if(FiveChessMainCode.SetManager.AllChessImage.get(counter).getID() == opponetChessId)
				{
					opponentPath = FiveChessMainCode.SetManager.AllChessImage.get(counter).getName() ;
				}
				
				if(FiveChessMainCode.SetManager.AllChessImage.get(counter).getID() == createrChessId)
				{
					createrPath = FiveChessMainCode.SetManager.AllChessImage.get(counter).getName() ;
				}
				
				if(opponentPath != null && createrPath != null)
				{
					break ;
				}
			}
			
			//通知两人游戏开始
			//房主和对手随机分配谁先下
			if((new Random()).nextBoolean())
			{
				Creater.GameStarted(true  , opponentPath);
				Opponent.GameStarted(false , createrPath);
				
				NowActiveUser = Creater ;
				IsCreaterActive = true ;
			}
			else
			{
				Creater.GameStarted(false , opponentPath);
				Opponent.GameStarted(true , createrPath);
				
				NowActiveUser = Opponent;
				IsCreaterActive = false;
			}
			
			//设置日期格式
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//保存游戏开始时间
			GameStartTime = df.format(new Date()) ;
			
			//通知所有正在查看的用户有房间被关闭了
			for (User user : FiveChessMainCode.TotalActiveUsers) 
			{
				if(user.isIsSeeingRoom())
				{
					user.RoomGameStart(HashID);
				}
			}
			
			return true ;
		}
		else
		{
			return false;
		}
	}

	//交换下棋的人
	//在下期完成之后调用
	private void ExchangeActiveUser()
	{
		//如果当前是创造者活跃，则切换为对手
		if(IsCreaterActive)
		{
			IsCreaterActive = false ;
			NowActiveUser = Opponent ;
		}
		//如果当前是对手活跃，则切换为创造者
		else
		{
			IsCreaterActive = true ;
			NowActiveUser = Creater ;
		}
	}
	/**
	 * 游戏是否已经开始
	 * @return
	 */
	public boolean isIsGameStart() {
		return IsGameStart;
	}
	
	/**
	 * 房间创建者主动关闭房间
	 * 只能由创建者调用
	 */
	public void CloseTheRoom()
	{
		//移除该房间
		FiveChessMainCode.TotalActiveRooms.remove(this) ;
		
		if(Opponent != null)
		{
			//通知参与者房间被关闭了
			Opponent.MJRoomClosed();
		}
		
		//通知所有正在查看的用户有房间被关闭了
		for (User user : FiveChessMainCode.TotalActiveUsers) 
		{
			if(user.isIsSeeingRoom())
			{
				user.ARoomClosed(HashID);
			}
		}
	}
	
	/**
	 * 获得创建者的信息
	 * @return
	 */
	public UserBaseInformation GetCInformation()
	{
		return Creater.GetUserInformation() ;
	}
	
	/**
	 * 是否有位置加入
	 * @return
	 */
 	public boolean IsEmpty()
	{
		if(GamerNumber == 1)
		{
			return true ;
		}
		else
		{
			return false ;
		}
	}
 	
 	/**
 	 * 下棋 , 同步机制 
 	 * @param commander发出命令的人
 	 * @param indexX棋子坐标X ， 从0开始
 	 * @param indexY棋子坐标Y ， 从0开始
 	 * @return
 	 */
 	public synchronized boolean PlayChess(User commander  , int indexX , int indexY)
 	{
 		InPositionChessE newChess  ;
 		//如果发出命令者不是当前活跃用户
 		//或者棋子超出了范围
 		//返回错误
 		if(commander.GetUserInformation().Id != NowActiveUser.GetUserInformation().Id ||
 				indexX >= TableWAH || indexY >= TableWAH)
 		{
 			return false ;
 		}
 		
 		//当前位置已经有了棋子
 		if(TableChess[indexY][indexX] != InPositionChessE.N)
 		{
 			return false ;
 		}
 		
 		//如果当前活跃的是创造者
 		if(IsCreaterActive)
 		{
 			TableChess[indexY][indexX] = InPositionChessE.C ;
 			newChess = InPositionChessE.C ;
 			
 			Opponent.OpponentPlayedChess(indexX, indexY);
 		}
 		//如果当前活跃的是对手
 		else
 		{
 			TableChess[indexY][indexX] = InPositionChessE.O ;
 			newChess = InPositionChessE.O ;
 			
 			Creater.OpponentPlayedChess(indexX, indexY);
 		}
 		
 		//当前这一棋子落下的时候是否5子连线赢得比赛
 		if(CalculateIsSomeOneWin(indexX, indexY, newChess))
 		{
 			int wId = 0 , dId = 0 ;
 			GameResultModel resultModel = null ;
 			
 			//设置日期格式
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//结束游戏开始时间
			String overTime = df.format(new Date()) ;
			
 			//创建者胜利
 			if(IsCreaterActive)
 			{
 				Creater.YouWin(FiveChessMainCode.SetManager.getAGameWM());
 				Opponent.YouDefeat();
 				
 				wId = Creater.GetUserInformation().Id ;
 				dId = Opponent.GetUserInformation().Id ;
 			}
 			//对手胜利
 			else
 			{
 				Opponent.YouWin(FiveChessMainCode.SetManager.getAGameWM());
 				Creater.YouDefeat();
 				
 				wId = Opponent.GetUserInformation().Id ;
 				dId = Creater.GetUserInformation().Id ;
 			}
 			
 			//保存对局记录
 			//胜利者失败者各保存一份
 			resultModel = new GameResultModel(wId, dId, overTime, GameStartTime, true) ;
 			FiveChessMainCode.GameDBManager.AddAGameResultRecord(resultModel) ;
 			
 			resultModel = new GameResultModel(dId, wId, overTime, GameStartTime, false) ;
 			FiveChessMainCode.GameDBManager.AddAGameResultRecord(resultModel) ;
 			
 			//通知所有正在查看的用户有房间游戏结束了
 			for (User user : FiveChessMainCode.TotalActiveUsers) 
 			{
 				if(user.isIsSeeingRoom())
 				{
 					user.ARoomGameOver(HashID);
 				}
 			}
 		}
 		//没有人胜利则交换活跃用户
 		else
 		{
 			//交换活跃用户
 			ExchangeActiveUser();
 			//给新的活跃用户下棋的权利                 
 			NowActiveUser.GiveRightToPlay();
 		}
 		
 		return true ;
 	}
 	
 	/**
 	 * 计算是否有人获胜
 	 * @param newX 新的棋子X坐标
 	 * @param newY 新的棋子Y坐标
 	 * @param newChess 新的棋子类型
 	 * @return 当前下棋的用户是否胜利
 	 */
 	private boolean CalculateIsSomeOneWin(int newX , int newY , InPositionChessE newChess)
 	{
 		//自己是一个棋子
 		int sameChessNumber = 0 ;
 		int cx = 0 , cy = 0 ;
 		
 		//竖线，向上走 , 将新棋子计算进去了
 		for(int counter = newY ; counter >= 0 ; counter--)
 		{
 			if(TableChess[counter][newX] == newChess)
 			{
 				sameChessNumber++ ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		//竖线，向上走 ,没有计算新棋子
 		for(int counter = newY + 1; counter < TableWAH ; counter++)
 		{
 			if(TableChess[counter][newX] == newChess)
 			{
 				sameChessNumber++ ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		if(sameChessNumber == 5)
 		{
 			return true ;
 		}
 		
 		sameChessNumber = 0 ;
 		
 		//横线，向左走 , 将新棋子计算进去了
 		for(int counter = newX ; counter >= 0 ; counter--)
 		{
 			if(TableChess[newY][counter] == newChess)
 			{
 				sameChessNumber++ ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		//横线，向左走 , 没有将新棋子计算进去了
 		for(int counter = newX + 1 ; counter < TableWAH ; counter++)
 		{
 			if(TableChess[newY][counter] == newChess)
 			{
 				sameChessNumber++ ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		if(sameChessNumber == 5)
 		{
 			return true ;
 		}
 		
 		sameChessNumber = 0 ;
 		
 		cx = newX ;
 		cy = newY ;
 		//45度角，往左上角走，计算了新棋子
 		while(true)
 		{
 			if(cx < 0 || cy < 0)
 			{
 				break ;
 			}
 			
 			if(TableChess[cy][cx] == newChess)
 			{
 				sameChessNumber++ ;
 				cy-- ;
 				cx-- ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		cx = newX + 1 ;
 		cy = newY + 1 ;
 		//45度角，往右下角走，没有计算新棋子
 		while(true)
 		{
 			if(cx >= TableWAH || cy >= TableWAH)
 			{
 				break ;
 			}
 			
 			if(TableChess[cy][cx] == newChess)
 			{
 				sameChessNumber++ ;
 				cy++ ;
 				cx++ ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		if(sameChessNumber == 5)
 		{
 			return true ;
 		}
 		
 		sameChessNumber = 0 ;
 		
 		cx = newX ;
 		cy = newY ;
 		//135度角，往右上方走，计算了新棋子
 		while(true)
 		{
 			if(cx >= TableWAH || cy < 0 )
 			{
 				break ;
 			}
 			
 			if(TableChess[cy][cx] == newChess)
 			{
 				sameChessNumber++ ;
 				cx++ ;
 				cy-- ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		cx = newX - 1 ;
 		cy = newY + 1;
 		//135度角，往左下方走，没有计算新棋子
 		while(true)
 		{
 			if(cx < 0 || cy >= TableWAH)
 			{
 				break ;
 			}
 			
 			if(TableChess[cy][cx] == newChess)
 			{
 				sameChessNumber++ ;
 				cx-- ;
 				cy++ ;
 			}
 			else
 			{
 				break ;
 			}
 		}
 		
 		if(sameChessNumber == 5)
 		{
 			return true ;
 		}
 		
 		//四个方向全部检测了都没有5子连线，GG
 		return false ;
 	}
 	
	//获得房间信息
	public RoomInformation GetInformation()
	{
		if(GamerNumber == 1)
		{
			return new RoomInformation(Creater.GetUserInformation(), HashID, true , IsGameStart) ;
		}
		//位置已满，返回没有空间
		else
		{
			return new RoomInformation(Creater.GetUserInformation(), HashID, false , IsGameStart) ;
		}
		
	}

	//获取用于服务器端显示的信息
	public String GetSDisplayInformation()
	{
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append(String.format("Creater Name:%10s", Creater.GetUserInformation().Name)) ;
		if(GamerNumber == 2)
		{
			bulider.append(String.format("   Opponent Name:%10s" , Opponent.GetUserInformation().Name)) ;
		}
		else
		{
			bulider.append(String.format("   Opponent Name:%10s" , "Nobody") );
		}
		
		bulider.append(String.format("   Hash:%s", HashID)) ;
		
		return bulider.toString() ;
	}
}
