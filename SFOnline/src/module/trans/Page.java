package module.trans;

import java.util.List;

import module.bean.Param;
import module.cache.ParamCache;

import common.exception.SFException;
import common.util.SFUtil;
/*
 * 分页对象
 */
public class Page<T> {
	private int pageSize=20;//分页大小
	
	private int startNum=0;//起始记录数
	
	private int pageNum=1;//当前页数
	
	private long totalNum;//总记录数
	
	private int totalPage;//总页数
	
	private List<T> pageData;//查询结果
	
	public Page(String perPageNum,int pageNum)throws SFException{
		//缓存中获取分页大小
		Param param=ParamCache.getValue("SF_PERPAGE_NUM",perPageNum);
		if(param!=null&&SFUtil.isNotEmpty(param.getValue())){
			this.pageSize=Integer.parseInt(param.getValue());
		}
		this.pageNum=pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public long getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(long totalNum) {
		this.totalNum = totalNum;
	}

	public int getTotalPage() {
		
		return totalNum%this.getPageSize()==0?Integer.parseInt( SFUtil.objectToString( totalNum/this.getPageSize() ) ):Integer.parseInt( SFUtil.objectToString( (totalNum/this.getPageSize())+1 ) );
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public List<T> getPageData() {
		return pageData;
	}

	public void setPageData(List<T> pageData) {
		this.pageData = pageData;
	}

	public long getStartNum() {
		int startPage;
		startPage=this.pageNum-1;
		if(startPage<0){
			startPage=0;
		}
		return startPage*this.pageSize;
	}
	
	public long getEndNum(){
		int endPage;
		endPage=this.pageNum;
		if(endPage<=0){
			endPage=1;
		}
		return endPage*this.pageSize;
	}

	public void setStartNum(int startNum) {
		this.startNum = startNum;
	}
}
