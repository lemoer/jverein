package de.jost_net.JVerein.util;

import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Scoring
{
  public static GenericIterator filterMitgliederByScoring(String suchstring, GenericIterator mitglieder)
    throws RemoteException
  {
    return Scoring.filterByScoring(suchstring, mitglieder, new Scoring.Scorable()
    {

      @Override public int score(String nextToken, Object o)
          throws RemoteException
      {
        Mitglied m = (Mitglied) o;

        return Scoring.scoreWord(nextToken, m.getName())
            + Scoring.scoreWord(nextToken, m.getVorname());
      }
    });
  }

  public static GenericIterator filterMitgliedskontenByScoring(String suchstring, GenericIterator mitgliedskonten)
    throws RemoteException
  {
    return Scoring.filterByScoring(suchstring, mitgliedskonten, new Scoring.Scorable()
    {

      @Override public int score(String nextToken, Object o)
          throws RemoteException
      {
        Mitgliedskonto mk = (Mitgliedskonto) o;

        return Scoring.scoreWord(nextToken, mk.getMitglied().getName()) + Scoring.scoreWord(
            nextToken, mk.getMitglied().getVorname()) + Scoring.scoreWord(nextToken,
            mk.getZweck1());
      }
    });
  }

  public static GenericIterator filterByScoring(String suchstring, GenericIterator it, Scorable s)
      throws RemoteException
  {
    // transform
    ArrayList<Object> ergebnis = new ArrayList<Object>();

    // In case the text search input is used, we calculate
    // an "equality" score for each Mitgliedskonto (aka
    // Mitgliedskontobuchung) entry. Only the entries with
    // score == maxScore will be shown.
    Integer maxScore = 0;

    while (it.hasNext())
    {
      Object mk =  (Object) it.next();

      StringTokenizer tok = new StringTokenizer(suchstring, " ,-");
      Integer score = 0;

      while (tok.hasMoreElements())
      {
        String nextToken = tok.nextToken();
        if (nextToken.length() > 3)
        {
          score = s.score(nextToken, mk);
        }
      }

      if (maxScore < score)
      {
        maxScore = score;
        // We found a Mitgliedskonto matching with a higher equality
        // score, so we drop all previous matches, because they were
        // less equal.
        ergebnis.clear();
      }
      else if (maxScore > score)
      {
        // This match is worse, so skip it.
        continue;
      }

      ergebnis.add(mk);

    }

    return PseudoIterator.fromArray(
        ergebnis.toArray(new GenericObject[ergebnis.size()]));
  }

  public interface Scorable {
    // Any number of final, static fields
    // Any number of abstract method declarations\
    public int score(String nextToken, Object o) throws RemoteException;
  }


  public static Integer scoreWord(String word, String in)
  {
    word = reduceWord(word);

    Integer wordScore = 0;
    StringTokenizer tok = new StringTokenizer(in, " ,-");

    while (tok.hasMoreElements())
    {
      String nextToken = tok.nextToken();
      nextToken = reduceWord(nextToken);

      // Full match is twice worth
      if (nextToken.equals(word))
      {
        wordScore += 2;
      }
      else if (nextToken.contains(word))
      {
        wordScore += 1;
      }
    }

    return wordScore;
  }

  private static String reduceWord(String word)
  {
    // We replace "ue" -> "u" and "ü" -> "u", because some bank institutions
    // remove the dots "ü" -> "u". So we get "u" == "ü" == "ue".
    return word.toLowerCase().replaceAll("ä", "a").replaceAll("ae", "a")
        .replaceAll("ö", "o").replaceAll("oe", "o").replaceAll("ü", "u")
        .replaceAll("ue", "u").replaceAll("ß", "s").replaceAll("ss", "s");
  }
}
