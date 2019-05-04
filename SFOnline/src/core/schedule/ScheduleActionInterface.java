package core.schedule;

import com.ecc.emp.core.Context;
import common.exception.SFException;
/**
 * 批量任务交易接口
 *
 */
public interface ScheduleActionInterface {
	
	public boolean init(Context context) throws SFException;

	public void execute(Context context) throws SFException;
}
