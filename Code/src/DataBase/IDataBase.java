package DataBase;

public interface IDataBase 
{
	/**
	 * 设置自己的ID
	 * @param id
	 */
	void SetMID(int id) ;
	/**
	 * 获取更新自己的SQL语句
	 * @return
	 */
	String GetMUpdateStatement() ;
	/**
	 * 获取添加自己的SQL语句
	 * @return
	 */
	String GetMAddStatement() ;
	/**
	 * 获取删除自己的SQL语句
	 * @return
	 */
	String GetMDeleteStatement() ;
}
