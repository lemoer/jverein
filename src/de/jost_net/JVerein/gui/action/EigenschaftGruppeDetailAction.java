/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 * heiner@jverein.de
 * www.jverein.de
 * $Log$
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.view.EigenschaftGruppeDetailView;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class EigenschaftGruppeDetailAction implements Action
{
  public void handleAction(Object context) throws ApplicationException
  {
    EigenschaftGruppe eg = null;

    if (context != null && (context instanceof EigenschaftGruppe))
    {
      eg = (EigenschaftGruppe) context;
    }
    else
    {
      try
      {
        eg = (EigenschaftGruppe) Einstellungen.getDBService().createObject(
            EigenschaftGruppe.class, null);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(JVereinPlugin.getI18n().tr(
            "Fehler bei der Erzeugung der neuen EigenschaftGruppe"), e);
      }
    }
    GUI.startView(EigenschaftGruppeDetailView.class.getName(), eg);
  }
}
