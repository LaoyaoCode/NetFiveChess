package MainPack;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorRecordManager
{
	/**
	 * ��ʽ���ַ���
	 */
	private final static String FormatString = "<EM:%s>#<CP:%s>#<T:%s>\n" ;
	private String RecordFilePath = null;
	private boolean Succeed = true ;
	
	public static class ErrorRecord
	{
		/**
		 * <Exception Message>
		 * �����л��з���<>
		 */
		public String ExceptionMessage = null;
		/**
		 * ����λ�� <ClassName+MethodName>
		 * �����л��з���<>
		 */
		public String CodePositionMessage = null;
		/**
		 * ����ʱ�� <yyyy-MM-dd HH:mm>
		 * �����л��з���<>
		 */
		public String Time = null;
		

		public ErrorRecord(String exceptionMessage, String codePositionMessage, String time) 
		{
			ExceptionMessage = exceptionMessage;
			CodePositionMessage = codePositionMessage;
			Time = time;
		}
		
		/**
		 * Time�Ѿ��Զ�����
		 */
		public ErrorRecord(String exceptionMessage, String codePositionMessage) 
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			ExceptionMessage = exceptionMessage;
			CodePositionMessage = codePositionMessage;
			Time = df.format(new Date());
		}
		
		/**
		 * Time�Ѿ��Զ�����
		 */
		public ErrorRecord(String exceptionMessage) 
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			ExceptionMessage = exceptionMessage;
			Time = df.format(new Date());
		}
		
		/**
		 * Time�Ѿ��Զ�����
		 */
		public ErrorRecord()
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Time = df.format(new Date());
		}
		
		/**
		 * ���ô���λ����Ϣ
		 * @param className ��������
		 * @param methodName ����������
		 */
		public void SetCodePostion(String className , String methodName)
		{
			CodePositionMessage = className + "+" + methodName ;
		}
		
	}

	public ErrorRecordManager(String filePath) 
	{
		File check = null ;
		RecordFilePath = filePath ;
		
		//����ļ��Ƿ���ڣ���������ֱ�Ӵ���
		check = new File(RecordFilePath) ;
		if(!check.exists())
		{
			try 
			{
				if(!check.createNewFile())
				{
					Succeed = false ;
				}
				
			} 
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				Succeed = false ;
			}
			
		}		
	}
	
	/**
	 * ��Ӵ����¼
	 * @param record �����¼
	 */
	public synchronized void AddRecord(ErrorRecord record) 
	{
		try
		{
			
			FileWriter writer = new FileWriter(RecordFilePath, true) ;
			
			writer.write(String.format(FormatString, record.ExceptionMessage , record.CodePositionMessage , record.Time));
			writer.close(); 
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ���еļ�¼
	 * �����������������ڷ������������е��ã�ֻ�ɹر�������������������
	 * @return ��¼�����
	 */
	public List<ErrorRecord> GetAllRecord()
	{
		List<ErrorRecord> records = new ArrayList<>();
		
		try
		{
			Scanner input = new Scanner(new File(RecordFilePath)) ;
			
			while (input.hasNext())
			{
				ErrorRecord record = new ErrorRecord();
				
				String recordLine = input.nextLine() ;
				recordLine = recordLine.replace("<", "") ;
				recordLine = recordLine.replace(">", "") ;
				recordLine = recordLine.replace("EM:", "") ;
				recordLine = recordLine.replace("CM:", "") ;
				recordLine = recordLine.replace("T:", "") ;
			    
				String[] datas = recordLine.split("#") ;
				record.ExceptionMessage = datas[0] ;
				record.CodePositionMessage = datas[1] ;
				record.Time = datas[2] ;
				records.add(record) ;
			}
			
			input.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return records;
	}

	public boolean isSucceed() {
		return Succeed;
	}
	
	
}
