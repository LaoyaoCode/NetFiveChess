package NetObjModel;

public class GameResultInformation 
{
	/**
	 * 对手的ID
	 */
	public int OpponentId = 0 ;
	/**
	 * 对手的信息
	 */
	public UserBaseInformation OpponentInformation = null;
	/**
	 * 游戏结束时间
	 */
	public String FinishedTime = null ;
	/**
	 * 游戏开始时间
	 */
	public String BeginTime = null ;
	/**
	 * 是否胜利
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
