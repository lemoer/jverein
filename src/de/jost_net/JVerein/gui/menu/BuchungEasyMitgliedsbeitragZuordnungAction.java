package de.jost_net.JVerein.gui.menu;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungEasyMitgliedsbeitragZuordnungAction implements Action
{
  private BuchungsControl control;

  public BuchungEasyMitgliedsbeitragZuordnungAction(BuchungsControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Buchung)
        && !(context instanceof Buchung[]))
    {
      throw new ApplicationException("Keine Buchung(en) ausgewählt");
    }
    try
    {
      Buchung[] b = null;
      if (context instanceof Buchung)
      {
        b = new Buchung[1];
        b[0] = (Buchung) context;
      }
      if (context instanceof Buchung[])
      {
        b = (Buchung[]) context;
      }
      if (b == null)
      {
        return;
      }
      if (b.length == 0)
      {
        return;
      }
      if (b[0].isNewObject())
      {
        return;
      }
      try
      {
        EasyMitgliedsbeitragAuswahlDialog mkaz = new EasyMitgliedsbeitragAuswahlDialog(b[0]);
        Object open = mkaz.open();
        Mitgliedskonto mk = null;

        if (open instanceof Mitgliedskonto)
        {
          mk = (Mitgliedskonto) open;
        }
        else if (open instanceof Mitglied)
        {
          Mitglied m = (Mitglied) open;
          mk = (Mitgliedskonto) Einstellungen.getDBService().createObject(
              Mitgliedskonto.class, null);

          Double betrag = 0d;
          for (Buchung buchung : b)
          {
            betrag += buchung.getBetrag();
          }

          mk.setBetrag(betrag);
          mk.setDatum(b[0].getDatum());
          mk.setMitglied(m);
          mk.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
          mk.setZweck1(b[0].getZweck());
          mk.store();
        }

        if (mk == null)
        {
          GUI.getStatusBar().setErrorText(
              "Fehler bei der Ermittlung des Mitgliedskontos");
        }

        for (Buchung buchung : b)
        {
          buchung.setMitgliedskonto(mk);
          buchung.store();
        }
        control.getBuchungsList();
        GUI.getStatusBar().setSuccessText("Mitgliedskonto zugeordnet");
      }
      catch (Exception e)
      {
        Logger.error("Fehler", e);
        GUI.getStatusBar().setErrorText(
            "Fehler bei der Zuordnung des Mitgliedskontos");
        return;
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Speichern.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }

}
