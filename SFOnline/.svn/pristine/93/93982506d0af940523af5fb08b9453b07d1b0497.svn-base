package com.dc.eai.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dc.expr.CompositeDataExprData;
import com.dc.expr.Exp;
import com.dc.expr.Expr;

public final class CompositeData extends AtomData
{
  private static Log log = LogFactory.getLog(CompositeData.class);

  private Map children = new LinkedHashMap();

  private boolean cascadeCreate = false;

  private ArrayList base = new ArrayList();

  private transient CompositeDataExprData exprData = new CompositeDataExprData(this);

  private transient Iterator it = new MyIterator();

  public CompositeData()
  {
    this(true);
  }

  public CompositeData(boolean createFlag)
  {
    this.cascadeCreate = createFlag;

    this.type = STRUCT_TYPE;
  }

  public void addBase(CompositeData data)
  {
    if ((data != this) && (!this.base.contains(data)))
    {
      this.base.add(data);
    }
  }

  public void addField(String name, Field field)
  {
    addObject(name, field);
  }

  public void addStruct(String name, CompositeData struct)
  {
    addObject(name, struct);
  }

  public void addArray(String name, Array array)
  {
    addObject(name, array);
  }

  public void addObject1(String fullname, AtomData atom)
  {
    if ((fullname == null) || (fullname.equals("")))
    {
      throw new IllegalArgumentException("bad-name");
    }
    if (atom == this)
    {
      throw new IllegalArgumentException("reference-to-self");
    }
    this.children.put(fullname, atom);
  }

  public void addObject(String fullname, AtomData atom)
  {
    if ((fullname == null) || (fullname.equals("")))
    {
      throw new IllegalArgumentException("bad-name");
    }
    if (atom == this)
    {
      throw new IllegalArgumentException("reference-to-self");
    }

    int pointIndex = fullname.indexOf('.');

    int lbIndex = fullname.lastIndexOf('[');

    int rbIndex = fullname.lastIndexOf(']');

    int len = fullname.length();

    if ((pointIndex < 0) && (lbIndex < 0) && (rbIndex < 0))
    {
      this.children.put(fullname, atom);

      return;
    }

    if (pointIndex > 0)
    {
      String parent = fullname.substring(0, pointIndex);

      String name = fullname.substring(pointIndex + 1);

      addToStruct(parent, name, atom);

      return;
    }

    if (lbIndex > 0)
    {
      if (rbIndex < lbIndex)
      {
        throw new IllegalArgumentException("bad-name");
      }
      String parent = fullname.substring(0, lbIndex);

      int arrayIndex = Integer.valueOf(fullname.substring(lbIndex + 1, rbIndex)).intValue();

      AtomData a = atom;
      while (true)
      {
        lbIndex = -1;

        rbIndex = -1;

        int pl = parent.length();

        for (int i = 0; i < pl; i++)
        {
          char c = parent.charAt(i);

          if (c == '[')
          {
            lbIndex = i;
          }
          else if (c == ']')
          {
            rbIndex = i;
          }

        }

        if (rbIndex < lbIndex)
        {
          throw new IllegalArgumentException("bad-name");
        }
        if ((lbIndex <= 0) || (rbIndex <= 0) || (rbIndex <= lbIndex))
        {
          break;
        }
        Array parentArray = (Array)getObject(parent);

        if (parentArray == null)
        {
          parentArray = new Array();
        }

        int index = Integer.valueOf(parent.substring(lbIndex + 1, rbIndex)).intValue();

        parent = parent.substring(0, lbIndex);

        if (a.isArray())
        {
          parentArray.addArray(arrayIndex, (Array)a);
        }
        else if (a.isStruct())
        {
          parentArray.addStruct(arrayIndex, (CompositeData)a);
        }
        else if (a.isField())
        {
          parentArray.addField(arrayIndex, (Field)a);
        }
        else
        {
          throw new IllegalArgumentException("bad-value");
        }

        a = parentArray;

        arrayIndex = index;
      }

      addToArray(parent, arrayIndex, a);

      return;
    }
  }

  private void addToStruct(String parent, String name, AtomData atom)
  {
    CompositeData struct = null;

    AtomData temp = getObject(parent);

    if ((temp != null) && (temp.isStruct()))
    {
      struct = (CompositeData)temp;

      struct.addObject(name, atom);

      return;
    }

    if ((struct == null) && (this.cascadeCreate))
    {
      struct = new CompositeData();

      struct.addObject(name, atom);

      int lbIndex = -1;

      int rbIndex = -1;

      int pl = parent.length();

      for (int i = 0; i < pl; i++)
      {
        char c = parent.charAt(i);

        if (c == '[')
        {
          lbIndex = i;
        }
        else if (c == ']')
        {
          rbIndex = i;
        }

      }

      if ((lbIndex < 0) && (rbIndex < 0))
      {
        this.children.put(parent, struct);
      }
      else
      {
        addObject(parent, struct);
      }
    }
  }

  private void addToArray(String parent, int arrayIndex, AtomData atom)
  {
    Array array = null;

    AtomData temp = getObject(parent);

    if ((temp != null) && (temp.isArray()))
    {
      array = (Array)temp;

      if (atom.isArray())
      {
        array.addArray(arrayIndex, (Array)atom);
      }
      else if (atom.isStruct())
      {
        array.addStruct(arrayIndex, (CompositeData)atom);
      }
      else if (atom.isField())
      {
        array.addField(arrayIndex, (Field)atom);
      }
      else
      {
        throw new IllegalArgumentException("bad-value");
      }

      return;
    }

    if ((array == null) && (this.cascadeCreate))
    {
      array = new Array();

      if (atom.isArray())
      {
        array.addArray(arrayIndex, (Array)atom);
      }
      else if (atom.isStruct())
      {
        array.addStruct(arrayIndex, (CompositeData)atom);
      }
      else if (atom.isField())
      {
        array.addField(arrayIndex, (Field)atom);
      }
      else
      {
        throw new IllegalArgumentException("bad-value");
      }

      this.children.put(parent, array);
    }
  }

  private String getParentRoot(String fullname)
  {
    int index = -1;

    int len = fullname.length();

    for (int i = 0; i < len; i++)
    {
      char c = fullname.charAt(i);

      if ((c == '.') || (c == '['))
      {
        index = i;

        break;
      }

    }

    if (index < 0)
    {
      return fullname;
    }
    if (index == 0)
    {
      throw new IllegalArgumentException("bad-name");
    }

    return fullname.substring(0, index);
  }

  public AtomData getObject(String fullname)
  {
    AtomData atom = getObjectFromLocal(fullname);

    if ((atom != null) && (atom.isStruct()))
    {
      AtomData baseAtom = getObjectFromBase(fullname);

      if ((baseAtom != null) && (baseAtom.isArray()))
      {
        ((Array)atom).addBase((Array)baseAtom);
      }

      if ((baseAtom != null) && (baseAtom.isStruct()))
      {
        ((CompositeData)atom).addBase((CompositeData)baseAtom);
      }

    }

    if (atom == null)
    {
      atom = getObjectFromBase(fullname);
    }

    return atom;
  }

  private AtomData getObjectFromLocal(String fullname)
  {
    AtomData result = (AtomData)this.children.get(fullname);

    if (result != null)
    {
      return result;
    }

    int pointIndex = -1;

    int lbIndex = -1;

    int rbIndex = -1;

    pointIndex = fullname.indexOf('.');

    lbIndex = fullname.lastIndexOf('[');

    rbIndex = fullname.lastIndexOf(']');

    if ((pointIndex < 0) && (lbIndex < 0) && (rbIndex < 0))
    {
      return null;
    }

    Exp exp = Expr.compile(fullname);

    exp = exp.eval(this.exprData);

    return (AtomData)exp.objectValue();
  }

  private AtomData getObjectFromBase(String name)
  {
    int size = this.base.size();

    for (int i = 0; i < this.base.size(); i++)
    {
      CompositeData data = (CompositeData)this.base.get(i);

      AtomData atom = data.getObject(name);

      if (atom != null)
      {
        return atom;
      }

    }

    return null;
  }

  public Field getField(String name)
  {
    AtomData obj = getObject(name);

    if (obj == null)
    {
      return null;
    }
    if ((obj instanceof Field))
    {
      return (Field)obj;
    }

    throw unmatchType();
  }

  public Array getArray(String name)
  {
    AtomData obj = getObject(name);

    if (obj == null)
    {
      if (this.cascadeCreate)
      {
        Array temp = new Array(this.cascadeCreate);

        addArray(name, temp);

        return temp;
      }

      return null;
    }

    if ((obj instanceof Array))
    {
      return (Array)obj;
    }

    throw unmatchType();
  }

  public CompositeData getStruct(String name)
  {
    AtomData obj = getObject(name);

    if (obj == null)
    {
      if (this.cascadeCreate)
      {
        CompositeData temp = new CompositeData(this.cascadeCreate);

        addStruct(name, temp);

        return temp;
      }

      return null;
    }

    if ((obj instanceof CompositeData))
    {
      return (CompositeData)obj;
    }

    throw unmatchType();
  }

  public void removeObject(String name)
  {
    if (this.children.containsKey(name))
    {
      this.children.remove(name);
    }
  }

  public Iterator iterator()
  {
    this.it = new MyIterator();

    return this.it;
  }

  private UnmatchTypeException unmatchType()
  {
    return new UnmatchTypeException();
  }

  public boolean isCascadeCreate()
  {
    return this.cascadeCreate;
  }

  public void setCascadeCreate(boolean b)
  {
    this.cascadeCreate = b;
  }

  public int size()
  {
    return this.children.size();
  }

  public boolean contains(String name)
  {
    if (this.children.containsKey(name))
    {
      return true;
    }

    return false;
  }

  private class MyIterator implements Iterator
  {
    Iterator it;
    int index = -1;

    MyIterator()
    {
      this.it = CompositeData.this.children.keySet().iterator();
    }

    public boolean hasNext()
    {
      if (this.it.hasNext())
      {
        return true;
      }

      this.index += 1;

      int size = CompositeData.this.base.size();

      if (this.index < size)
      {
        CompositeData data = (CompositeData)CompositeData.this.base.get(this.index);

        Iterator tmp = data.iterator();

        if (tmp != CompositeData.this.children.keySet().iterator())
        {
          this.it = tmp;
        }

        return hasNext();
      }

      return false;
    }

    public Object next()
    {
      return this.it.next();
    }

    public void remove()
    {
      this.it.remove();
    }
  }
}