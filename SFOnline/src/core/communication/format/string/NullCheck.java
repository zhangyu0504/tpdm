package core.communication.format.string;

import com.ecc.emp.format.EMPFormatException;

import core.communication.format.Decorator;

public class NullCheck extends Decorator 
{


   /**
    * @roseuid 44FD3F2001C5
    */
   public NullCheck()
   {
    
   }
   
   
   /**
    * @param src
    * @param offset
    * @return int
    * @roseuid 44E40C5C0280
    */
   public int extract(Object src, int offset) throws EMPFormatException
   {
	   return 0;
   }
   
   /**
    * @param src
    * @return Object
    * @roseuid 44E40CF3007D
    */
   public Object addDecoration(Object src) 
   {
	   if( src == null )
		   return "";

	   return src;
   }
   
   /**
    * @param src
    * @return Object
    * @roseuid 44E40D210251
    */
   public Object removeDecoration(Object src) 
   {
	   return src;
   }

   
	public String toString()
	{
		return toString(0);
	}
	
	public String toString(int tabCount )
	{
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<tabCount; i++ )
		{
			buf.append( "\t");
		}
		buf.append("<nullCheck/>");
		return buf.toString() ;
	}
   
}
