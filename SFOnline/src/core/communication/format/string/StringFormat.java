package core.communication.format.string;

import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;

import core.communication.format.FormatField;

public class StringFormat extends FormatField 
{
	public int extract (Object src, int offset) throws EMPFormatException {
		int retLen = super.extract(src, offset);
		if (retLen == -1) {
			return -1;
		}
		else if (retLen == 0) {
			if (src.getClass().isArray())
				return ((byte[]) src).length - offset;
			else
				return ((String)src).length() - offset;
		}
		else {
			return retLen;
		}
	}
	
   public String toString()
   {
	   return toString(0);
   }
   
   public String toString( int tabCount )
   {
	   StringBuffer buf = new StringBuffer();
	   for( int i=0; i<tabCount; i++ )
		   buf.append( "\t");
	   
	   buf.append("<fString dataName=\"");
	   buf.append(getDataName() );
	   buf.append("\"/>\n");
	   
	   for( int i=0; i<this.getDecorators().size(); i++ )
	   {
		   FormatElement fmt = (FormatElement)getDecorators().get( i );
		   buf.append( fmt.toString(tabCount) );
		   buf.append("\n");
	   }
	   
	   return buf.toString();
   }
}
