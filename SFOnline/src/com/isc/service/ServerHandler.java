package com.isc.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.pingan.isc.core.MessageHandler;

public class ServerHandler implements MessageHandler {

	private String filename = "";

	private boolean print = true;

	public String getFilename() {
		return filename;
	}

	public void setFilename( String filename ) {
		this.filename = filename;
	}

	public boolean isPrint() {
		return print;
	}

	public void setPrint( boolean print ) {
		this.print = print;
	}

	public void messageReceived( Object message ) throws Exception {

		System.out.println( "ServerHandler messageReceived message=[" + message + "]" );

		// saveTOFile((String)message);
	}

	public synchronized void saveTOFile( String msg ) {
		if( isPrint() ) {
			System.out.println( "recv=[" + msg + "]" );
		}

		String outfilename = filename;
		try {
			File f = new File( outfilename );
			if( !f.exists() ) {

				if( !f.createNewFile() ) {
					System.out.println( "文件创建失败！" );
				}
			}

			BufferedWriter output = null;
			output = new BufferedWriter( new FileWriter( f, true ) );
			output.write( msg + "\r\n" );
			output.flush();
			output.close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

}
