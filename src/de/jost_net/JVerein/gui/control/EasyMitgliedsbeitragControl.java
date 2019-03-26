package de.jost_net.JVerein.gui.control;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.menu.EasyMitgliedsbeitragAuswahlDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.EasyBeitragsmonat;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.Beitragsmonat;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class EasyMitgliedsbeitragControl extends AbstractControl
{
 

  /**
   * Erzeugt einen neuen AbstractControl der fuer die angegebene View.
   *
   * @param view die View, fuer die dieser AbstractControl zustaendig ist.
   */
  public EasyMitgliedsbeitragControl(AbstractView view)
  {
    super(view);
  }

  public int countBeitraegeGezahlt(ArrayList<Beitragsmonat> beitragsmonate)
  {
    int count = 0;
    for (Beitragsmonat b: beitragsmonate)
    {
      if (!b.isBezahlt())
      {
        continue;
      }
      count += 1;
    }
    return count;
  }

  public int countBeitraegeSoll(ArrayList<Beitragsmonat> beitragsmonate)
  {
    int count = 0;
    for (Beitragsmonat b: beitragsmonate)
    {
      if (b.isZukunft())
      {
        continue;
      }
      count += 1;
    }
    return count;
  }

  public ArrayList<Beitragsmonat> getBeitragsmonateByMitglied(
      Mitglied sel,
      Buchung buchung) throws
      RemoteException
  {
    ArrayList<Beitragsmonat> beitragsmonate = new ArrayList<Beitragsmonat>();

    Date von = sel.getEintritt();
    Date bis = sel.getAustritt();

    GregorianCalendar calVon = new GregorianCalendar();
    GregorianCalendar calBis = new GregorianCalendar();

    if (von != null)
    {
      calVon.setTime(von);
      calVon.set(Calendar.DAY_OF_MONTH, 1);
    }
    else
    {
      // TODO: WAS NUN?
    }

    if (bis != null)
    {
      calBis.setTime(bis);
      calBis.set(Calendar.DAY_OF_MONTH, 2);
    }
    else
    {
      calBis.setTime(new Date());

      calBis.set(Calendar.DAY_OF_MONTH, 2);
    }

    //GridLayout allYearsLayout = new GridLayout(13, false);
    //allYears.setLayout(allYearsLayout);

    Calendar it = (Calendar) calVon.clone();
    Calendar end = calBis;

    DBIterator<EasyBeitragsmonat> zhl = Einstellungen.getDBService()
        .createList(EasyBeitragsmonat.class);
    zhl.addFilter("mitglied=?", sel.getID());

    beitragsmonate.clear();

    while (zhl.hasNext())
    {
      EasyBeitragsmonat next =  zhl.next();

      int jahr = next.getJahr();
      int monat = next.getMonat();

      Buchung b1 = next.getBuchung();
      Buchung b2 = buchung;

      double sollBetrag = sel.getBeitragsgruppe().getBetragMonatlich();

      if (b1 == null || !b1.equals(b2))
      {
        beitragsmonate.add(new Beitragsmonat(jahr, monat,
                sollBetrag, true));
      }
      else
      {
        Beitragsmonat e = new Beitragsmonat(
            jahr, monat, sollBetrag);
        e.setSelected(true);
        beitragsmonate.add(e);
      }
    }

    for (; it.before(end); it.add(Calendar.MONTH, 1)) {

      int jahr = it.get(Calendar.YEAR);
      int monat = it.get(Calendar.MONTH);

      double sollBetrag = sel.getBeitragsgruppe().getBetragMonatlich();
      Beitragsmonat bm = new Beitragsmonat(
          jahr, monat, sollBetrag);
      if (!beitragsmonate.contains(bm))
        beitragsmonate.add(bm);
    }

    return beitragsmonate;
  }
}
