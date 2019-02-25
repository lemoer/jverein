package de.jost_net.JVerein.gui.menu;

import com.mckoi.util.IntegerListInterface;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.hbci.gui.parts.SparQuote;
import org.apache.commons.lang.ObjectUtils;
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
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import org.relique.jdbc.csv.SqlParser;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

      Composite allYears = new Composite(tabNurIst.getComposite(), FILL_BOTH);

      SelectInput zahler = new SelectInput(zhl, null);
      zahler.setAttribute("namevorname");
      zahler.setPleaseChoose("Bitte auswählen");
      zahler.addListener(event -> {
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


          }
          catch (RemoteException e)
          {

            // TODO: is this a good idea?
            e.printStackTrace();
          }


        }
      });
      zahler.paint(tabNurIst.getComposite());

      allYears.getHorizontalBar().setVisible(false);
      allYears.getVerticalBar().setVisible(false);

      GridLayout allYearsLayout = new GridLayout(13, false);
      allYears.setLayout(allYearsLayout);

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

      for (int i = 2015; i < 2019; i++)
      {
        newLbl(allYears, Integer.toString(i));

        for (int j = 0; j < 12; j++)
        {
          Button b = new Button(allYears, SWT.CHECK);
          b.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));
        }
      }
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
