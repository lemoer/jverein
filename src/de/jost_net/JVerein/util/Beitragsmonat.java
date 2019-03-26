package de.jost_net.JVerein.util;

import de.jost_net.JVerein.gui.menu.EasyMitgliedsbeitragAuswahlDialog;

public class Beitragsmonat implements Comparable<Beitragsmonat> {

  private final int jahr;

  private final int monat;

  private final boolean bezahlt;

  private boolean selected;

  private Double sollBetrag;

  public Beitragsmonat(int jahr, int monat, Double sollBetrag, boolean bezahlt) {
    this.jahr = jahr;
    this.monat = monat;
    this.bezahlt = bezahlt;
    this.selected = false;
    this.sollBetrag = sollBetrag;
    assert jahr > 1900;
    assert monat >= 0;
    assert monat < 12;
  }

  public Beitragsmonat(int jahr, int monat, Double sollBetrag) {
    this(jahr, monat, sollBetrag, false);
  }

  public Beitragsmonat(int jahr, int monat) {
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
