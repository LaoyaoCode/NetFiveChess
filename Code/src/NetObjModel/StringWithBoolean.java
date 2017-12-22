package NetObjModel;

import java.io.Serializable;

public class StringWithBoolean implements Serializable
{
	public String STRING_VALUE = null;
	public boolean BOOLEAN_VALUE = false ;
	
	public StringWithBoolean(String sv, boolean bv) 
	{
		STRING_VALUE = sv;
		BOOLEAN_VALUE = bv;
	}
	
	public StringWithBoolean()
	{
		this(null , false) ;
	}
}
