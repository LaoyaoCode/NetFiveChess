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
		//����������
		private ObjectInputStream OIS = null;
		//�Ƿ�ر�
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
					//����رգ��ͻ���ʧȥ������
					//�������Ҫʵ���������ܣ���ֱ�ӽ�User�رգ��Ծֹرգ�����ر�
					
					//���������ƺ�������������˵��Щ����
					//����ж��Է��᲻�����أ��ѵ�һֱ���ţ�
					//�������ʣ�µ�����������
					//���վ������û�ʧȥ���ӵ�ʱ��ֱ�ӹر�
					ErrorRecord record = new ErrorRecord(e.getMessage()) ;
					record.SetCodePostion(getClass().getSimpleName(), "run");
					FiveChessMainCode.ErrorRManager.AddRecord(record);
					
					//��ҪCLIENT�����Ͽ����Ӳŵ���
					//SERVER�Զ��رղ���Ҫ����
					if(!IsClose)
					{
						//����ί��
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
	
	//��ͻ������ӵ��׽���
	private Socket ConnectWithClient = null;
	//��������������ע���߳�ͬ�����⣬��Ϊֻ��һ���߳�
	private ObjectInputStream IS = null;
	//���������ע���߳�ͬ������!!!!!!!!!!!!!!!!!!!!!!!!!!
	private ObjectOutputStream OS = null;
	//�û�������Ϣ
	private UserBaseModel Information = null ;
	//�Ƿ�����˷���
	private boolean IsEnterRoom = false ;
	//�Ƿ�������Ϸ֮��
	private boolean IsInGame = false ;
	//�Ƿ��½
	private boolean IsLoginIn = false ;
	//�Ƿ����ڲ鿴����
	private boolean IsSeeingRoom = false;
	//����ķ������
	private Room EnterRoomObject = null;
	//�Ƿ��Ƿ���Ĵ�����
	private boolean IsRoomCreater = false;
	//�Ƿ��Ѿ�׼���ã�ֻ����ڷ���Ĳμ���
	private boolean IsReady =false;
	//ʹ�����ӵ�ID
	//IDΪ����ÿ��Ĭ�ϵ�����
	private int UseChessID = 0 ;
	//���봦�����
	private InputDisposeObj IDO = null;
	//�Ƿ�׼�������壬Ҳ�����Ƿ��ֵ����û�
	private boolean IsReadyToChess = false ;
	
	public User(Socket connection , ObjectInputStream iStream ,
			ObjectOutputStream oStream)
	{
		ConnectWithClient = connection ;
		IS = iStream ;
		OS = oStream;
		IDO = new InputDisposeObj(IS, this , this);
		
		//�����߳�ִ��
		FiveChessMainCode.TPExecutor.execute(IDO);
	}

	//û�������߳���ִ�� , ����ִ�к�ʱ����
	@Override
	public void RecieveData(ProtocolObject obj)
	{
		//�����˼�BREAK
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
	
	//�ͻ��˵�¼
	private void LoginIn(LoginInInformation information)
	{
		UserBaseModel model = FiveChessMainCode.UserDBManager.UseNameToFindUser(information.Name) ;
		ChessOrIconImage iconImage = null ;
		
		
		//�գ��򴴽�����ӵ����ݿ�
		if(model == null)
		{
			
			Random random = new Random() ;
			
			//���ͷ��
			iconImage = FiveChessMainCode.SetManager.AllIconImage.get(
					random.nextInt(FiveChessMainCode.SetManager.AllIconImage.size())) ;
			
			model = new UserBaseModel(information.Name, 0, 0, 0, null, iconImage.getID() , information.MachineID) ;
			
			//��Ӳ��ɹ�
			if(!FiveChessMainCode.UserDBManager.AddAUserBaseRecord(model))
			{
				ProtocolObject obj = new ProtocolObject() ;
				//��ʶ����Ϊ��¼
				obj.CommandType = CommandTypeEnum.LoginIn;
				obj.Result = ResultEnum.DisposeFailed ;
				//��Ӵ�����Ϣ
				obj.Data = "�������û�ʧ��" ;
				
				//����ʧ����Ϣ
				SendData(obj) ;
				return ;
			}
		}
		//��ȡ��Ӧicon����
		else
		{
			//machine id �������
			//Ҳ���ǻ�����user name�������
			if(!model.MachineID.equals(information.MachineID))
			{
				ProtocolObject obj = new ProtocolObject() ;
				//��ʶ����Ϊ��¼
				obj.CommandType = CommandTypeEnum.LoginIn;
				obj.Result = ResultEnum.DisposeFailed ;
				//��Ӵ�����Ϣ
				obj.Data = "���û����ѱ�ʹ��" ;
				
				//����ʧ����Ϣ
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
		
		
		//�����û���Ϣ
		Information = model ;
		
		ProtocolObject obj = new ProtocolObject() ;
		//��ʶ����Ϊ��¼
		obj.CommandType = CommandTypeEnum.LoginIn;
		//��������
		obj.Data = iconImage;
		obj.Result = ResultEnum.DisposeSucceed ;
		
		//������Ϣ
		SendData(obj) ;
		
		//��ʶΪ�Ѿ���¼
		IsLoginIn = true;
		
		return;
	}
	
	//�ڿͻ��˵õ������еķ�����Ϣ֮�����
	//����EnterRoomSelectW ��Ҫ�� SeeRoom֮�����
	private void EnterRoomSelectW()
	{
		ProtocolObject obj = new ProtocolObject() ;
		
		IsSeeingRoom = true ;
		
		//��������
		obj.CommandType = CommandTypeEnum.EnterSelectRoomW ;
		obj.Result = ResultEnum.DisposeSucceed ;
				
		//������Ϣ
		SendData(obj) ;
	}
	
	//�뿪����ѡ�񴰿�
	private void CancelRoomSelectW()
	{
		ProtocolObject obj = new ProtocolObject() ;
		
		IsSeeingRoom = false ;
		
		//��������
		obj.CommandType = CommandTypeEnum.EnterSelectRoomW ;
		obj.Result = ResultEnum.DisposeSucceed ;
				
		//������Ϣ
		SendData(obj) ;
		
		System.out.println("Cancel Room Select W");
	}
	
	//�鿴���еķ��� �� δ����
	private void SeeRoom()
	{
		List<RoomInformation> data = new LinkedList<>() ;
		
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.SeeRoom;
		
		//������з��������
		for(int counter = 0 ; counter < FiveChessMainCode.TotalActiveRooms.size() ; counter++)
		{
			data.add(FiveChessMainCode.TotalActiveRooms.get(counter).GetInformation());
		}
		
		object.Data = data ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		SendData(object);
	}
	
	//��������
	private void CreateRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.CreateRoom ;
		
		//û�е�¼��ֱ�ӷ��ش���
		//�Ѿ����˷��䣬ֱ�ӷ��ش���
		if(!IsLoginIn || EnterRoomObject != null)
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		else
		{
			//�����µķ���
			Room room = new Room(this, FiveChessMainCode.SetManager.getChessTableRC()) ;
			//��ӵ������ܼ�
			FiveChessMainCode.TotalActiveRooms.add(room) ;
			
			//��ʾΪ�Ƿ��䴴����
			IsRoomCreater = true ;
		
			//�Ѿ������˷���
			IsEnterRoom = true;
			
			//��ֵ�������
			EnterRoomObject = room;
			
			object.Data = room.GetHashID();
			object.Result = ResultEnum.DisposeSucceed ;
		}
		
		SendData(object);
	}
	
	//�û�������Ʒ
	private void BuyGoods(int goodsId)
	{
		ChessInformationM goodsInformation = null ;
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.BuyGoods ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		//Ѱ�һ�����Ϣ
		for (ChessInformationM chess : FiveChessMainCode.SetManager.AllChessImage) 
		{
			if(chess.getID() == goodsId)
			{
				goodsInformation = chess;
			}
		}
		
		//û���ҵ���Ʒ������û�е�¼��ֱ�ӷ���ʧ��
		if(goodsInformation == null || !IsLoginIn)
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		//�ҵ�����Ʒ
		else
		{
			//��Ǯ���㣬����ʧ��
			if(Information.Money < goodsInformation.getPrice())
			{
				object.Result = ResultEnum.DisposeFailed ;
			}
			//��Ǯ�㹻
			else
			{
				int originalMoney = Information.Money ;
				UserAndGoodsModel buyRecord = new UserAndGoodsModel() ;
					
				buyRecord.GoodsId = goodsId ;
				buyRecord.UserId = Information.Id ;
				
				//д����Ʒ��¼ʧ��
				//ֱ�ӷ��ش���
				if(!FiveChessMainCode.UserDBManager.AddAUserAndGoodsRecord(buyRecord))
				{
					object.Result = ResultEnum.DisposeFailed ;
				}
				//д����Ʒ�����¼�ɹ�
				else
				{
					Information.Money = Information.Money - goodsInformation.getPrice() ;
					
					//д�����ݱ�ʧ�ܣ�Ĩ���޸ģ�����ԭֵ
					//������Ʒ��¼�Ѿ�д��
					//�ʶ���ʱ��ֻ�ܹ����û���ѹ�����һ����Ʒ
					if(!FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information))
					{
						Information.Money = originalMoney ;
					}
				}
			}
		}
		
		//��������
		this.SendData(object);
	}
	
	//�鿴���е�Icons
	private void SeeAllIcons()
	{	
		
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = FiveChessMainCode.SetManager.AllIconImage;
		object.CommandType = CommandTypeEnum.SeeAllIcons ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	//�鿴��������
	private void SeeAllChesss()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.Data = FiveChessMainCode.SetManager.AllChessImage;
		object.CommandType = CommandTypeEnum.SeeAllChesss ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	//�ı��û�ͷ��
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
		
		//��¼+Ѱ�ҵ���Ӧ����Ϣ��Ҳ������������ȷ��ID
		if(IsLoginIn || isGetMatchInformation)
		{
			//����ԭʼ��ICON ID
			originalIconId = Information.IconID ;
			
			//����ͷ��ID
			Information.IconID = iconId ;
			result = FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information) ;
			
			//���ݿ�ɹ��򷵻�ִ�гɹ�
			if(result)
			{
				object.Result = ResultEnum.DisposeSucceed ;
			}
			//���ݿ����ʧ����ָ�ICON ID �� ���ش���
			else
			{
				//�ָ�Ϊԭ����ICON ID
				Information.IconID = originalIconId ;
				object.Result = ResultEnum.DisposeFailed ;
			}
		}
		//û�е�¼��û���ҵ���Ϣ�����޷��޸��û�ICON ID��ֱ�ӷ��ش���
		else
		{
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		
		this.SendData(object);
	}
	
	//�û���鿴�Լ�����ϸ��Ϣ
	private void SeeMineInformation()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		
		//û�е�¼����ֱ�ӷ��ش���
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
		
		//�����̷߳������ݵ��ͻ���
		this.SendData(object);
	}
	
	//ѡ��ʹ�õ�����
	private void ChooseUseChess(int chessId)
	{
		boolean isFind = false ;
		
		ProtocolObject object = new ProtocolObject();
		
		//����Ѱ�Ҷ�ӦID�Ƿ����
		for (ChessOrIconImage chess : FiveChessMainCode.SetManager.AllChessImage) 
		{
			if(chess.getID() == chessId)
			{
				isFind = true ;
			}
		}
		
		//�ҵ���
		if(isFind)
		{
			object.CommandType = CommandTypeEnum.ChangeUserIcon;
			object.Result = ResultEnum.DisposeSucceed ;
			
			UseChessID = chessId ;
		}
		//���û���ҵ� �� ���ı������ֵ
		else
		{
			object.CommandType = CommandTypeEnum.ChangeUserIcon;
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		this.SendData(object);
	}

	//�ҹر����Լ��ķ��� , δ����
	private void ICloseMRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.ICloseMRoom ;
		
		//ֻ�м����˷��䲢�ҷ���������
		//���ҷ������Լ�������ʱ��
		if(IsEnterRoom && EnterRoomObject != null && IsRoomCreater)
		{
			//�رշ���
			EnterRoomObject.CloseTheRoom();
			//������ñ�־λ
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
		
		//������Ϣ��ȥ
		SendData(object);
	}
	
	//��׼������ �� δ����
	private void GetReady()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.GetReady ;
		//�����˷���
		//���Ƿ��䴴����
		//�������Ϊ��
		//û��׼��
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

	//ȡ��׼��״̬��δ����
	private void CancelReady()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.CancelReady;
		
		//�����˷���
		//���Ƿ��䴴����
		//�������Ϊ��
		//׼������
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
	
	//���뷿��
	private void JoinRoom(String hashId) 
	{
		ProtocolObject object = new ProtocolObject() ;
		Room matchRoom = null ;
		
		object.CommandType = CommandTypeEnum.JoinRoom;
		
		//Ѱ�Ҷ�ӦID�ķ���
		for(int counter = 0 ; counter < FiveChessMainCode.TotalActiveRooms.size() ; counter++)
		{
			if(FiveChessMainCode.TotalActiveRooms.get(counter).GetHashID().equals(hashId))
			{
				matchRoom = FiveChessMainCode.TotalActiveRooms.get(counter) ;
				break ;
			}
		}
		
		//�ҵ��˷���
		//û�м��뷿��
		//���Ƿ��䴴����
		if(matchRoom != null && !IsEnterRoom && !IsRoomCreater)
		{	
			//�����п�λ����Ϸû�п�ʼ
			if(matchRoom.IsEmpty() && !matchRoom.isIsGameStart())
			{
				//���뷿��ɹ�
				if(matchRoom.Join(this))
				{
					IsEnterRoom = true ;
					IsReady = false ;
					EnterRoomObject = matchRoom ;
					
					UseChessID = 0 ;
					
					//���ͻ��˷�������Ϣ
					object.Data = matchRoom.GetCInformation() ;
					
					object.Result = ResultEnum.DisposeSucceed ;
				}
				//���뷿��ʧ��
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
	
	//�뿪����
	private void CancelRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.CancelRoom;
		//�����˷���
		//���Ƿ��䴴����
		//�������Ϊ��
		if(IsEnterRoom && !IsRoomCreater && EnterRoomObject != null)
		{
			//֪ͨ�������Ѿ��뿪
			EnterRoomObject.Cancel() ;
			//��ԭ�����ͱ�־
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
	
	//��Ϸ��ʼ��ֻ���ɷ��䴴���ߵ���
	private void StartGame()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.StartGame ;
		
		if(IsEnterRoom && IsRoomCreater && EnterRoomObject != null)
		{
			//��ʼ��Ϸ�ɹ�
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
	
	//�û���
	private void IPlayPosition(ChessPosition position)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.IPlayPosition ;
		
		//������Ϸ��
		//ӵ�������Ȩ��
		if(IsInGame && IsReadyToChess)
		{
			//������ɹ�
			if(EnterRoomObject.PlayChess(this, position.X, position.Y))
			{
				//����û�������Ȩ����
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
	
	//����Լ����ж�ս��Ϣ��δ����
	private void GMAllResult()
	{
		ProtocolObject object = new ProtocolObject() ;
		boolean isSucceed = true ; 
		List<GameResultInformation> informations = null ;
		
		object.CommandType = CommandTypeEnum.GMAllResult ;
		
		//��½�ɹ�
		//Ȼ��ɹ���ѯ��������
		if(IsLoginIn && (informations = FiveChessMainCode.GameDBManager.GetTotalResult(Information.Id)) != null)
		{
			for(int counter = 0 ; counter < informations.size() ; counter++)
			{
				//��ö���ģ����Ϣ
				UserBaseModel model = FiveChessMainCode.UserDBManager.UseIDToFindUser(informations.get(counter).OpponentId) ;
				
				//û�л�ȡ����ֱ�ӷ��ش���
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
		
		//����ɹ���װ������
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
	//�û��Ͽ����ӣ�ֱ�ӽ��û�ɾ���ڻ�Ծ�û��б�֮��
	@Override
	public void ClientLeave()
	{
		//ֻ�е�¼�˵��˲Ż�ȥ�޸�����¼��ʱ��
		if(IsLoginIn)
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			//���û��Ƴ���Ծ�û��б�
			FiveChessMainCode.TotalActiveUsers.remove(this) ;
			//��������Ϸ�����Լ���Ϸ�����˳�
			//�޸�������ʱ��
			Information.LastEnterTime = df.format(new Date());
			//�޸����ݣ����������ݿ�֮��,��������
			FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information);
			
		}
		
		if(IsEnterRoom)
		{
			//����Ǵ����߲��ҷ��䲻Ϊ��
			//ֱ�ӹرշ���
			if(IsRoomCreater && EnterRoomObject != null)
			{
				EnterRoomObject.CloseTheRoom(); 
			}
			
			//�ǲ�����
			//�˳�����
			if(!IsRoomCreater && EnterRoomObject != null)
			{
				EnterRoomObject.Cancel();
			}
		}
				
		//�ر��Լ�
		this.Close();
	}

	//�������ݣ�����ͬ�� �� �첽����
	private void SendData(ProtocolObject obj) 
	{
		FiveChessMainCode.TPExecutor.execute(new Runnable() 
		{
			
			@Override
			public void run() 
			{
				//��ס����������
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

	//�鿴�����Լ�������������Ϣ
	//���Ϊ�������ÿ���û�Ĭ�϶�ӵ��
	//����֪ͨ
	//δ����
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
			
			//��ѯʧ�ܣ�ֱ�ӷ��ش���
			if(mCID == null)
			{
				object.Result =ResultEnum.DisposeFailed ;
			}
			else
			{
				for(int counter = 0 ; counter < mCID.size() ; counter++)
				{
					//�����б�Ѱ�Ҷ�ӦID����Ϣ
					for (ChessInformationM information : FiveChessMainCode.SetManager.AllChessImage)
					{
						//Ѱ�ҵ�������ӵ�data��
						if(information.getID() == mCID.get(counter))
						{
							mChesss.add(information) ;
						}
					}
				}
				
				//�������
				object.Data = mChesss ;
			}
		}
		//û�е�¼��ֱ�ӷ��ش���
		else
		{
			object.Result =ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
//------------------------------------------------------------------------------
	
	/**
	 * ��ȡ�û���Ϣ
	 * @return
	 */
	public UserBaseInformation GetUserInformation()
	{
		return UserBaseModel.GetSendInstance(Information);
	}
	
	/**
	 * �Ƿ��Ѿ�׼����
	 * @return
	 */
	public boolean isIsReady() {
		return IsReady;
	}

	/**
	 * ��Ϸ��ʼ
	 * @param isOriginalActive ��ʼ״̬�Ƿ��ǿ�������
	 * @param ����ʹ�õ������ļ�����ͨ���ļ���
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
	 * ��Ϸ�ڿ�ʼ֮�����ʧȥ�����ӣ�֪ͨ��,���Ƿ��䴴����
	 */
	public void OpponentOutOfConnection()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//������Ϣ���
		IsEnterRoom = false ;
		EnterRoomObject = null ;
		//׼��״̬��ԭ
		IsReady = false ;
		IsInGame = false ;
		
		object.CommandType = CommandTypeEnum.OpponentOutOfConnection ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	/**
	 * ���Ѿ�ʤ��
	 * @param winMoney Ӯ�õĽ�Ǯ��
	 */
	public void YouWin(int winMoney)
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//׼��״̬��ԭ
		//��Ϸ״̬��ԭ
		IsReady = false ;
		IsInGame = false ;
		UseChessID = 0 ;
		
		Information.Money += winMoney ;
		Information.WinTimes ++ ;
		Information.TotalGameTimes ++ ;
		
		//�������ݿ����ݳɹ�
		//���سɹ�����
		if(FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information))
		{
			object.Data = winMoney ;
			object.CommandType = CommandTypeEnum.YouWin ;
			object.Result = ResultEnum.DisposeSucceed ;
		}
		//�������ݿ�ʧ�ܣ����ش����ʾ����Ӯ�ˣ����Ƿ���˳����˴���
		//����û�б���
		else
		{
			object.CommandType = CommandTypeEnum.YouWin ;
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		
		SendData(object);
	}
	
	/**
	 * ������
	 */
	public void YouDefeat()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//׼��״̬��ԭ
		//��Ϸ״̬��ԭ
		IsReady = false ;
		IsInGame = false ;
		UseChessID = 0 ;
		
		Information.TotalGameTimes ++ ;
		
		//�������ݿ����ݳɹ�
		//���سɹ�����
		if(FiveChessMainCode.UserDBManager.UpDateAUserBaseRecord(Information))
		{
			object.CommandType = CommandTypeEnum.YouDefeat ;
			object.Result = ResultEnum.DisposeSucceed ;
		}
		//�������ݿ�ʧ�ܣ����ش����ʾ���������ˣ����Ƿ���˳����˴���
		//����û�б���
		else
		{
			object.CommandType = CommandTypeEnum.YouDefeat ;
			object.Result = ResultEnum.DisposeFailed ;
		}
		
		SendData(object);
	}
	
	
	/**
	 * �����û������Ȩ��
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
	 * ���䱻�����߹ر���
	 * ֪ͨ���ַ��䱻�ر�
	 */
	public void MJRoomClosed()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		//������Ϣ���
		IsEnterRoom = false ;
		EnterRoomObject = null ;
		//׼��״̬��ԭ
		IsReady = false ;
		IsInGame = false ;
		
		object.CommandType = CommandTypeEnum.MJRoomClosed ;
		object.Result = ResultEnum.DisposeSucceed ;
		
		this.SendData(object);
	}
	
	/**
	 * һ�����䱻�ر��ˣ�ֻ�����ڲ鿴������û��Żᱻ����
	 * @param hashId���رշ����HASH ֵ
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
	 * һ���������Ϸ�Ѿ������ˣ�ֻ�����ڲ鿴������û��Żᱻ����
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
	 * ��������
	 * �ڸ�������Ȩ��֮ǰ����
	 * @param x �����µ�X����
	 * @param y �����µ�Y����
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
	 * �µķ��䱻�����ˣ�ֻ�����ڲ鿴������û��Żᱻ����
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
	 * �����Ƿ��ȱ״̬���ı䣬ֻ�����ڲ鿴������û��Żᱻ����
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
	 * ����������Ƿ�׼������
	 * USERΪ������
	 * @param isReady �Ƿ�׼����
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
	 * ������Ϸ��ʼ��֪ͨ���ڲ鿴�������
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
	 * ���ּ����˷���
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
	 * �����뿪�˷���
	 */
	public void OpponentCancelRoom()
	{
		ProtocolObject object = new ProtocolObject() ;
		
		object.CommandType = CommandTypeEnum.OpponentCancelRoom;
		object.Result = ResultEnum.DisposeSucceed ;
		
		SendData(object);
	}
	
	/**
	 * �û��Ƿ����ڲ鿴����
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
	
	//����ڷ���������ʾ���ַ�
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
