package DataBase;

import java.io.Serializable;

public class DataBaseObject implements IDataBase , Serializable
{
	protected String BelongTableName = null;
	
	/**
	 * 
	 * @param tableName 从属的表名
	 */
	public DataBaseObject(String tableName) 
	{
		BelongTableName = tableName ;
	}
	
	@Override
	public void SetMID(int id) 
	{
		
	}

	@Override
	public String GetMUpdateStatement() 
	{
		
		return null;
	}

	@Override
	public String GetMAddStatement() 
	{
		
		return null;
	}

	@Override
	public String GetMDeleteStatement() 
	{
		
		return null;
	}
}
