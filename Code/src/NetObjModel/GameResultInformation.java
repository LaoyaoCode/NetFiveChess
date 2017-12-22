package NetObjModel;

public class GameResultInformation 
{
	/**
	 * ���ֵ�ID
	 */
	public int OpponentId = 0 ;
	/**
	 * ���ֵ���Ϣ
	 */
	public UserBaseInformation OpponentInformation = null;
	/**
	 * ��Ϸ����ʱ��
	 */
	public String FinishedTime = null ;
	/**
	 * ��Ϸ��ʼʱ��
	 */
	public String BeginTime = null ;
	/**
	 * �Ƿ�ʤ��
	 */
	public boolean IsWin = false ;
	
	public GameResultInformation(int id , UserBaseInformation opponent, String finishedTime, String beginTime, boolean isWin) {
		OpponentId = id ;
		OpponentInformation = opponent;
		FinishedTime = finishedTime;
		BeginTime = beginTime;
		IsWin = isWin;
	}
	
	public GameResultInformation() {
		this(0, null, null, null, false) ;
	}
}
