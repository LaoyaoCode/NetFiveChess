package DataBase;

public interface IDataBase 
{
	/**
	 * �����Լ���ID
	 * @param id
	 */
	void SetMID(int id) ;
	/**
	 * ��ȡ�����Լ���SQL���
	 * @return
	 */
	String GetMUpdateStatement() ;
	/**
	 * ��ȡ����Լ���SQL���
	 * @return
	 */
	String GetMAddStatement() ;
	/**
	 * ��ȡɾ���Լ���SQL���
	 * @return
	 */
	String GetMDeleteStatement() ;
}
