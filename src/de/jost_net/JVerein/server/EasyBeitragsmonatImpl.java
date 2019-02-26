package de.jost_net.JVerein.server;

import de.jost_net.JVerein.rmi.*;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

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

    try
    {
      if (getMonat() > 11 || getMonat() < 0)
        throw new ApplicationException("Monat des Beitragsmonat muss zwischen 0 und 11 liegen!");

      if (getMitglied() == null)
        throw new ApplicationException("Der Beitragsmonat muss einem Nutzer zugeordnet sein.");

    }
    catch (RemoteException e)
    {
      e.printStackTrace();

      throw new ApplicationException("Fehler beim erstellen des Beitragsmonats.");
    }

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

  @Override public void setBuchung(Buchung b) throws RemoteException
  {
    setAttribute("buchung", b.getID());
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

  @Override public void setMitglied(Mitglied m) throws RemoteException
  {
    setAttribute("buchung", m.getID());
  }

  @Override public int getJahr() throws RemoteException
  {
    Long i = (Long) getAttribute("jahr");
    if (i == null)
      return 0;
    return i.intValue();
  }

  @Override public void setJahr(int jahr) throws RemoteException
  {
    setAttribute("jahr", jahr);
  }

  @Override public int getMonat() throws RemoteException
  {
    Long i = (Long) getAttribute("monat");
    if (i == null)
      return 0;
    return i.intValue();
  }

  @Override public void setMonat(int m) throws RemoteException
  {
    setAttribute("monat", m);
  }
}
