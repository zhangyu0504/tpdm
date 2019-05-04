package common.action.dataoper;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.EMPAction;

public class SFCopyKcollAction extends EMPAction
{
  private String sourceKcoll;
  private String destKcoll;

  public String getSourceKcoll()
  {
    return this.sourceKcoll;
  }

  public void setSourceKcoll(String sourceKcoll)
  {
    this.sourceKcoll = sourceKcoll;
  }

  public String getDestKcoll()
  {
    return this.destKcoll;
  }

  public void setDestKcoll(String destKcoll)
  {
    this.destKcoll = destKcoll;
  }

  public String execute(Context context)
    throws EMPException
  {
    KeyedCollection srcKcoll = (KeyedCollection)context.getDataElement(this.sourceKcoll);
    KeyedCollection dstKcoll = (KeyedCollection)context.getDataElement(this.destKcoll);

    for (int i = 0; i < srcKcoll.size(); i++)
    {
      DataField dField = (DataField)srcKcoll.getDataElement(i);
      String dString = dField.getName();
      if (!dstKcoll.containsKey(dString))
        continue;
      dstKcoll.setDataValue(dString, dField.getValue());
    }

    return "0";
  }
}
