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
	 * 格式化字符串
	 */
	private final static String FormatString = "<EM:%s>#<CP:%s>#<T:%s>\n" ;
	private String RecordFilePath = null;
	private boolean Succeed = true ;
	
	public static class ErrorRecord
	{
		/**
		 * <Exception Message>
		 * 不可有换行符和<>
		 */
		public String ExceptionMessage = null;
		/**
		 * 代码位置 <ClassName+MethodName>
		 * 不可有换行符和<>
		 */
		public String CodePositionMessage = null;
		/**
		 * 发生时间 <yyyy-MM-dd HH:mm>
		 * 不可有换行符和<>
		 */
		public String Time = null;
		

		public ErrorRecord(String exceptionMessage, String codePositionMessage, String time) 
		{
			ExceptionMessage = exceptionMessage;
			CodePositionMessage = codePositionMessage;
			Time = time;
		}
		
		/**
		 * Time已经自动设置
		 */
		public ErrorRecord(String exceptionMessage, String codePositionMessage) 
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			ExceptionMessage = exceptionMessage;
			CodePositionMessage = codePositionMessage;
			Time = df.format(new Date());
		}
		
		/**
		 * Time已经自动设置
		 */
		public ErrorRecord(String exceptionMessage) 
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			ExceptionMessage = exceptionMessage;
			Time = df.format(new Date());
		}
		
		/**
		 * Time已经自动设置
		 */
		public ErrorRecord()
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Time = df.format(new Date());
		}
		
		/**
		 * 设置代码位置信息
		 * @param className 所属类名
		 * @param methodName 所属方法名
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
		
		//检查文件是否存在，不存在则直接创建
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
	 * 添加错误记录
	 * @param record 错误记录
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
	 * 获取所有的记录
	 * ！！！！！！不可在服务器主程序中调用，只可关闭了主程序后辅助程序调用
	 * @return 记录结合体
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
