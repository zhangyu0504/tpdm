<%@page language="java" contentType="text/html; charset=gb2312"%>
<%

	Exception exception = null;
	String errorMsg = "";
	String errStack = "";

	try
	{
		exception =(Exception)request.getAttribute("exception");
		errorMsg = exception.toString();
		java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
		java.io.PrintStream ps = new java.io.PrintStream(bo);
		exception.printStackTrace(ps);
		errStack = new String( bo.toByteArray() );
	}
	catch (Exception e)
	{
		System.out.println(e);
	}
%>

<HTML><HEAD>
<TITLE>EMP Error info</TITLE>
<META http-equiv="Content-Type" content="text/html; charset=gb2312"/>

<SCRIPT language="javaScript">
	function showMsg(){
	 if(document.all.errorDetialMsg.style.display==''){
	 	document.all.errorDetialMsg.style.display='none';
	 	document.all.btn.innerHTML='>>>>>>>...';
	 }else{
	 	document.all.errorDetialMsg.style.display='';
	 	document.all.btn.innerHTML='<<<<<<<...';
	 }
	}
</SCRIPT>

</HEAD><BODY>

<TABLE border="0" width="100%" class="commonTable" cellspacing="0" cellpadding="0">
	<TR>
		<TD align="left">
		<h2>Internal error !</h2>
		</TD>
	</TR>
	<TR>
	<TD>
      <TABLE border="0" width="80%" cellspacing="0" cellpadding="0" align="center">
        <TR aligh="center">
          <TD>ErrorMessage：<%=errorMsg%> </TD>
        </TR>
        
        <TR> 
          <TD align="right">
          	<LABEL id="btn" onclick="javaScript:showMsg();">>>>>>>...</LABEL>
          </TD>
        </TR>
        <TR>
          <TD>
          	<LABEL id="errorDetialMsg" style="display: none"><%=errStack%></LABEL>
         </TD>
        </TR>
      </TABLE>
	</TD>
	</TR>
</TABLE>
</BODY>
</HTML>
