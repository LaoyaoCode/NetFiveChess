package NetObjModel;

import java.io.Serializable;

public class ChessPosition implements Serializable
{
	/**
	 * 棋子的X坐标
	 */
	public int X = 0 ;
	/**
	 * 棋子的Y坐标
	 */
	public int Y = 0 ;
	
	
	public ChessPosition(int x, int y) {
		super();
		X = x;
		Y = y;
	}
	
	public ChessPosition()
	{
		this(0 ,  0 ) ;
	}

	@Override
	public String toString() {
		return "ChessPosition [X=" + X + ", Y=" + Y + "]";
	}
	
	
}
