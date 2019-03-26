package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.EasyMitgliedsbeitragControl;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.rmi.EasyBeitragsmonat;
import de.jost_net.JVerein.server.EasyBeitragsmonatImpl;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.jost_net.JVerein.util.Beitragsmonat;
import de.jost_net.JVerein.util.Scoring;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.*;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.dialogs.MitgliedskontoAuswahlDialog;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;

import java.rmi.RemoteException;
import java.util.*;

import static org.eclipse.swt.layout.GridData.*;

public class EasyMitgliedsbeitragAuswahlDialog extends AbstractDialog<Object>
{

  private de.willuhn.jameica.system.Settings settings;

  private String text = null;

  private Object choosen = null;

  private EasyMitgliedsbeitragControl control;

  private TablePart mitgliedskontolist = null;

  private TablePart mitgliedlist = null;

  private Buchung buchung;

  private ArrayList<Beitragsmonat> beitragsmonate = new ArrayList<Beitragsmonat>();

  private AbstractDBObject transactionHolder;

  private SelectInput zahler;

  private Label ist;

  private TabGroup tabNurIst;

  private Composite allYears;

  public EasyMitgliedsbeitragAuswahlDialog(Buchung buchung)
  {
    super(MitgliedskontoAuswahlDialog.POSITION_MOUSE, true);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);

    this.setSize(600, 700);
    this.setTitle("Mitgliedskonto-Auswahl");
    this.buchung = buchung;
    control = new EasyMitgliedsbeitragControl(null);
  }

  private void autoFill()
  {
    double differenz = 0;
    try
    {
      differenz = buchung.getBetrag();
    }
    catch (RemoteException e)
    {
      e.printStackTrace();
    }

    // We want to fill the early months first
    Collections.sort(beitragsmonate);

    for (Beitragsmonat beitragsmonat : beitragsmonate)
    {

      if (beitragsmonat.isBezahlt())
      {
        continue;
      }

      beitragsmonat.setSelected(false);

      if (differenz <= 0)
      {
        continue;
      }

      beitragsmonat.setSelected(true);
      differenz -= beitragsmonat.getSollBetrag();
    }

    updateBeitragsGrid();
  }

  private void updateCalculation()
  {
    double sum = 0;

    for (int i = 0; i < beitragsmonate.size(); i++)
    {
      Beitragsmonat beitragsmonat =  beitragsmonate.get(i);

      if (!beitragsmonat.isSelected())
        continue;

      sum += beitragsmonat.getSollBetrag();
    }

    double istValue;
    try
    {
      istValue = buchung.getBetrag();
    }
    catch (RemoteException e)
    {
      e.printStackTrace();
      istValue = 0;
    }

    String istText = "Ist-Buchung: ";
    istText += String.format("%.2f", istValue) + "\n";
    istText += "Selektiert: ";
    istText += String.format("%.2f",sum) + "\n";
    istText += "Differenz: ";
    istText += String.format("%.2f", istValue - sum);

    ist.setText(istText);

    tabNurIst.getComposite().layout();

  }

  private Mitglied getMitgliedGuess() throws RemoteException
  {
    DBIterator<Mitglied> mitglieder = Einstellungen.getDBService()
        .createList(Mitglied.class);
    mitglieder.setOrder("order by name, vorname");

    if (buchung.getName() != null)
    {
      GenericIterator it2 = Scoring
          .filterMitgliederByScoring(buchung.getName(), mitglieder);

      if (it2.hasNext())
      {
        // maybe we drop other equal matches here.
        return (Mitglied) it2.next();
      }

    }

    return null;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {

    TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(FILL_BOTH));

    {

      tabNurIst = new TabGroup(folder, "nur Ist", false, 1);

      final Composite test = new Composite(tabNurIst.getComposite(), FILL_BOTH);
      GridLayout testLayout = new GridLayout(2, false);
      test.setLayout(testLayout);

      test.getHorizontalBar().setVisible(false);
      test.getVerticalBar().setVisible(false);

      MitgliedControl mitgliedControl = new MitgliedControl(null);

      DBIterator<Mitglied> zhl = Einstellungen.getDBService()
          .createList(Mitglied.class);
      //zhl.addFilter(cond.toString());
      MitgliedUtils.setNurAktive(zhl);
      MitgliedUtils.setMitglied(zhl);
      zhl.setOrder("ORDER BY name, vorname");

      allYears = new Composite(test, FILL_BOTH);
      allYears.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));

      Composite rightCol = new Composite(test, FILL_BOTH);
      GridLayout rightColLayout = new GridLayout(1, false);
      rightCol.setLayout(rightColLayout);
      rightCol.getHorizontalBar().setVisible(false);
      rightCol.getVerticalBar().setVisible(false);

      ist = new Label(rightCol, 0);
      ist.setText("TBD2");
      ist.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));

      de.willuhn.jameica.gui.parts.Button autoFillBtn = new de.willuhn.jameica.gui.parts.Button("AutoFill", new Action()
      {
        @Override public void handleAction(Object context)
            throws ApplicationException
        {
          autoFill();
        }
      });
      autoFillBtn.paint(rightCol);

      // (maybe) get preselected mitglied
      Mitglied preselected = null;
      {
        DBIterator<EasyBeitragsmonat> preIter = Einstellungen.getDBService().createList(EasyBeitragsmonat.class);
        preIter.addFilter("buchung=?", buchung.getID());

        if (preIter.hasNext())
        {
          EasyBeitragsmonat next = preIter.next();
          preselected = next.getMitglied();
        }
        else
        {
          // there is no Mitglied assigned to this Buchung yet.
          preselected = getMitgliedGuess();
          // TODO: maybe this should not happen automatically?
          // TODO: maybe we should have a minimal score here?
        }
      }

      if (preselected != null)
      {
        selectAndLoadMitglied(preselected, allYears, tabNurIst);
      }

      zahler = new SelectInput(zhl, preselected);
      zahler.setAttribute("namevorname");
      zahler.setPleaseChoose("Bitte auswählen");
      zahler.addListener(new Listener()
      {
        @Override public void handleEvent(Event event)
        {
          if (event != null && event.type == SWT.Selection)
          {
            Mitglied sel = (Mitglied) zahler.getValue();

            selectAndLoadMitglied(sel, allYears, tabNurIst);
          }
        }
      });
      zahler.paint(tabNurIst.getComposite());

      allYears.getHorizontalBar().setVisible(false);
      allYears.getVerticalBar().setVisible(false);
    }

    ButtonArea b = new ButtonArea();

    b.addButton("übernehmen", new Action()
    {

      @Override public void handleAction(Object context)
      {
        try
        {
          DBTransactionStart();

          // delete all old things
          // TODO: does the deletion happen in the transaction?
          DBIterator<EasyBeitragsmonat> zhl = Einstellungen.getDBService()
              .createList(EasyBeitragsmonat.class);
          zhl.addFilter("buchung=?", buchung.getID());

          while (zhl.hasNext())
          {
            EasyBeitragsmonat next = zhl.next();
            next.delete();
          }

          // insert new

          for (int i = 0; i < beitragsmonate.size(); i++)
          {
            Beitragsmonat beitragsmonat = beitragsmonate.get(i);

            if (!beitragsmonat.isSelected())
              continue;

            EasyBeitragsmonat bm = (EasyBeitragsmonat) Einstellungen.getDBService()
                .createObject(EasyBeitragsmonat.class, null);
            bm.setBuchung(buchung);
            bm.setMitglied((Mitglied) zahler.getValue());
            bm.setJahr(beitragsmonat.getJahr());
            bm.setMonat(beitragsmonat.getMonat());
            bm.store();

          }

          DBTransactionCommit();
        }
        catch (RemoteException e)
        {
          e.printStackTrace();
          try
          {
            DBTransactionRollback();
          }
          catch (RemoteException e2)
          {
            e2.printStackTrace();
          }
        }
        catch (ApplicationException e)
        {
          // ApplicationException wird vom .store() geworfen
          e.printStackTrace();
          try
          {
            DBTransactionRollback();
          }
          catch (RemoteException e2)
          {
            e2.printStackTrace();
          }
        }

        close();
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

  private void selectAndLoadMitglied(Mitglied sel, Composite allYears,
      TabGroup tabNurIst)
  {
    try
    {
      beitragsmonate = control.getBeitragsmonateByMitglied(sel, buchung);

      GridLayout allYearsLayout = new GridLayout(13, false);
      allYears.setLayout(allYearsLayout);

      updateBeitragsGrid();

    }
    catch (RemoteException e)
    {

      // TODO: is this a good idea?
      e.printStackTrace();
    }
  }

  private void updateBeitragsGrid()
  {
    SWTUtil.disposeChildren(allYears);

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

    Beitragsmonat min = Collections.min(beitragsmonate);
    Beitragsmonat max = Collections.max(beitragsmonate);

    for (int i = min.getJahr(); i <= max.getJahr(); i++)
    {
      newLbl(allYears, Integer.toString(i));

      for (int j = 0; j < 12; j++)
      {
        Beitragsmonat iter = new Beitragsmonat(i, j);

        if (!beitragsmonate.contains(iter))
        {
          // Mitglied ist noch nicht eingetreten.
          newLbl(allYears, "x");
          continue;
        }
        else
        {
          final Beitragsmonat data = beitragsmonate.get(beitragsmonate.indexOf(iter));

          final Button b = new Button(allYears, SWT.CHECK);
          if (data.isBezahlt())
          {
            b.setEnabled(false);
            b.setSelection(true);
          }

          if (data.isSelected())
          {
            // TODO: will man die Zustände mergen?
            b.setSelection(true);
          }

          b.addSelectionListener(new SelectionListener()
          {
            @Override public void widgetSelected(SelectionEvent e)
            {
              data.setSelected(b.getSelection());
              updateCalculation();
            }

            @Override public void widgetDefaultSelected(SelectionEvent e)
            {
              // TODO: what is this event?
            }
          });

          b.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));
        }
      }
    }

    allYears.layout();
    tabNurIst.getComposite().layout();

    updateCalculation();
  }

  private void DBTransactionStart() throws RemoteException
  {
    transactionHolder = (EasyBeitragsmonatImpl) Einstellungen.getDBService()
        .createObject(EasyBeitragsmonat.class, null);
    transactionHolder.transactionBegin();
  }

  private void DBTransactionCommit() throws RemoteException
  {
    if (null != transactionHolder)
      transactionHolder.transactionCommit();
    transactionHolder = null;
  }

  private void DBTransactionRollback() throws RemoteException
  {
    if (null != transactionHolder)
      transactionHolder.transactionRollback();
    transactionHolder = null;
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
