package nl.nfi.cellscanner.collect.cellinfo;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import nl.nfi.cellscanner.CellscannerApp;
import nl.nfi.cellscanner.collect.CollectorFactory;
import nl.nfi.cellscanner.collect.DataCollector;
import nl.nfi.cellscanner.collect.DataReceiver;

public class CellInfoCollectorFactory extends CollectorFactory {
    @Override
    public String getTitle() {
        return "cell info";
    }

    @Override
    public String getStatusText() {
        return CellscannerApp.getDatabase().getUpdateStatus();
    }

    @Override
    public DataCollector createCollector(Context ctx) {
        return new CellInfoCollector(new DataReceiver(ctx));
    }

    @Override
    public void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS cellinfo ("+
                "  subscription VARCHAR(20) NOT NULL,"+
                "  date_start INT NOT NULL,"+
                "  date_end INT NOT NULL,"+
                "  registered INT NOT NULL,"+
                "  radio VARCHAR(10) NOT NULL,"+ // radio technology (GSM, UMTS, LTE)
                "  mcc INT NOT NULL,"+
                "  mnc INT NOT NULL,"+
                "  area INT NOT NULL,"+ // Location Area Code (GSM, UMTS) or TAC (LTE)
                "  cid INT NOT NULL,"+ // Cell Identity (GSM: 16 bit; LTE: 28 bit)
                "  bsic INT,"+ // Base Station Identity Code (GSM only)
                "  arfcn INT,"+ // Absolute RF Channel Number (GSM only)
                "  psc INT,"+ // 9-bit UMTS Primary Scrambling Code described in TS 25.331 (UMTS only/)
                "  uarfcn INT,"+ // 16-bit UMTS Absolute RF Channel Number (UMTS only)
                "  pci INT"+ // Physical Cell Id 0..503, Integer.MAX_VALUE if unknown (LTE only)
                ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS cellinfo_date_end ON cellinfo(date_end)");
    }

    @Override
    public void upgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // no upgrade for versions prior to 2
            return;
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE cellinfo ADD COLUMN subscription VARCHAR(20) NOT NULL DEFAULT 'unknown'");
        }
    }

    @Override
    public void dropDataUntil(SQLiteDatabase db, long timestamp) {
        db.delete("cellinfo", "date_end <= ?", new String[]{Long.toString(timestamp)});
    }
}
