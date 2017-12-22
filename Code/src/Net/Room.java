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
	 * ��ĳ��λ�õ���������
	 * @author Laoyao
	 *
	 */
	private enum InPositionChessE
	{
		/**
		 * creater �����������λ��
		 */
		C ,
		/**
		 * opponent �����������λ��
		 */
		O ,
		/**
		 * ���λ��Ϊ��
		 */
		N
	}
	
	/**
	 * ��������
	 * ��һά��Ϊ�У��ڶ�ά��Ϊ��
	 */
	private InPositionChessE[][] TableChess = null;
	//���̵ĳ���
	private int TableWAH = 0;
	//��Ϸ�ߵ���Ŀ
	private int GamerNumber = 0 ;
	//��Ϸ�Ƿ�ʼ
	private boolean IsGameStart = false ;
	//���ڻ�Ծ�ŵ��û���Ҳ�������ڿ���������û�
	private User NowActiveUser = null;
	//�Ƿ��Ǵ����߻�Ծ
	private boolean IsCreaterActive = false ;
	//��Ϸ��ʼ��ʱ��
	private String GameStartTime = null;
	
	public Room(User creater , int tableWAH) 
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		Creater = creater;
		TableWAH = tableWAH ;
		GamerNumber++ ;
		
		TableChess = new InPositionChessE[TableWAH][TableWAH] ;
		
		//��ʼ����������λ��Ϊ��
		for(int counterR = 0 ; counterR < TableWAH ; counterR++)
		{
			for(int counterC = 0 ; counterC < TableWAH ; counterC++ )
			{
				TableChess[counterR][counterC] = InPositionChessE.N;
			}
		}
		
		//����Ψһ��ROOM ID
		HashID = StringDispose.GetMD5(creater.GetUserInformation().Id +
				creater.GetUserInformation().IconID + df.format(new Date()) +
				(new Random()).nextDouble()) ;
		
		//֪ͨ�������ڲ鿴���û����·��䱻����
		for (User user : FiveChessMainCode.TotalActiveUsers) 
		{
			if(user.isIsSeeingRoom())
			{
				user.NewRoomCreated(this.GetInformation());
			}
		}
	}
	
	/**
	 * ֪ͨ����Ĵ����߶��ֵ�׼��״̬�����˸ı�
	 * @param isReady
	 */
	public void OpponentReadyStateChange(boolean isReady)
	{
		Creater.OpponentReadyState(isReady);
	}
	
	/**
	 * ���뷿���ս,�����ڲ�����
	 * @param opponent
	 */
	public synchronized boolean Join(User opponent)
	{
		//��������ȴ��Ҫǿ�м��룬ֱ�ӷ��ش���
		if(GamerNumber >= 2)
		{
			return false ;
		}
		
		Opponent = opponent ;
		
		//֪ͨ�����߶����Ѿ��ҵ�
		Creater.OpponentJoinRoom(opponent.GetUserInformation());
		//������Ϸ����
		GamerNumber++ ;
		
		//֪ͨ�������ڲ鿴���û���������
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
	 * �����뿪�������ڲ�����
	 * �����û�п�ʼ��Ϸ֪ͨ���ڲ鿴�����б���˷����ѿ�
	 * ����Ѿ���ʼ����Ϸ����ֱ�ӹرշ���
	 */
	public void Cancel()
	{
		//�����Ϸ�Ѿ���ʼ����֪ͨ�����߶���ʧȥ������
		//�����Զ��ر�
		if(IsGameStart)
		{
			Opponent = null ;
			//֪ͨ�����߶����Ѿ�ʧȥ�����ӣ�����ر�
			Creater.OpponentOutOfConnection();
			
			//�رշ���
			CloseTheRoom() ;
		}
		else
		{
			Opponent = null ;
			
			//֪ͨ�����߶����Ѿ��뿪����
			Creater.OpponentCancelRoom();
			
			//��Ϸ��ʼ
			GamerNumber-- ;
			
			//֪ͨ�������ڲ鿴���û������ѿ�
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
	 * ��÷���ΨһHASH IDֵ
	 * @return
	 */
	public String GetHashID()
	{
		return HashID;
	}
	
	/**
	 * ��Ϸ��ʼ
	 * @param commander 
	 * @return
	 */
	public boolean GameStart(User commander)
	{
		//ȷ�����д����߷���
		//ȷ���ж���
		//ȷ�϶����Ѿ�׼����
		if(commander.GetUserInformation().Id == Creater.GetUserInformation().Id
				&& GamerNumber == 2 && Opponent.isIsReady())
		{
			IsGameStart = true ;
			int opponetChessId = Opponent.getUseChessID()  , createrChessId = Creater.getUseChessID();
			String opponentPath = null , createrPath = null ;
			
			//Ѱ�Ҷ��ֵ������ļ���
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
			
			//֪ͨ������Ϸ��ʼ
			//�����Ͷ����������˭����
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
			
			//�������ڸ�ʽ
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//������Ϸ��ʼʱ��
			GameStartTime = df.format(new Date()) ;
			
			//֪ͨ�������ڲ鿴���û��з��䱻�ر���
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

	//�����������
	//���������֮�����
	private void ExchangeActiveUser()
	{
		//�����ǰ�Ǵ����߻�Ծ�����л�Ϊ����
		if(IsCreaterActive)
		{
			IsCreaterActive = false ;
			NowActiveUser = Opponent ;
		}
		//�����ǰ�Ƕ��ֻ�Ծ�����л�Ϊ������
		else
		{
			IsCreaterActive = true ;
			NowActiveUser = Creater ;
		}
	}
	/**
	 * ��Ϸ�Ƿ��Ѿ���ʼ
	 * @return
	 */
	public boolean isIsGameStart() {
		return IsGameStart;
	}
	
	/**
	 * ���䴴���������رշ���
	 * ֻ���ɴ����ߵ���
	 */
	public void CloseTheRoom()
	{
		//�Ƴ��÷���
		FiveChessMainCode.TotalActiveRooms.remove(this) ;
		
		if(Opponent != null)
		{
			//֪ͨ�����߷��䱻�ر���
			Opponent.MJRoomClosed();
		}
		
		//֪ͨ�������ڲ鿴���û��з��䱻�ر���
		for (User user : FiveChessMainCode.TotalActiveUsers) 
		{
			if(user.isIsSeeingRoom())
			{
				user.ARoomClosed(HashID);
			}
		}
	}
	
	/**
	 * ��ô����ߵ���Ϣ
	 * @return
	 */
	public UserBaseInformation GetCInformation()
	{
		return Creater.GetUserInformation() ;
	}
	
	/**
	 * �Ƿ���λ�ü���
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
 	 * ���� , ͬ������ 
 	 * @param commander�����������
 	 * @param indexX��������X �� ��0��ʼ
 	 * @param indexY��������Y �� ��0��ʼ
 	 * @return
 	 */
 	public synchronized boolean PlayChess(User commander  , int indexX , int indexY)
 	{
 		InPositionChessE newChess  ;
 		//������������߲��ǵ�ǰ��Ծ�û�
 		//�������ӳ����˷�Χ
 		//���ش���
 		if(commander.GetUserInformation().Id != NowActiveUser.GetUserInformation().Id ||
 				indexX >= TableWAH || indexY >= TableWAH)
 		{
 			return false ;
 		}
 		
 		//��ǰλ���Ѿ���������
 		if(TableChess[indexY][indexX] != InPositionChessE.N)
 		{
 			return false ;
 		}
 		
 		//�����ǰ��Ծ���Ǵ�����
 		if(IsCreaterActive)
 		{
 			TableChess[indexY][indexX] = InPositionChessE.C ;
 			newChess = InPositionChessE.C ;
 			
 			Opponent.OpponentPlayedChess(indexX, indexY);
 		}
 		//�����ǰ��Ծ���Ƕ���
 		else
 		{
 			TableChess[indexY][indexX] = InPositionChessE.O ;
 			newChess = InPositionChessE.O ;
 			
 			Creater.OpponentPlayedChess(indexX, indexY);
 		}
 		
 		//��ǰ��һ�������µ�ʱ���Ƿ�5������Ӯ�ñ���
 		if(CalculateIsSomeOneWin(indexX, indexY, newChess))
 		{
 			int wId = 0 , dId = 0 ;
 			GameResultModel resultModel = null ;
 			
 			//�������ڸ�ʽ
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//������Ϸ��ʼʱ��
			String overTime = df.format(new Date()) ;
			
 			//������ʤ��
 			if(IsCreaterActive)
 			{
 				Creater.YouWin(FiveChessMainCode.SetManager.getAGameWM());
 				Opponent.YouDefeat();
 				
 				wId = Creater.GetUserInformation().Id ;
 				dId = Opponent.GetUserInformation().Id ;
 			}
 			//����ʤ��
 			else
 			{
 				Opponent.YouWin(FiveChessMainCode.SetManager.getAGameWM());
 				Creater.YouDefeat();
 				
 				wId = Opponent.GetUserInformation().Id ;
 				dId = Creater.GetUserInformation().Id ;
 			}
 			
 			//����Ծּ�¼
 			//ʤ����ʧ���߸�����һ��
 			resultModel = new GameResultModel(wId, dId, overTime, GameStartTime, true) ;
 			FiveChessMainCode.GameDBManager.AddAGameResultRecord(resultModel) ;
 			
 			resultModel = new GameResultModel(dId, wId, overTime, GameStartTime, false) ;
 			FiveChessMainCode.GameDBManager.AddAGameResultRecord(resultModel) ;
 			
 			//֪ͨ�������ڲ鿴���û��з�����Ϸ������
 			for (User user : FiveChessMainCode.TotalActiveUsers) 
 			{
 				if(user.isIsSeeingRoom())
 				{
 					user.ARoomGameOver(HashID);
 				}
 			}
 		}
 		//û����ʤ���򽻻���Ծ�û�
 		else
 		{
 			//������Ծ�û�
 			ExchangeActiveUser();
 			//���µĻ�Ծ�û������Ȩ��                 
 			NowActiveUser.GiveRightToPlay();
 		}
 		
 		return true ;
 	}
 	
 	/**
 	 * �����Ƿ����˻�ʤ
 	 * @param newX �µ�����X����
 	 * @param newY �µ�����Y����
 	 * @param newChess �µ���������
 	 * @return ��ǰ������û��Ƿ�ʤ��
 	 */
 	private boolean CalculateIsSomeOneWin(int newX , int newY , InPositionChessE newChess)
 	{
 		//�Լ���һ������
 		int sameChessNumber = 0 ;
 		int cx = 0 , cy = 0 ;
 		
 		//���ߣ������� , �������Ӽ����ȥ��
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
 		
 		//���ߣ������� ,û�м���������
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
 		
 		//���ߣ������� , �������Ӽ����ȥ��
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
 		
 		//���ߣ������� , û�н������Ӽ����ȥ��
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
 		//45�Ƚǣ������Ͻ��ߣ�������������
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
 		//45�Ƚǣ������½��ߣ�û�м���������
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
 		//135�Ƚǣ������Ϸ��ߣ�������������
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
 		//135�Ƚǣ������·��ߣ�û�м���������
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
 		
 		//�ĸ�����ȫ������˶�û��5�����ߣ�GG
 		return false ;
 	}
 	
	//��÷�����Ϣ
	public RoomInformation GetInformation()
	{
		if(GamerNumber == 1)
		{
			return new RoomInformation(Creater.GetUserInformation(), HashID, true , IsGameStart) ;
		}
		//λ������������û�пռ�
		else
		{
			return new RoomInformation(Creater.GetUserInformation(), HashID, false , IsGameStart) ;
		}
		
	}

	//��ȡ���ڷ���������ʾ����Ϣ
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
