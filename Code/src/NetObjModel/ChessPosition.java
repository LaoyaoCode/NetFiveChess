package NetObjModel;

import java.io.Serializable;

public class ChessPosition implements Serializable
{
	/**
	 * ���ӵ�X����
	 */
	public int X = 0 ;
	/**
	 * ���ӵ�Y����
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
