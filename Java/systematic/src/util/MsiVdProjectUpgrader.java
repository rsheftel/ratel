package util;

import static db.clause.Clause.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Systematic.*;

import java.util.*;

import db.*;
import db.columns.*;
import db.tables.ScheduleDB.*;

import file.*;

public class MsiVdProjectUpgrader {
    public static final String UPGRADE_CODE = "F254E8AA-36A0-4368-95AF-905D2F067503";

    public static void main(String[] args) {
        Arguments arguments = Arguments.arguments(args, list("useZeros"));
        boolean useZeros = arguments.containsKey("useZeros");
        QFile file = mainDir().file("dotNET\\QExcelSetup\\QExcelSetup.vdproj");
        String content;
        try {
            content = file.text();
            int upgradeIndex = indexOf(content, UPGRADE_CODE);
            bombUnless(content.indexOf(UPGRADE_CODE, upgradeIndex + 1) == -1, 
                "found " + UPGRADE_CODE + " in file twice.");
            int sectionStart = content.lastIndexOf("{\r\n", upgradeIndex) + 2;
            int sectionEnd = content.indexOf("}\r\n", upgradeIndex) + 1;
            String section = content.substring(sectionStart, sectionEnd);
            StringBuilder buf = new StringBuilder(content.substring(0, sectionStart));
            for (String line : split("\r\n", section)) {
                if (line.matches(".*ProductCode.*")) line = "        \"ProductCode\" = \"8:{" + guid(useZeros) + "}\"";
                if (line.matches(".*PackageCode.*")) line = "        \"PackageCode\" = \"8:{" + guid(useZeros) + "}\""; 
                if (line.matches(".*ProductVersion.*")) 
                    line = line.replaceAll("(.*8:).*(\")", "$1" + newVersion() + "$2");
                buf.append(line).append("\r\n");
            }
            buf.append(content.substring(sectionEnd + 2)); 
            QFile backup = file.withSuffix(".bak");
            backup.deleteIfExists();
            file.copyTo(backup);
            file.overwrite(buf.toString());
            info("updated " + file.path() + ", backed up to .bak");
        } catch (RuntimeException e) {
            throw bomb("failed upgrading version numbers in " + file.path(), e);
        }
    }

    private static String newVersion() {
        final String[] newVersion = { "" };
        new Db.Transaction() { 
            @Override public void transact() { 
                NvarcharColumn versionColumn = QVersionBase.T_Q_VERSION.C_Q_VERSION;
                String oldVersion = versionColumn.value(TRUE);
                List<String> parts = split(".", oldVersion);
                parts.set(2, String.valueOf(Integer.valueOf(parts.get(2)) + 1));
                newVersion[0] = join(".", parts);
                versionColumn.updateAll(TRUE, newVersion[0]);
            }
        }.execute();
        return newVersion[0];
    }

    
    private static int indexOf(String content, String toFind) {
        int upgradeIndex = content.indexOf(toFind);
        bombIf(upgradeIndex == -1, "could not find string " + toFind);
        return upgradeIndex;
    }
}
