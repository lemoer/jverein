/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.rmi;

import de.willuhn.datasource.rmi.DBObject;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.TreeSet;

public interface EasyBeitragsmonat extends DBObject
{

  public Buchung getBuchung() throws RemoteException;
  public void setBuchung() throws RemoteException;

  public Mitglied getMitglied() throws RemoteException;
  public void setMitglied() throws RemoteException;

  public int getJahr() throws RemoteException;
  public void setJahr() throws RemoteException;

  public int getMonat() throws RemoteException;
  public void setMonat() throws RemoteException;

}
