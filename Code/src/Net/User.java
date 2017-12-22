package Net;
import DataBase.*;
import MainPack.ErrorRecordManager.ErrorRecord;
import MainPack.FiveChessMainCode;
import Net.ProtocolObject.CommandTypeEnum;
import Net.ProtocolObject.ResultEnum;
import NetObjModel.ChessInformationM;
import NetObjModel.ChessOrIconImage;
import NetObjModel.ChessPosition;
import NetObjModel.GameResultInformation;
import NetObjModel.LoginInInformation;
import NetObjModel.RoomInformation;
import NetObjModel.StringWithBoolean;
import NetObjModel.UserBaseInformation;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class User implements IClientSendToServer , IClientLeave
{
	private static class InputDisposeObj implements Runnable
	{
		//对象输入流
		private ObjectInputStream OIS = null;
		//是否关闭
		private boolean IsClose = false ;
		private IClientSendToServer RecieveDispose = null;
		private IClientLeave Leave = null;
		
		
		public InputDisposeObj(ObjectInputStream iStream , IClientSendToServer del , IClientLeave leave) 
		{
			OIS = iStream ;
			RecieveDispose = del ;
			Leave = leave;
		}
		
		@Override
		public void run() 
		{
			while(!IsClose)
			{		
				try
				{
					ProtocolObject obj =(ProtocolObject) OIS.readObject();
					
					RecieveDispose.RecieveData(obj);
				}
				catch (Exception e)
				{
					//意外关闭，客户端失去了连接
					//如果不需要实现续连功能，则直接将User关闭，对局关闭，房间关闭
					
					//续连功能似乎对于五子棋来说有些鸡肋
					//如何判定对方会不会来呢？难道一直等着？
					//亦或是由剩下的人来决定？
					//最终决定当用户失去连接的时候，直接关闭
					ErrorRecord record = new ErrorRecord(e.getMessage()) ;
					record.SetCodePostion(getClass().getSimpleName(), "run");
					FiveChessMainCode.ErrorRManager.AddRecord(record);
					
					//需要CLIENT主动断开连接才调用
					//SERVER自动关闭不需要调用
					if(!IsClose)
					{
						//调用委托
						Leave.ClientLeave();
					}
					
					break ;
				}
			}
		}

		public void Finished()
		{
			IsClose = true ;
		}
		
	}
	
	//与客户端连接的套接字
	private Socket ConnectWithClient = null;
	//对象输入流无需注意线程同步问题，因为只有一个线程
	private ObjectInputStream IS = null;
	//对象输出流注意线程同步问题!!!!!!!!!!!!!!!!!!!!!!!!!!
	private ObjectOutputStream OS = null;
	//用户基本信息
	private UserBaseModel Information = null ;
	//是否加入了房间
	private boolean IsEnterRoom = false ;
	//是否正在游戏之中
	private boolean IsInGame = false ;
	//是否登陆
	private boolean IsLoginIn = false ;
	//是否正在查看房间
	private boolean IsSeeingRoom = false;
	//加入的房间对象
	private Room EnterRoomObject = null;
	//是否是房间的创建者
	private boolean IsRoomCreater = false;
	//是否已经准备好，只针对于房间的参加者
	private boolean IsReady =false;
	//使用棋子的ID
	//ID为零是每次默认的棋子
	private int UseChessID = 0 ;
	//输入处理对象
	private InputDisposeObj IDO = null;
	//是否准备好下棋，也就是是否轮到该用户
	private boolean IsReadyToChess = false ;
	
	public User(Socket connection , ObjectInputStream iStream ,
			ObjectOutputStream oStream)
	{
		ConnectWithClient = connection ;
		IS = iStream ;
		OS = oStream;
		IDO = new InputDisposeObj(IS, this , this);
		
		//加入线程执行
		FiveChessMainCode.TPExecutor.execute(IDO);
	}

	//没有在主线程中执行 , 不可执行耗时操作
	@Override
	public void RecieveData(ProtocolObject obj)
	{
		//别忘了加BREAK
		switch (obj.CommandType) {
		case LoginIn:
			LoginIn((LoginInInformation)obj.Data) ;
			break;
		case SeeMineInformation:
			SeeMineInformation();
			break;
		case SeeAllIcons:
			SeeAllIcons();
			break;
		case SeeAllChesss:
			SeeAllChesss();
			break;
		case ChangeUserIcon:
			ChangeUserIcon((int)obj.Data);
			break;
		case BuyGoods:
			BuyGoods((int)obj.Data) ;
			break;
		case ChooseChess:
			ChooseUseChess((int)obj.Data);
			break;
		case CreateRoom:
			CreateRoom() ;
			break ;
		case SeeRoom:
			SeeRoom() ;
			break ;
		case EnterSelectRoomW:
			EnterRoomSelectW();
			break ;
		case CancelSelectRoomW:
			CancelRoomSelectW();
			break;
		case ICloseMRoom:
			ICloseMRoom() ;
			break ;
		case GetReady:
			GetReady() ;
			break ;
		case CancelReady:
			CancelReady();
			break ;
		case JoinRoom:
			JoinRoom((String)obj.Data) ;
			break ;
		case CancelRoom:
			CancelRoom() ;
			break ;
		case StartGame:
			StartGame() ;
			break ;
		case IPlayPosition:
			IPlayPosition((ChessPosition)obj.Data);
			break ;
		case GMAllResult:
			GMAllResult() ;
			break ;
		case SeeAllMChess:
			SeeAllMChess() ;
			break ;
		default:
			break;
		}
	}
	
	//客户端登录
	private void LoginIn(LoginInInformation information)
	{
		UserBaseModel model = FiveChessMainCode.UserDBManager.UseNameToFindUser(information.Name) ;
		ChessOrIconImage iconImage = null ;
		
		
		//空，则创建并添加到数据库
		if(model == null)
		{
			
			Random random = new Random() ;
			
			//随机头像
			iconImage = FiveChessMainCode.SetManager.AllIconImage.get(
					random.nextInt(FiveChessMainCode.SetManager.AllIconImage.size())) ;
			
			model = new UserBaseModel(information.Name, 0, 0, 0, null, iconImage.getID() , information.MachineID) ;
			
			//添加不成功
			if(!FiveChessMainCode.UserDBManager.AddAUserBaseRecord(model))
			{
				ProtocolObject obj = new ProtocolObject() ;
				//标识类型为登录
				obj.CommandType = CommandTypeEnum.LoginIn;
				obj.Result = ResultEnum.DisposeFailed ;
				//添加错误信息
				obj.Data = "创建新用户失败" ;
				
				//返回失败信息
				SendData(obj) ;
				return ;
			}
		}
		//获取对应icon数据
		else
		{
			//machine id 不相符合
			//也就是机器与user name不相符合
			if(!model.MachineID.equals(information.MachineID))
			{
				ProtocolObject obj = new ProtocolObject() ;
				//标识类型为登录
				obj.CommandType = CommandTypeEnum.LoginIn;
				obj.Result = ResultEnum.DisposeFailed ;
				//添加错误信息
				obj.Data = "该用户名已被使用" ;
				
				//返回失败信息
				SendData(obj) ;
				return ;
			}
			
			for(int counter = 0 ; counter < FiveChessMainCode.SetManager.AllIconImage.size() ; counter++)
			{
				if(FiveChessMainCode.SetManager.AllIconImage.get(counter).getID()
						== model.IconID)
				{
					iconImage = FiveChessMainCode.SetManager.AllIconImage.get(counter) ;
					break ;
				}
			}
		}
		
		
		//保存用户信息
		Information = model ;
		
		ProtocolObject obj = new ProtocolObject() ;
		//标识类型为登录
		obj.CommandType = CommandTypeEnum.LoginIn;
		//载入数据
		obj.Data = iconImage;
		obj.Result = ResultEnum.DisposeSucceed ;
		
		//返回信息
		SendData(obj) ;
		
		//标识为已经登录
		IsLoginIn = true;
		
		return;
	}
	
	//在客户端得到了所有的房间信息之后调用
	//即，EnterRoomSelectW 需要在 SeeRoom之后调用
	private void EnterRoomSelectW()
	{
		ProtocolObject obj = new ProtocolObject() ;
		
		IsSeeingRoom = true ;
		
		//载入数据
		obj.CommandType = CommandTypeEnum.EnterSelectRoomW ;
		obj.Result = ResultEnum.DisposeSucceed ;
				
		//返回信息
		SendData(obj) ;
	}
	
	//离开房间选择窗口
	private void CancelRoomSelectW()
	{
		ProtocolObject obj = new ProtocolObject() ;
		
		IsSeeingRoom = false ;
		
		//载入数据
		obj.CommandType = CommandTypeEnum.EnterSelectRoomW ;
		obj.Result = ResultEnum.DisposeSucceed ;
				
		//返回信息
		SendData(obj) ;
		
		System.out.println("Cancel Room Select W");
	}
	
	//查看所有的房间 ， 未测试
	private void SeeRoom()
	{
		List<RoomInformation> data = new LinkedList<>() ;
		
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.SeeRoom;
		
		//获得所有房间的数据
		for(int counter = 0 ; counter < FiveChessMainCode.TotalActiveRooms.size() ; counter++)
		{
			data.add(FiveChessMainCode.TotalActiveRooms.get(counter).GetInformation());
		}
		
		object.Data = data ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		SendData(object);
	}
	
	//创建房间
	private void CreateRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.CreateRoom ;
		
		//没有登录，直接返回错误
		//已经有了房间，直接返回错误
		if(!IsLoginIn || EnterRoomObject != null)
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		else
		{
			//创建新的房间
			Room room = new Room(this, FiveChessMainCode.SetManager.getChessTableRC()) ;
			//添加到房间总集
			FiveChessMainCode.TotalActiveRooms.add(room) ;
			
			//标示为是房间创造者
			IsRoomCreater = true ;
		
			//已经进入了房间
			IsEnterRoom = true;
			
			//赋值房间对象
			EnterRoomObject = room;
			
			object.Data = room.GetHashID();
			object.Result = ResultEnum.DisposeSucceed ;
		}
		
		SendData(object);
	}
	
	//用户购买物品
	private void BuyGoods(int goodsId)
	{
		ChessInformationM goodsInformation = null ;
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.BuyGoods ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		//寻找货物信息
		for (ChessInformationM chess : FiveChessMainCode.SetManager.AllChessImage) 
		{
			if(chess.getID() == goodsId)
			{
				goodsInformation = chess;
			}
		}
		
		//没有找到物品，或者没有登录，直接返回失败
		if(goodsInformation == null || !IsLoginIn)
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		//找到了物品
		else
		{
			//金钱不足，返回失败
			if(Information.Money < goodsInformation.getPrice())
			{
				object.Result = ResultEnum.DisposeFailed ;
			}
			//金钱足够
			else
			{
				int originalMoney = Information.Money ;
				UserAndGoodsModel buyRecord = new UserAndGoodsModel() ;
					
				buyRecord.GoodsId = goodsId ;
				buyRecord.UserId = Information.Id ;
				
				//写入物品记录失败
				//直接返回错误
				if(!FiveChessMainCode.UserDBManager.AddAUserAndGoodsRecord(buyRecord))
				{
					object.Result = ResultEnum.DisposeFailed ;
				}
				//写入物品购买记录成功
				else
				{
					Information.Money = Information.Money - goodsInformation.getPrice() ;
					
					//写入数据表失败，抹除修改，返回原值
					//由于物品记录已经写入
					//故而此时，只能够让用户免费购买了一次物品
					if(!FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information))
					{
						Information.Money = originalMoney ;
					}
				}
			}
		}
		
		//发送数据
		this.SendData(object);
	}
	
	//查看所有的Icons
	private void SeeAllIcons()
	{	
		
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = FiveChessMainCode.SetManager.AllIconImage;
		object.CommandType = CommandTypeEnum.SeeAllIcons ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	//查看所有棋子
	private void SeeAllChesss()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = FiveChessMainCode.SetManager.AllChessImage;
		object.CommandType = CommandTypeEnum.SeeAllChesss ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	//改变用户头像
	private void ChangeUserIcon(int iconId)
	{
		boolean result = false;
		ProtocolObject object = new ProtocolObject() ;
		int originalIconId = 0 ;
		boolean isGetMatchInformation = false ;
		
		object.CommandType = CommandTypeEnum.ChangeUserIcon ;
		
		for (ChessOrIconImage information : FiveChessMainCode.SetManager.AllIconImage)
		{
			if(information.getID() == iconId)
			{
				isGetMatchInformation = true ;
			}
		}
		
		//登录+寻找到对应的信息，也就是输入了正确的ID
		if(IsLoginIn || isGetMatchInformation)
		{
			//保存原始的ICON ID
			originalIconId = Information.IconID ;
			
			//更新头像ID
			Information.IconID = iconId ;
			result = FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information) ;
			
			//数据库成功则返回执行成功
			if(result)
			{
				object.Result = ResultEnum.DisposeSucceed ;
			}
			//数据库更新失败则恢复ICON ID ， 返回错误
			else
			{
				//恢复为原来的ICON ID
				Information.IconID = originalIconId ;
				object.Result = ResultEnum.DisposeFailed ;
			}
		}
		//没有登录，没有找到信息，则无法修改用户ICON ID，直接返回错误
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		
		this.SendData(object);
	}
	
	//用户想查看自己的详细信息
	private void SeeMineInformation()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		
		//没有登录，则直接返回错误
		if(!IsLoginIn)
		{
			System.out.println("Not Login In , but want to see");
			object.CommandType = CommandTypeEnum.SeeMineInformation ;
			object.Result = ResultEnum.DisposeFailed ;
		}
		else
		{
			object.Data = UserBaseModel.GetSendInstance(Information);
			object.CommandType = CommandTypeEnum.SeeMineInformation ;
			object.Result = ResultEnum.DisposeSucceed ;
		}
		
		//开启线程发送数据到客户端
		this.SendData(object);
	}
	
	//选择使用的棋子
	private void ChooseUseChess(int chessId)
	{
		boolean isFind = false ;
		
		ProtocolObject object = new ProtocolObject();
		
		//遍历寻找对应ID是否存在
		for (ChessOrIconImage chess : FiveChessMainCode.SetManager.AllChessImage) 
		{
			if(chess.getID() == chessId)
			{
				isFind = true ;
			}
		}
		
		//找到了
		if(isFind)
		{
			object.CommandType = CommandTypeEnum.ChangeUserIcon;
			object.Result = ResultEnum.DisposeSucceed ;
			
			UseChessID = chessId ;
		}
		//如果没有找到 ， 不改变变量的值
		else
		{
			object.CommandType = CommandTypeEnum.ChangeUserIcon;
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		this.SendData(object);
	}

	//我关闭了自己的房间 , 未测试
	private void ICloseMRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.ICloseMRoom ;
		
		//只有加入了房间并且房间对象存在
		//并且房间是自己创建的时候
		if(IsEnterRoom && EnterRoomObject != null && IsRoomCreater)
		{
			//关闭房间
			EnterRoomObject.CloseTheRoom();
			//清空重置标志位
			IsRoomCreater = false ;
			EnterRoomObject = null ;
			IsEnterRoom = false ;
			IsInGame = false ;
			
			object.Result = ResultEnum.DisposeSucceed ;
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		//发送信息回去
		SendData(object);
	}
	
	//我准备好了 ， 未测试
	private void GetReady()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.GetReady ;
		//进入了房间
		//不是房间创建者
		//房间对象不为空
		//没有准备
		if(IsEnterRoom && !IsRoomCreater && EnterRoomObject != null && !IsReady)
		{
			IsReady = true ;
			EnterRoomObject.OpponentReadyStateChange(true);
			object.Result = ResultEnum.DisposeSucceed ;
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}

	//取消准备状态，未测试
	private void CancelReady()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.CancelReady;
		
		//进入了房间
		//不是房间创建者
		//房间对象不为空
		//准备好了
		if(IsEnterRoom && !IsRoomCreater && EnterRoomObject != null && IsReady)
		{
			IsReady = false ;
			EnterRoomObject.OpponentReadyStateChange(false);
			object.Result = ResultEnum.DisposeSucceed ;
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	//加入房间
	private void JoinRoom(String hashId) 
	{
		ProtocolObject object = new ProtocolObject() ;
		Room matchRoom = null ;
		
		object.CommandType = CommandTypeEnum.JoinRoom;
		
		//寻找对应ID的房间
		for(int counter = 0 ; counter < FiveChessMainCode.TotalActiveRooms.size() ; counter++)
		{
			if(FiveChessMainCode.TotalActiveRooms.get(counter).GetHashID().equals(hashId))
			{
				matchRoom = FiveChessMainCode.TotalActiveRooms.get(counter) ;
				break ;
			}
		}
		
		//找到了房间
		//没有加入房间
		//不是房间创建者
		if(matchRoom != null && !IsEnterRoom && !IsRoomCreater)
		{	
			//房间有空位且游戏没有开始
			if(matchRoom.IsEmpty() && !matchRoom.isIsGameStart())
			{
				//加入房间成功
				if(matchRoom.Join(this))
				{
					IsEnterRoom = true ;
					IsReady = false ;
					EnterRoomObject = matchRoom ;
					
					UseChessID = 0 ;
					
					//给客户端房主的信息
					object.Data = matchRoom.GetCInformation() ;
					
					object.Result = ResultEnum.DisposeSucceed ;
				}
				//加入房间失败
				else
				{
					object.Result = ResultEnum.DisposeFailed ;
				}
				
			}
			else
			{
				object.Result = ResultEnum.DisposeFailed ;
			}
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	//离开房间
	private void CancelRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.CancelRoom;
		//进入了房间
		//不是房间创建者
		//房间对象不为空
		if(IsEnterRoom && !IsRoomCreater && EnterRoomObject != null)
		{
			//通知房间我已经离开
			EnterRoomObject.Cancel() ;
			//还原变量和标志
			IsEnterRoom = false ;
			EnterRoomObject = null ;
			IsReady = false ;
			
			object.Result = ResultEnum.DisposeSucceed ;
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	//游戏开始，只能由房间创建者调用
	private void StartGame()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.StartGame ;
		
		if(IsEnterRoom && IsRoomCreater && EnterRoomObject != null)
		{
			//开始游戏成功
			if(EnterRoomObject.GameStart(this))
			{
				object.Result = ResultEnum.DisposeSucceed ;
			}
			else
			{
				object.Result = ResultEnum.DisposeFailed ;
			}
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	//用户下
	private void IPlayPosition(ChessPosition position)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.IPlayPosition ;
		
		//正在游戏中
		//拥有下棋的权利
		if(IsInGame && IsReadyToChess)
		{
			//当下棋成功
			if(EnterRoomObject.PlayChess(this, position.X, position.Y))
			{
				//现在没有下棋的权利了
				IsReadyToChess = false ;
				object.Result = ResultEnum.DisposeSucceed ;
			}
			else
			{
				object.Result = ResultEnum.DisposeFailed ;
			}
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	//获得自己所有对战信息，未测试
	private void GMAllResult()
	{
		ProtocolObject object = new ProtocolObject() ;
		boolean isSucceed = true ; 
		List<GameResultInformation> informations = null ;
		
		object.CommandType = CommandTypeEnum.GMAllResult ;
		
		//登陆成功
		//然后成功查询到了数据
		if(IsLoginIn && (informations = FiveChessMainCode.GameDBManager.GetTotalResult(Information.Id)) != null)
		{
			for(int counter = 0 ; counter < informations.size() ; counter++)
			{
				//获得对手模型信息
				UserBaseModel model = FiveChessMainCode.UserDBManager.UseIDToFindUser(informations.get(counter).OpponentId) ;
				
				//没有获取到则直接返回错误
				if(model == null)
				{
					isSucceed = false ;
					break ;
				}
				else
				{
					informations.get(counter).OpponentInformation = UserBaseModel.GetSendInstance(model) ;
				}
			}
		}
		else
		{
			isSucceed = false ;
		}
		
		//如果成功则装入数据
		if(isSucceed)
		{
			object.Data = informations ;
			object.Result = ResultEnum.DisposeSucceed ;
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	//用户断开连接，直接将用户删除在活跃用户列表之中
	@Override
	public void ClientLeave()
	{
		//只有登录了的人才会去修改最后登录的时间
		if(IsLoginIn)
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			//将用户移除活跃用户列表
			FiveChessMainCode.TotalActiveUsers.remove(this) ;
			//将归属游戏房间以及游戏结束退出
			//修改最后进入时间
			Information.LastEnterTime = df.format(new Date());
			//修改数据，保存在数据库之中,更新数据
			FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information);
			
		}
		
		if(IsEnterRoom)
		{
			//如果是创建者并且房间不为空
			//直接关闭房间
			if(IsRoomCreater && EnterRoomObject != null)
			{
				EnterRoomObject.CloseTheRoom(); 
			}
			
			//是参与者
			//退出房间
			if(!IsRoomCreater && EnterRoomObject != null)
			{
				EnterRoomObject.Cancel();
			}
		}
				
		//关闭自己
		this.Close();
	}

	//发送数据，开启同步 ， 异步进行
	private void SendData(ProtocolObject obj) 
	{
		FiveChessMainCode.TPExecutor.execute(new Runnable() 
		{
			
			@Override
			public void run() 
			{
				//锁住发送流对象
				synchronized (OS) 
				{
					try
					{
						
						OS.writeObject(obj);
						OS.flush();
					}
					catch (Exception e) 
					{
						ErrorRecord record = new ErrorRecord(e.getMessage()) ;
						record.SetCodePostion(getClass().getSimpleName(), "SendData");
						FiveChessMainCode.ErrorRManager.AddRecord(record);
					}
				}
			}
		});
	}

	//查看属于自己的所有棋子信息
	//编号为零的棋子每个用户默认都拥有
	//无需通知
	//未测试
	private void SeeAllMChess()
	{
		List<ChessInformationM> mChesss= new ArrayList<>();
		List<Integer> mCID = null ;
		
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.SeeAllMChess ;
		object.Result =ResultEnum.DisposeSucceed ;
		
		if(IsLoginIn)
		{
			mCID = FiveChessMainCode.UserDBManager.GetAllUserHadGoods(Information.Id) ;
			
			//查询失败，直接返回错误
			if(mCID == null)
			{
				object.Result =ResultEnum.DisposeFailed ;
			}
			else
			{
				for(int counter = 0 ; counter < mCID.size() ; counter++)
				{
					//遍历列表寻找对应ID的信息
					for (ChessInformationM information : FiveChessMainCode.SetManager.AllChessImage)
					{
						//寻找到了则添加到data里
						if(information.getID() == mCID.get(counter))
						{
							mChesss.add(information) ;
						}
					}
				}
				
				//添加数据
				object.Data = mChesss ;
			}
		}
		//没有登录，直接返回错误
		else
		{
			object.Result =ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
//------------------------------------------------------------------------------
	
	/**
	 * 获取用户信息
	 * @return
	 */
	public UserBaseInformation GetUserInformation()
	{
		return UserBaseModel.GetSendInstance(Information);
	}
	
	/**
	 * 是否已经准备好
	 * @return
	 */
	public boolean isIsReady() {
		return IsReady;
	}

	/**
	 * 游戏开始
	 * @param isOriginalActive 初始状态是否是可以下棋
	 * @param 对手使用的棋子文件名，通用文件名
	 */
	public void GameStarted(boolean isOriginalActive , String opponentChessFileName)
	{
		StringWithBoolean data = new StringWithBoolean() ;
		ProtocolObject object = new ProtocolObject() ;
		
		IsInGame = true ;
		IsReadyToChess = isOriginalActive ;
		
		data.BOOLEAN_VALUE = isOriginalActive ;
		data.STRING_VALUE = opponentChessFileName ;
		
		object.Data = data ;
		object.CommandType = CommandTypeEnum.GameStarted ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	/**
	 * 游戏在开始之后对手失去了连接，通知我,我是房间创建者
	 */
	public void OpponentOutOfConnection()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//房间信息清空
		IsEnterRoom = false ;
		EnterRoomObject = null ;
		//准备状态还原
		IsReady = false ;
		IsInGame = false ;
		
		object.CommandType = CommandTypeEnum.OpponentOutOfConnection ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	/**
	 * 你已经胜利
	 * @param winMoney 赢得的金钱数
	 */
	public void YouWin(int winMoney)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//准备状态还原
		//游戏状态还原
		IsReady = false ;
		IsInGame = false ;
		UseChessID = 0 ;
		
		Information.Money += winMoney ;
		Information.WinTimes ++ ;
		Information.TotalGameTimes ++ ;
		
		//更新数据库数据成功
		//返回成功数据
		if(FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information))
		{
			object.Data = winMoney ;
			object.CommandType = CommandTypeEnum.YouWin ;
			object.Result = ResultEnum.DisposeSucceed ;
		}
		//更新数据库失败，返回错误表示，你赢了，但是服务端出现了错误
		//数据没有保存
		else
		{
			object.CommandType = CommandTypeEnum.YouWin ;
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		
		SendData(object);
	}
	
	/**
	 * 你输了
	 */
	public void YouDefeat()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//准备状态还原
		//游戏状态还原
		IsReady = false ;
		IsInGame = false ;
		UseChessID = 0 ;
		
		Information.TotalGameTimes ++ ;
		
		//更新数据库数据成功
		//返回成功数据
		if(FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information))
		{
			object.CommandType = CommandTypeEnum.YouDefeat ;
			object.Result = ResultEnum.DisposeSucceed ;
		}
		//更新数据库失败，返回错误表示，你输了了，但是服务端出现了错误
		//数据没有保存
		else
		{
			object.CommandType = CommandTypeEnum.YouDefeat ;
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	
	/**
	 * 给予用户下棋的权利
	 */
	public void GiveRightToPlay()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		IsReadyToChess = true ;
		
		object.CommandType = CommandTypeEnum.GiveRightToPlay ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		SendData(object);
	}
	
	
	/**
	 * 房间被创建者关闭了
	 * 通知对手房间被关闭
	 */
	public void MJRoomClosed()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//房间信息清空
		IsEnterRoom = false ;
		EnterRoomObject = null ;
		//准备状态还原
		IsReady = false ;
		IsInGame = false ;
		
		object.CommandType = CommandTypeEnum.MJRoomClosed ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	/**
	 * 一个房间被关闭了，只有正在查看房间的用户才会被调用
	 * @param hashId被关闭房间的HASH 值
	 */
	public void ARoomClosed(String hashId)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = hashId ;
		object.CommandType = CommandTypeEnum.ARoomClosed ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	/**
	 * 一个房间的游戏已经结束了，只有正在查看房间的用户才会被调用
	 * @param hashId
	 */
	public void ARoomGameOver(String hashId)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = hashId ;
		object.CommandType = CommandTypeEnum.ARoomGameOver ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	/**
	 * 对手下棋
	 * 在给我下棋权利之前调用
	 * @param x 对手下的X坐标
	 * @param y 对手下的Y坐标
	 */
	public void OpponentPlayedChess(int x , int y)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.OpponentPlayPosition ;
		
		if(IsInGame)
		{
			ChessPosition position = new ChessPosition() ;
			
			position.X = x ;
			position.Y = y ;
			
			object.Data = position ;
			object.Result = ResultEnum.DisposeSucceed ;
		}
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	
	
	/**
	 * 新的房间被创建了，只有正在查看房间的用户才会被调用
	 */
	public void NewRoomCreated(RoomInformation information)
	{
		if(!IsSeeingRoom)
		{
			return ;
		}
		else
		{
			ProtocolObject object = new ProtocolObject() ;
			
			object.Data = information ;
			object.CommandType = CommandTypeEnum.NewRoomCreated ;
			object.Result = ResultEnum.DisposeSucceed ;
			
			this.SendData(object);
		}
	}

	/**
	 * 房间是否空缺状态被改变，只有正在查看房间的用户才会被调用
	 * @param hashID
	 * @param isEmpty
	 */
	public void RoomEmptyStateChange(String hashID , boolean isEmpty)
	{
		if(!IsSeeingRoom)
		{
			return ;
		}
		{
			StringWithBoolean data = new StringWithBoolean() ;
			ProtocolObject object = new ProtocolObject() ;
			
			data.BOOLEAN_VALUE = isEmpty ;
			data.STRING_VALUE = hashID ;
			
			object.Data = data ;
			object.CommandType = CommandTypeEnum.RoomEmptyStateChange ;
			object.Result = ResultEnum.DisposeSucceed ;
			
			this.SendData(object);
		}
		
	}

	/**
	 * 房间参与者是否准备好了
	 * USER为创建者
	 * @param isReady 是否准备好
	 */
	public void OpponentReadyState(boolean isReady)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = isReady ;
		object.CommandType = CommandTypeEnum.OpponentReadyState ; 
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}

	/**
	 * 房间游戏开始，通知正在查看房间的人
	 * @param hashID
	 */
	public void RoomGameStart(String hashID)
	{
		if(!IsSeeingRoom)
		{
			return ;
		}
		else
		{
			ProtocolObject object = new ProtocolObject() ;
			
			object.Data = hashID ;
			object.CommandType = CommandTypeEnum.RoomGameStart ;
			object.Result = ResultEnum.DisposeSucceed ;
			
			this.SendData(object);
		}
	}
	
	/**
	 * 对手加入了房间
	 * @param information
	 */
	public void OpponentJoinRoom(UserBaseInformation information)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = information ;
		object.CommandType = CommandTypeEnum.OpponentJoinRoom;
		object.Result = ResultEnum.DisposeSucceed ;
		
		SendData(object);
	}
	
	/**
	 * 对手离开了房间
	 */
	public void OpponentCancelRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.OpponentCancelRoom;
		object.Result = ResultEnum.DisposeSucceed ;
		
		SendData(object);
	}
	
	/**
	 * 用户是否正在查看房间
	 * @return
	 */
	public boolean isIsSeeingRoom() {
		return IsSeeingRoom;
	}

	
	public int getUseChessID() {
		return UseChessID;
	}

	public void Close()
	{
		try
		{
			IDO.Finished();

			OS.close();
			IS.close();
			ConnectWithClient.close();
		}
		catch (Exception e) 
		{
			// TODO: handle exception
		}
	}
	
	//获得在服务器端显示的字符
	public String GetSDisplayInformation()
	{
		StringBuilder bulider = new StringBuilder() ;
		
		if(IsLoginIn)
		{
			bulider.append(String.format("User Id:%5d" ,Information.Id )) ;
			bulider.append(String.format("   Name:%10s",Information.Name)) ;
		}
		else
		{
			bulider.append("User Not Login In") ;
		}
		
		return bulider.toString() ;
	}
}
