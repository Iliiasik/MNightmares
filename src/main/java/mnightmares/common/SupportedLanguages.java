package mnightmares.common;

import java.util.List;
import java.util.Locale;

public final class SupportedLanguages {
    public static final String DEFAULT = "en_us";

    private record Entry(String code, String localePrefix) {}

    private static final List<Entry> ENTRIES = List.of(
            new Entry("en_us", "en"),
            new Entry("de_de", "de"),
            new Entry("es_es", "es"),
            new Entry("ru_ru", "ru"),
            new Entry("zh_cn", "zh"),
            new Entry("pt_br", "pt"),
            new Entry("fr_fr", "fr"),
            new Entry("uk_ua", "uk"),
            new Entry("pl_pl", "pl"),
            new Entry("ja_jp", "ja"),
            new Entry("ko_kr", "ko"),
            new Entry("it_it", "it"),
            new Entry("tr_tr", "tr")
    );

    private static final List<String> CODES = ENTRIES.stream().map(Entry::code).toList();

    private SupportedLanguages() {}

    public static List<String> codes() {
        return CODES;
    }

    public static String fromLocale(String locale) {
        if (locale == null) return DEFAULT;
        String normalized = locale.toLowerCase(Locale.ROOT);
        for (Entry entry : ENTRIES) {
            if (normalized.equals(entry.code())) return entry.code();
        }
        for (Entry entry : ENTRIES) {
            if (normalized.startsWith(entry.localePrefix())) return entry.code();
        }
        return DEFAULT;
    }
}
