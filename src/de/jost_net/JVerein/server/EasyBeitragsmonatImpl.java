package de.jost_net.JVerein.server;

import de.jost_net.JVerein.rmi.*;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.rmi.RemoteException;

public class EasyBeitragsmonatImpl extends AbstractDBObject
  implements EasyBeitragsmonat
{
  private static final long serialVersionUID = 500102542884220659L;

  public EasyBeitragsmonatImpl() throws RemoteException
  {
    super();
  }

  @Override protected String getTableName()
  {
    return "easy_beitragsmonat";
  }

  @Override public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override protected void deleteCheck() throws ApplicationException
  {

  }

  @Override protected void insertCheck() throws ApplicationException
  {
    // TODO:
  }

  @Override protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override protected Class<?> getForeignObject(String field)
      throws RemoteException
  {
    if ("buchung".equals(field))
    {
      return Buchung.class;
    }
    else if ("mitglied".equals(field))
    {
      return Mitglied.class;
    }
    return null;
  }

  @Override public Buchung getBuchung() throws RemoteException
  {
    return (Buchung) getAttribute("buchung");
  }

  @Override public void setBuchung() throws RemoteException
  {

  }

  @Override public Mitglied getMitglied() throws RemoteException
  {
    Object o = super.getAttribute("mitglied");
    Long l = null;
    if (o instanceof Long)
    {
      l = (Long) o;
    }
    if (o instanceof Integer)
    {
      l = new Long((Integer) o);
    }
    if (l == null)
    {
      return null; // Kein Mitglied zugeordnet
    }
    Cache cache = Cache.get(Mitglied.class, true);
    return (Mitglied) cache.get(l);
  }

  @Override public void setMitglied() throws RemoteException
  {
    // not implemented
  }

  @Override public int getJahr() throws RemoteException
  {
    Long i = (Long) getAttribute("jahr");
    if (i == null)
      return 0;
    return i.intValue();
  }

  @Override public void setJahr() throws RemoteException
  {

  }

  @Override public int getMonat() throws RemoteException
  {
    Long i = (Long) getAttribute("monat");
    if (i == null)
      return 0;
    return i.intValue();
  }

  @Override public void setMonat() throws RemoteException
  {

  }
}
