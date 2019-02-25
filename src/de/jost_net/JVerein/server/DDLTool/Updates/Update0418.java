package de.jost_net.JVerein.server.DDLTool.Updates;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0418 extends AbstractDDLUpdate
{
  public Update0418(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table t = new Table("easy_beitragsmonat");
    Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
    t.add(pk);

    Column buchung = new Column("buchung", COLTYPE.BIGINT, 10, null, false,
        false);
    t.add(buchung);

    Column mitglied = new Column("mitglied", COLTYPE.BIGINT, 10, null, true,
        false);
    t.add(mitglied);

    Column jahr = new Column("jahr", COLTYPE.BIGINT, 10, null, true,
        false);
    t.add(jahr);

    Column monat = new Column("monat", COLTYPE.BIGINT, 10, null, true,
        false);
    t.add(monat);

    t.setPrimaryKey(pk);

    execute(this.createTable(t));

    // TODO: on delete and on update?
    execute(createForeignKey("FKEasyBeitrag_Mitglied", "easy_beitragsmonat",
        "mitglied", "mitglied", "id", "RESTRICT", "RESTRICT"));

    execute(createForeignKey("FKEasyBeitrag_Buchung", "easy_beitragsmonat",
        "buchung", "buchung", "id", "RESTRICT", "RESTRICT"));

  }
}