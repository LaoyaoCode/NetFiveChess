package NetObjModel;

public class ChessInformationM extends ChessOrIconImage
{
	private int Price = 0;

	public ChessInformationM(int iD, String name, String des, int price)
	{
		super(iD, name, des);
		Price = price;
	}

	public int getPrice()
	{
		return Price;
	}
	
	
}
