package DataBase;

import java.io.Serializable;

public class UserAndGoodsModel extends DataBaseObject implements Serializable
{
	/**
	 * 物品对应关系ID
	 */
	public int Id = 0 ;
	/**
	 * 货物ID，现在只有棋子
	 */
	public int GoodsId = 0 ;
	/**
	 * 用户ID
	 */
	public int UserId = 0 ;
	/**
	 * 拥有数量，现在没有任何作用
	 */
	public int Number = 0 ;
	
	public UserAndGoodsModel(int id, int goodsId, int userId, int number) {
		super("GoodsAndUser");
		Id = id;
		GoodsId = goodsId;
		UserId = userId;
		Number = number;
	}
	
	public UserAndGoodsModel(int goodsId, int userId, int number) {
		super("GoodsAndUser");
		GoodsId = goodsId;
		UserId = userId;
		Number = number;
	}
	
	public UserAndGoodsModel(int goodsId, int userId) {
		super("GoodsAndUser");
		GoodsId = goodsId;
		UserId = userId;
	}
	
	public UserAndGoodsModel() {
		super("GoodsAndUser");
	}
	
	@Override
	public void SetMID(int id) {
		Id = id;
	}
	
	@Override
	public String GetMUpdateStatement() {
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append("UPDATE ");
		bulider.append(BelongTableName) ;
		bulider.append(" ") ;
		
		bulider.append("SET ");
		bulider.append("GoodsID=" + + GoodsId + ",") ;
		bulider.append("UserID=" + UserId + ",") ;
		bulider.append("Number=" + Number ) ;
		bulider.append(" ") ;
		
		bulider.append("WHERE ID=" + Id + ";") ;
		return bulider.toString();
	}
	@Override
	public String GetMAddStatement() {
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append("INSERT INTO ") ;
		bulider.append(BelongTableName) ;
		bulider.append("(GoodsID,UserID,Number)") ;
		bulider.append(" ") ;
		
		bulider.append("VALUES");
		bulider.append("(");
		bulider.append(GoodsId+ ",") ;
		bulider.append(UserId+ ",") ;
		bulider.append(Number) ;
		bulider.append(");");
		
		return bulider.toString();
	}
	@Override
	public String GetMDeleteStatement() {
		StringBuilder bulider = new StringBuilder() ;
		
		bulider.append("DELETE FROM ") ;
		bulider.append(BelongTableName);
		bulider.append(" ") ;
		
		bulider.append("WHERE ID=");
		bulider.append(Id) ;
		bulider.append(";");
		
		return bulider.toString();
	}
	
	@Override
	public String toString()
	{
		StringBuilder bulider = new StringBuilder();
		
		bulider.append(String.format("%-15s = %-10s\n", "Id" , Id)) ;
		bulider.append(String.format("%-15s = %-10s\n", "GoodsId" , GoodsId)) ;
		bulider.append(String.format("%-15s = %-10s\n", "UserId" , UserId)) ;
		bulider.append(String.format("%-15s = %-10s\n", "Number" , Number)) ;

		return bulider.toString();
	}
}
