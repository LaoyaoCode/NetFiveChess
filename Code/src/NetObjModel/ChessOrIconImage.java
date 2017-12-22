package NetObjModel;

import java.io.Serializable;

public class ChessOrIconImage implements Serializable
{
	private int ID ;
	private String Name ;
	private String Des ;
	
	public ChessOrIconImage(int iD, String name , String des) 
	{
		ID = iD;
		Name = name;
		Des = des;
	}

	/**
	 * 获取图片ID	
	 * @return
	 */
	public int getID() {
		return ID;
	}

	/**
	 * 获取文件名
	 * @return
	 */
	public String getName() {
		return Name;
	}
	
	/**
	 * 获取文件描述
	 * @return
	 */
	public String getDes() {
		return Des ;
	}
}
