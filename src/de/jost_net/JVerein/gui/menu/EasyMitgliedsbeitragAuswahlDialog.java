package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.rmi.EasyBeitragsmonat;
import de.jost_net.JVerein.server.EasyBeitragsmonatImpl;
import de.jost_net.JVerein.server.MitgliedUtils;
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
import de.jost_net.JVerein.gui.control.MitgliedskontoControl;
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

  private MitgliedskontoControl control;

  private TablePart mitgliedskontolist = null;

  private TablePart mitgliedlist = null;

  private Buchung buchung;

  private ArrayList<Beitragsmonat> beitragsmonate = new ArrayList<Beitragsmonat>();

  private AbstractDBObject transactionHolder;

  private SelectInput zahler;

  private Label soll;
  private Label ist;

  private TabGroup tabNurIst;

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

    private boolean selected;

    private Double sollBetrag;

    Beitragsmonat(int jahr, int monat, Double sollBetrag, boolean bezahlt) {
      this.jahr = jahr;
      this.monat = monat;
      this.bezahlt = bezahlt;
      this.selected = false;
      this.sollBetrag = sollBetrag;
      assert jahr > 1900;
      assert monat >= 0;
      assert monat < 12;
    }

    Beitragsmonat(int jahr, int monat, Double sollBetrag) {
      this(jahr, monat, sollBetrag, false);
    }

    Beitragsmonat(int jahr, int monat) {
      this(jahr, monat, null);
    }

    public Double getSollBetrag() { return sollBetrag; }

    public int getJahr()
    {
      return jahr;
    }

    public int getMonat()
    {
      return monat;
    }

    public boolean isSelected()
    {
      return this.selected;
    }

    public void setSelected(boolean selected)
    {
      this.selected = selected;
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

  private void updateCalculation()
  {
    String text = "<b>Soll:</b>\n\n";
    double sum = 0;

    for (int i = 0; i < beitragsmonate.size(); i++)
    {
      Beitragsmonat beitragsmonat =  beitragsmonate.get(i);

      if (!beitragsmonat.isSelected())
        continue;

      text += "+ " + beitragsmonat.getSollBetrag() + "\n";
      sum += beitragsmonat.getSollBetrag();
    }

    text += "---------------\n";
    text += "= " + sum + "\n";

    soll.setText(text);

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

    String istText = "<b>Ist:</b> ";
    istText += istValue + "\n";
    istText += "<b>Soll:</b> ";
    istText += sum + "\n";
    istText += "<b>Differenz:</b> ";
    istText += (istValue - sum);

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


      // TODO: any style?
      soll = new Label(test, 0);
      soll.setText("TBD");
      soll.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));

      ist = new Label(test, 0);
      ist.setText("TBD2");
      ist.setLayoutData(new GridData(VERTICAL_ALIGN_BEGINNING));

      MitgliedControl mitgliedControl = new MitgliedControl(null);

      DBIterator<Mitglied> zhl = Einstellungen.getDBService()
          .createList(Mitglied.class);
      //zhl.addFilter(cond.toString());
      MitgliedUtils.setNurAktive(zhl);
      MitgliedUtils.setMitglied(zhl);
      zhl.setOrder("ORDER BY name, vorname");

      final Composite allYears = new Composite(tabNurIst.getComposite(), FILL_BOTH);

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
        calBis.setTime(von);
        calBis.set(Calendar.DAY_OF_MONTH, 1);
      }
      else
      {
        // show checkboxes 1 year into future from now
        calBis.setTime(new Date());
        calBis.add(Calendar.YEAR, 1);

        calBis.set(Calendar.DAY_OF_MONTH, 1);
      }

      SWTUtil.disposeChildren(allYears);

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
        Buchung b2 = EasyMitgliedsbeitragAuswahlDialog.this.buchung;

        double sollBetrag = sel.getBeitragsgruppe().getBetragMonatlich();

        if (b1 == null || !b1.equals(b2))
        {
          beitragsmonate.add(new Beitragsmonat(jahr, monat, sollBetrag, true));
        }
        else
        {
          Beitragsmonat e = new Beitragsmonat(jahr, monat, sollBetrag);
          e.setSelected(true);
          beitragsmonate.add(e);
        }
      }

      for (; it.before(end); it.add(Calendar.MONTH, 1)) {

        int jahr = it.get(Calendar.YEAR);
        int monat = it.get(Calendar.MONTH);

        double sollBetrag = sel.getBeitragsgruppe().getBetragMonatlich();
        Beitragsmonat bm = new Beitragsmonat(jahr, monat, sollBetrag);
        if (!beitragsmonate.contains(bm))
          beitragsmonate.add(bm);
      }

      GridLayout allYearsLayout = new GridLayout(13, false);
      allYears.setLayout(allYearsLayout);

      updateBeitragsGrid(allYears, beitragsmonate);

      allYears.layout();
      tabNurIst.getComposite().layout();

    }
    catch (RemoteException e)
    {

      // TODO: is this a good idea?
      e.printStackTrace();
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
          final Beitragsmonat data = l.get(l.indexOf(iter));

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
