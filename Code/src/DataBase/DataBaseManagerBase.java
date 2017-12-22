package DataBase;
import java.sql.Connection;  
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.sql.Statement;

import MainPack.ErrorRecordManager.ErrorRecord;
import MainPack.FiveChessMainCode; 

public class DataBaseManagerBase 
{
	//��ȻSqlite֧�ֶ��߳����Ӷ�ȡ������ֻ֧�ֵ��̵߳�����д��
	//���Ƕ�ͬһ������Ķ�дȴ�����ڲ��Ѿ�ͬ����
	//�ʶ���������һ�����ݿ�ֻʹ��һ�����Ӷ���
	//�����ٿ������ݿ���߳�ͬ������
	private String DBConnectCommand = null;
	protected boolean IsSucceed = false ;
	protected Connection DBConnection = null;
	
	/**
	 * 
	 * @param filePath ���ݿ��ļ���
	 * @param createInsideTableStatement �����ݿ��ļ������ڵ�ʱ�򴴽����ݿ���SQL���
	 */
	public DataBaseManagerBase(String filePath , String[] createInsideTableStatements)
	{		
		//�����趨�ַ����룬������֮ǰ�趨���ַ����뷽ʽ�Ǵ��
		DBConnectCommand = "jdbc:sqlite:" + filePath;
		
		try
		{
			DBConnection = DriverManager.getConnection(DBConnectCommand) ;
			//�������
			Statement dBStatement = DBConnection.createStatement() ;
			
			//�����Ƿ���ڣ��������򴴽�
			for (String  statement: createInsideTableStatements) 
			{
				//ִ�б�����䣬(if not exists)
				dBStatement.execute(statement);
			}
			
			//�ر��������
			dBStatement.close();

			IsSucceed = true ;
		}
		catch (Exception e) 
		{
			//��ʼ���׶�ֱ���������
			System.out.println("Error Message:" + e.getMessage());
		}
	}
	
	/**
	 * �Ƿ��Ѿ��ɹ�ͨ�����Ӳ���
	 * @return
	 */
	public boolean isIsSucceed() {
		return IsSucceed;
	}
		
	/**
	 * ����һ����¼
	 * ���Գɹ���2017.10.12��laoyao
	 * @param record ��¼���
	 * @return �Ƿ���ӳɹ�
	 */
	protected boolean AddARecord(IDataBase record)
	{
		PreparedStatement dbStatement = null ;
		ResultSet resultSet = null ;
		
		try 
		{
			DBConnection = DriverManager.getConnection(DBConnectCommand) ;
			dbStatement = DBConnection.prepareStatement(record.GetMAddStatement() , 
					PreparedStatement.RETURN_GENERATED_KEYS) ;
			dbStatement.executeUpdate() ;
			resultSet = dbStatement.getGeneratedKeys();
			
			//���������
			if(resultSet.next())
			{
				//���ü�¼��ΨһID
				record.SetMID(resultSet.getInt(1));
				
				resultSet.close();
				dbStatement.close();
				
				return true ;
			}
			else
			{
				resultSet.close();
				dbStatement.close();

				return false;
			}
		} 
		catch (Exception e) 
		{
			ErrorRecord errorRecord = new ErrorRecord(e.getMessage()) ;
			errorRecord.SetCodePostion(getClass().getName(), "AddARecord");
			
			FiveChessMainCode.ErrorRManager.AddRecord(errorRecord);
			
			return false ;
		}
	}
	
	/**
	 * ����һ����¼
	 * ���Գɹ������������ַ����������д������2017.10,12��laoyao
	 * @param record ��¼�ӿ�
	 * @return
	 */
	protected boolean UpDateARecord(IDataBase record)
	{
		Statement dbStatement = null ;
		int disposeCount = 0 ;
		
		try
		{
			dbStatement = DBConnection.createStatement();
			//ִ��SQL���
			disposeCount = dbStatement.executeUpdate(record.GetMUpdateStatement()) ;
			
			//�ر�����
			dbStatement.close();
			
			if(disposeCount == 0)
			{
				return false ;
			}
			else
			{
				return true ;
			}
		}
		catch(Exception e)
		{
			ErrorRecord errorRecord = new ErrorRecord(e.getMessage()) ;
			errorRecord.SetCodePostion(getClass().getName(), "UpdataARecord");
			
			FiveChessMainCode.ErrorRManager.AddRecord(errorRecord);
			return false ;
		}
	}

	/**
	 * ɾ��һ����¼
	 * @param record ��¼���
	 * @return
	 */
	protected boolean DeleteARecord(IDataBase record)
	{
		Statement dbStatement = null ;
		int disposeCount = 0 ;
		
		try
		{
			dbStatement = DBConnection.createStatement();
			//ִ��SQL���
			disposeCount = dbStatement.executeUpdate(record.GetMDeleteStatement()) ;
			
			//�ر�����
			dbStatement.close();
			
			if(disposeCount == 0)
			{
				return false ;
			}
			else
			{
				return true ;
			}
		}
		catch(Exception e)
		{
			ErrorRecord errorRecord = new ErrorRecord(e.getMessage()) ;
			errorRecord.SetCodePostion(getClass().getName(), "DeleteARecord");
			
			FiveChessMainCode.ErrorRManager.AddRecord(errorRecord);
			return false ;
		}
	}
	
	public void Close()
	{
		try 
		{
			DBConnection.close();
		}
		catch (Exception e) 
		{
			// TODO: handle exception
		}
		
	}
}
