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
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.BuchungsartAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.BuchungsartControl;
import de.jost_net.JVerein.gui.control.EasyMitgliedsbeitragControl;
import de.jost_net.JVerein.gui.menu.BuchungsartMenu;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.Beitragsmonat;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class EasyMitgliedsbeitragListeView extends AbstractView
{
  private TablePart mitgliederlist;

  private EasyMitgliedsbeitragControl control;

  public EasyMitgliedsbeitragListeView()
  {
    this.control = new EasyMitgliedsbeitragControl(null);
  }

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Easy Mitgliedsbeitrag");

    LabelGroup group2 = new LabelGroup(getParent(), "Liste", true);
    group2.addPart(getMitgliederlist());

  }

  @SuppressWarnings("unchecked")
  public Part getMitgliederlist() throws RemoteException
  {

    DBService service = Einstellungen.getDBService();
    DBIterator<Mitglied> mitglieder = service
        .createList(Mitglied.class);

    if (mitgliederlist == null)
    {

      mitgliederlist = new TablePart(mitglieder, null);
      mitgliederlist.addColumn("Name", "name");
      mitgliederlist.addColumn("Vorname", "vorname");
      mitgliederlist.addColumn( "Eintritt","eintritt",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      mitgliederlist.addColumn( "Austritt","austritt",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      mitgliederlist.addColumn("Beitragsmonate gesamt", "id", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          Integer mid = Integer.parseInt((String) o);
          DBIterator<Mitglied> zhl = null;
          try
          {
            zhl = Einstellungen.getDBService()
                .createList(Mitglied.class);
            zhl.addFilter("id=" + mid);

            assert zhl.hasNext();
            Mitglied m = zhl.next();

            ArrayList<Beitragsmonat> beitragsmonate = control
                .getBeitragsmonateByMitglied(m, null);

            return Integer.valueOf(control.count(beitragsmonate, false)).toString();
          }
          catch (RemoteException e)
          {
            e.printStackTrace();
            return "ERROR";
          }
        }
      }, false, Column.ALIGN_LEFT);
      mitgliederlist.addColumn("Beitragsmonate unbezahlt", "id", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          Integer mid = Integer.parseInt((String) o);
          DBIterator<Mitglied> zhl = null;
          try
          {
            zhl = Einstellungen.getDBService()
                .createList(Mitglied.class);
            zhl.addFilter("id=" + mid);

            assert zhl.hasNext();
            Mitglied m = zhl.next();

            ArrayList<Beitragsmonat> beitragsmonate = control
                .getBeitragsmonateByMitglied(m, null);

            return Integer.valueOf(control.count(beitragsmonate, false)-control.count(beitragsmonate, true)).toString();
          }
          catch (RemoteException e)
          {
            e.printStackTrace();
            return "ERROR";
          }
        }
      }, false, Column.ALIGN_LEFT);
      mitgliederlist.setContextMenu(new BuchungsartMenu());
      mitgliederlist.setRememberColWidths(true);
      mitgliederlist.setRememberOrder(true);
      mitgliederlist.setRememberState(true);
      mitgliederlist.setSummary(true);
    }
    else
    {
      mitgliederlist.removeAll();

      for (Buchungsart bu : (List<Buchungsart>) PseudoIterator
          .asList(mitglieder))
      {
        mitgliederlist.addItem(bu);
      }
      mitgliederlist.sort();
    }
    return mitgliederlist;
  }

}