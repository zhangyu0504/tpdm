package core.communication.format.string;

import java.io.UnsupportedEncodingException;

import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.util.EMPUtils;

import core.communication.format.Decorator;


public class Delimiter extends Decorator 
{

	
	String delimChar;
	
   /**
    * @roseuid 44FD3F2001C5
    */
   public Delimiter() 
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
   
	   try {
		   if( String.class.isAssignableFrom( src.getClass() ))
		   {
			   int idx = ((String)src).indexOf(delimChar, offset);
			   if (idx < 0)
				   return -1;
			   else
				   return idx - offset + delimChar.length();
		   }
		   else
		   {
			   byte[] srcArr = (byte[])src;
			   int idx = EMPUtils.findSubArray( srcArr, delimChar.getBytes(this.getEncoding()), offset);
			   if (idx < 0)
				   return -1;
			   else
				   return idx - offset + delimChar.getBytes(this.getEncoding()).length;
		   }
	   }
	   catch (UnsupportedEncodingException e) {
			throw new EMPFormatException("Delimiter extract failed: invalid charset code!", e);
	   }
   }
   
   /**
    * @param src
    * @return Object
    * @roseuid 44E40CF3007D
    */
   public Object addDecoration(Object src) 
   {
	   if( String.class.isAssignableFrom( src.getClass() ))
	   {
		   return ((String)src) + delimChar; 
	   }
	   else
	   {
		   byte[] srcArr = (byte[])src;
		   byte[] delimArr = delimChar.getBytes();
		   byte[] dst = new byte[srcArr.length + delimArr.length];
		   System.arraycopy( srcArr, 0, dst, 0, srcArr.length );
		   System.arraycopy( delimArr, 0, dst, srcArr.length, delimArr.length );
		   return dst;
	   }
   }
   
   /**
    * @param src
    * @return Object
    * @roseuid 44E40D210251
    */
   public Object removeDecoration(Object src) 
   {
	   if( String.class.isAssignableFrom( src.getClass() ))
	   {
		   String strSrc = (String )src;
		   return strSrc.subSequence(0, strSrc.length() - delimChar.length() );
	   }
	   else
	   {
		   byte[] srcArr = (byte[])src;
		   byte[] delimArr = delimChar.getBytes();
		   
		   byte[] dst = new byte[srcArr.length - delimArr.length];
		   System.arraycopy(srcArr, 0, dst, 0, srcArr.length - delimArr.length);
		   return dst;
	   }
   }


	public String getDelimChar() {
		return delimChar;
	}
	
	
	public void setDelimChar(String delimChar) {
		this.delimChar = delimChar;
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
		buf.append("<delim delimChar=\"");
		buf.append( this.delimChar );
		buf.append( "\"/>");
		return buf.toString() ;
	}
   
}
