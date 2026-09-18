package mg.immo.util;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Function;

/** Mini utilitaire JSON : evite d'ajouter une dependance (Jackson/Gson). */
public final class Json {

    private Json() {
    }

    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n");  break;
                case '\r': b.append("\\r");  break;
                case '\t': b.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        return b.toString();
    }

    public static String str(String s) {
        return s == null ? "null" : "\"" + escape(s) + "\"";
    }

    public static <T> String array(Collection<T> items, Function<T, String> mapper) {
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (T item : items) {
            j.add(mapper.apply(item));
        }
        return j.toString();
    }

    public static String ok(String dataJson) {
        return "{\"status\":\"OK\",\"data\":" + dataJson + "}";
    }

    public static String erreur(String message) {
        return "{\"status\":\"ERREUR\",\"message\":" + str(message) + "}";
    }
}
