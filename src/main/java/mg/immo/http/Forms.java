package mg.immo.http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Petit analyseur de corps de requete "application/x-www-form-urlencoded". */
public final class Forms {

    private Forms() {
    }

    public static Map<String, String> parse(String corps) {
        Map<String, String> valeurs = new LinkedHashMap<>();
        if (corps == null || corps.isBlank()) {
            return valeurs;
        }
        for (String paire : corps.split("&")) {
            if (paire.isBlank()) {
                continue;
            }
            int eq = paire.indexOf('=');
            String cle = eq >= 0 ? paire.substring(0, eq) : paire;
            String valeur = eq >= 0 ? paire.substring(eq + 1) : "";
            valeurs.put(URLDecoder.decode(cle, StandardCharsets.UTF_8),
                    URLDecoder.decode(valeur, StandardCharsets.UTF_8));
        }
        return valeurs;
    }

    public static String get(Map<String, String> valeurs, String cle, String defaut) {
        String v = valeurs.get(cle);
        return (v == null || v.isBlank()) ? defaut : v;
    }

    public static double getDouble(Map<String, String> valeurs, String cle, double defaut) {
        try {
            String v = valeurs.get(cle);
            return (v == null || v.isBlank()) ? defaut : Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return defaut;
        }
    }

    public static int getInt(Map<String, String> valeurs, String cle, int defaut) {
        try {
            String v = valeurs.get(cle);
            return (v == null || v.isBlank()) ? defaut : Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaut;
        }
    }
}
