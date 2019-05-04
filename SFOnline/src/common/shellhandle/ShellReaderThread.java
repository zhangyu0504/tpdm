package common.shellhandle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ecc.emp.core.Context;

import core.log.SFLogger;

public class ShellReaderThread extends Thread {
	
	InputStream is = null;
	String type = null;
	Context context = null;
	
	public ShellReaderThread(Context context, InputStream is, String type){
		this.context = context;
		this.is = is;
		this.type = type;
	}
	
	public void run(){
		
		InputStreamReader isr = null;
		BufferedReader br = null;
		String msg = null;
		
		try {
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			while((msg = br.readLine())!=null){
				if("ERROR".equals(type))
					SFLogger.error(context, msg);
			}
		} catch (Exception e) {
			SFLogger.error(context, e.getMessage());
		} finally {
			try {
				if(br!=null)
					br.close();
				if(isr!=null)
					isr.close();
			} catch (IOException e1) {
				SFLogger.error(context, e1.getMessage());
			}
				
		}
	}
}
