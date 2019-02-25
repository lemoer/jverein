package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.*;
import de.willuhn.jameica.hbci.gui.parts.SparQuote;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl.DIFFERENZ;
import de.jost_net.JVerein.gui.dialogs.MitgliedskontoAuswahlDialog;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import org.relique.jdbc.csv.SqlParser;

import java.rmi.RemoteException;
import java.util.*;

import static org.eclipse.swt.layout.GridData.*;

public class EasyMitgliedsbeitragAuswahlDialog extends AbstractDialog<Object>
{

  private de.willuhn.jameica.system.Settings settings;

  private String text = null;

  private Object choosen = null;

  private MitgliedskontoControl control;

  private TablePart mitgliedskontolist = null;

  private TablePart mitgliedlist = null;

  private Buchung buchung;

  public EasyMitgliedsbeitragAuswahlDialog(Buchung buchung)
  {
    super(MitgliedskontoAuswahlDialog.POSITION_MOUSE, true);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);

    this.setSize(600, 700);
    this.setTitle("Mitgliedskonto-Auswahl");
    this.buchung = buchung;
    control = new MitgliedskontoControl(null);
  }

  class Beitragsmonat implements Comparable<Beitragsmonat> {

    private final int jahr;

    private final int monat;

    private final boolean bezahlt;

    Beitragsmonat(int jahr, int monat, boolean bezahlt) {
      this.jahr = jahr;
      this.monat = monat;
      this.bezahlt = bezahlt;
      assert jahr > 1900;
      assert monat >= 0;
      assert monat < 12;
    }

    Beitragsmonat(int jahr, int monat) {
      this(jahr, monat, false);
    }

    public int getJahr()
    {
      return jahr;
    }

    public int getMonat()
    {
      return monat;
    }

    public boolean isBezahlt()
    {
      return bezahlt;
    }

    public int getFastRepr()
    {
      return getMonat() + 100 * getJahr();
    }

    @Override public int compareTo(Beitragsmonat o)
    {
      if (getFastRepr() > o.getFastRepr())
      {
        return 1;
      }
      else if (getFastRepr() == o.getFastRepr()) {
        return 0;
      }
      else {
        return -1;
      }
    }

    @Override public boolean equals(Object o)
    {
      if (!(o instanceof Beitragsmonat))
        return false;


      return getFastRepr() == ((Beitragsmonat) o).getFastRepr();
    }

  }

  @Override
  protected void paint(Composite parent) throws Exception
  {

    TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(FILL_HORIZONTAL));

    {
//      TabGroup tabNurIst = new TabGroup(folder, "nur Ist", false, 1);
//      Container grNurIst = new SimpleContainer(tabNurIst.getComposite());
//      grNurIst.addHeadline("Auswahl des Mitgliedskontos");
//      if (text == null || text.length() == 0)
//      {
//        text = "Bitte wählen Sie das gewünschte Mitgliedskonto aus.";
//      }
//
//      grNurIst.addText(text, true);
//      TextInput suNa = control.getSuchName();
//      suNa.setValue(buchung.getName());
//      grNurIst.addLabelPair("Name", suNa);
//      grNurIst.addLabelPair("Differenz", control.getDifferenz(DIFFERENZ.EGAL));
//      Action action = new Action()
//      {
//
//        @Override public void handleAction(Object context)
//        {
//          if (context == null || !(context instanceof Mitgliedskonto))
//          {
//            return;
//          }
//          choosen = context;
//          close();
//        }
//      };
//      mitgliedskontolist = control.getMitgliedskontoList(action, null);
//      mitgliedskontolist.paint(tabNurIst.getComposite());

      TabGroup tabNurIst = new TabGroup(folder, "nur Ist", false, 1);

      MitgliedControl mitgliedControl = new MitgliedControl(null);

      DBIterator<Mitglied> zhl = Einstellungen.getDBService()
          .createList(Mitglied.class);
      //zhl.addFilter(cond.toString());
      MitgliedUtils.setNurAktive(zhl);
      MitgliedUtils.setMitglied(zhl);
      zhl.setOrder("ORDER BY name, vorname");

//      String suche = "";
//      if (getMitglied().getZahlerID() != null)
//      {
//        suche = getMitglied().getZahlerID().toString();
//      }
//      Mitglied zahlmitglied = (Mitglied) Einstellungen.getDBService()
//          .createObject(Mitglied.class, suche);

      final Composite allYears = new Composite(tabNurIst.getComposite(), FILL_BOTH);

      final SelectInput zahler = new SelectInput(zhl, null);
      zahler.setAttribute("namevorname");
      zahler.setPleaseChoose("Bitte auswählen");
      zahler.addListener(new Listener()
      {
        @Override public void handleEvent(Event event)
        {
          if (event != null && event.type == SWT.Selection)
          {
            Mitglied sel = (Mitglied) zahler.getValue();

            try
            {
              Date von = sel.getEintritt();
              Date bis = sel.getAustritt();

              if (von != null)
              {
                GregorianCalendar calVon = new GregorianCalendar();
                calVon.setTime(von);

                int vonYear = calVon.get(Calendar.YEAR);
                int vonMonth = calVon.get(Calendar.MONTH);
              }
              if (bis != null)
              {
                GregorianCalendar calBis = new GregorianCalendar();
                calBis.setTime(bis);

                int bisYear = calBis.get(Calendar.YEAR);
                int bisMonth = calBis.get(Calendar.MONTH);
              }

              SWTUtil.disposeChildren(allYears);

              //GridLayout allYearsLayout = new GridLayout(13, false);
              //allYears.setLayout(allYearsLayout);


              ArrayList<Beitragsmonat> beitragsmonate = new ArrayList<Beitragsmonat>();
              beitragsmonate.add(new Beitragsmonat(2017,1));
              beitragsmonate.add(new Beitragsmonat(2017,2));
              beitragsmonate.add(new Beitragsmonat(2017,3));
              beitragsmonate.add(new Beitragsmonat(2017,4));
              beitragsmonate.add(new Beitragsmonat(2019,1));
              beitragsmonate.add(new Beitragsmonat(2017,5));
              beitragsmonate.add(new Beitragsmonat(2017,6));
              beitragsmonate.add(new Beitragsmonat(2017,7));
              beitragsmonate.add(new Beitragsmonat(2017,8));
              beitragsmonate.add(new Beitragsmonat(2017,9));
              beitragsmonate.add(new Beitragsmonat(2017,10));
              beitragsmonate.add(new Beitragsmonat(2017,11));
              beitragsmonate.add(new Beitragsmonat(2018,1));
              beitragsmonate.add(new Beitragsmonat(2018,2));
              beitragsmonate.add(new Beitragsmonat(2018,3));
              beitragsmonate.add(new Beitragsmonat(2018,4));
              beitragsmonate.add(new Beitragsmonat(2018,5));


              GridLayout allYearsLayout = new GridLayout(13, false);
              allYears.setLayout(allYearsLayout);

              updateBeitragsGrid(allYears, beitragsmonate);

              allYears.layout();

            }
            catch (RemoteException e)
            {

              // TODO: is this a good idea?
              e.printStackTrace();
            }

          }
        }
      });
      zahler.paint(tabNurIst.getComposite());

      allYears.getHorizontalBar().setVisible(false);
      allYears.getVerticalBar().setVisible(false);

      GridLayout allYearsLayout = new GridLayout(13, false);
      allYears.setLayout(allYearsLayout);

      ArrayList<Beitragsmonat> beitragsmonate = new ArrayList<Beitragsmonat>();
      beitragsmonate.add(new Beitragsmonat(2017,1, true));
      beitragsmonate.add(new Beitragsmonat(2017,2, true));
      beitragsmonate.add(new Beitragsmonat(2017,3));
      beitragsmonate.add(new Beitragsmonat(2017,4));
      beitragsmonate.add(new Beitragsmonat(2019,1));
      beitragsmonate.add(new Beitragsmonat(2017,5));
      beitragsmonate.add(new Beitragsmonat(2017,6));
      beitragsmonate.add(new Beitragsmonat(2017,7));
      beitragsmonate.add(new Beitragsmonat(2017,8));
      beitragsmonate.add(new Beitragsmonat(2017,9));
      beitragsmonate.add(new Beitragsmonat(2017,10));
      beitragsmonate.add(new Beitragsmonat(2017,11));
      beitragsmonate.add(new Beitragsmonat(2018,1));
      beitragsmonate.add(new Beitragsmonat(2018,2));
      beitragsmonate.add(new Beitragsmonat(2018,3));
      beitragsmonate.add(new Beitragsmonat(2018,4));
      beitragsmonate.add(new Beitragsmonat(2018,5));

      updateBeitragsGrid(allYears, beitragsmonate);
    }

    //
    {
      TabGroup tabSollIst = new TabGroup(folder, "Soll u. Ist", true, 1);
      Container grSollIst = new SimpleContainer(tabSollIst.getComposite());
      grSollIst.addHeadline("Auswahl des Mitgliedskontos");

      if (text == null || text.length() == 0)
      {
        text = "Bitte wählen Sie das gewünschte Mitgliedskonto aus.";
      }
      grSollIst.addText(text, true);
      control.getSuchName2(true).setValue(buchung.getName());
      grSollIst.addLabelPair("Name", control.getSuchName2(false));
      grSollIst.addInput(control.getSpezialSuche());

      final Action action2 = new Action()
      {

        @Override public void handleAction(Object context)
        {
          if (context == null || !(context instanceof Mitglied))
          {
            return;
          }
          choosen = context;
          close();
        }
      };
      mitgliedlist = control.getMitgliedskontoList2(action2, null);
      mitgliedlist.paint(tabSollIst.getComposite());

      ButtonArea b = new ButtonArea();

      b.addButton("übernehmen", new Action()
      {

        @Override public void handleAction(Object context)
        {
          Object o = mitgliedskontolist.getSelection();

          if (o instanceof Mitgliedskonto)
          {
            choosen = o;
            close();
          }
          else
          {
            o = mitgliedlist.getSelection();

            if (o instanceof Mitglied)
            {
              choosen = o;
              close();
            }
          }
          return;
        }
      }, null, false, "check.png");

      b.addButton("entfernen", new Action()
      {

        @Override public void handleAction(Object context)
        {
          choosen = null;
          close();
        }
      }, null, false, "undo.png");
      b.addButton("Hilfe", new DokumentationAction(), DokumentationUtil.MITGLIEDSKONTO_AUSWAHL, false,
          "question-circle.png");

      b.addButton("abbrechen", new Action()
      {

        @Override public void handleAction(Object context)
        {
          close();
        }
      }, null, false, "stop-circle.png");
      b.paint(parent);
    }
  }

  private void updateBeitragsGrid(Composite allYears, ArrayList<Beitragsmonat> l)
  {
    newLbl(allYears, " ");
    newLbl(allYears, "J");
    newLbl(allYears, "F");
    newLbl(allYears, "M");
    newLbl(allYears, "A");
    newLbl(allYears, "M");
    newLbl(allYears, "J");
    newLbl(allYears, "J");
    newLbl(allYears, "A");
    newLbl(allYears, "S");
    newLbl(allYears, "O");
    newLbl(allYears, "N");
    newLbl(allYears, "D");

    Beitragsmonat min = Collections.min(l);
    Beitragsmonat max = Collections.max(l);

    for (int i = min.getJahr(); i <= max.getJahr(); i++)
    {
      newLbl(allYears, Integer.toString(i));

      for (int j = 0; j < 12; j++)
      {
        Beitragsmonat iter = new Beitragsmonat(i, j);

        if (!l.contains(iter))
        {
          // Mitglied ist noch nicht eingetreten.
          newLbl(allYears, "x");
          continue;
        }
        else
        {
          Beitragsmonat data = l.get(l.indexOf(iter));

          Button b = new Button(allYears, SWT.CHECK);
          if (data.isBezahlt())
          {
            b.setEnabled(false);
            b.setSelection(true);
          }
          b.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));
        }
      }
    }
  }

  private void newLbl(Composite allYears, String s)
  {
    Label l = new Label(allYears, 0);
    l.setText(s);
    l.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));
  }

  /**
   * Liefert das ausgewaehlte Mitgliedskonto zurueck oder <code>null</code> wenn
   * der Abbrechen-Knopf gedrueckt wurde.
   *
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return choosen;
  }

  /**
   * Optionale Angabe des anzuzeigenden Textes. Wird hier kein Wert gesetzt,
   * wird ein Standard-Text angezeigt.
   *
   * @param text
   */
  public void setText(String text)
  {
    this.text = text;
  }
}
