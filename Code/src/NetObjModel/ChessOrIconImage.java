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
	 * ��ȡͼƬID	
	 * @return
	 */
	public int getID() {
		return ID;
	}

	/**
	 * ��ȡ�ļ���
	 * @return
	 */
	public String getName() {
		return Name;
	}
	
	/**
	 * ��ȡ�ļ�����
	 * @return
	 */
	public String getDes() {
		return Des ;
	}
}
