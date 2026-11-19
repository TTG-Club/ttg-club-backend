package club.dnd5.portal.util.markup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Отчёт конвертера HTML → разметка core-api: что переведено неоднозначно или потеряно.
 * Ключ — вид замечания (например {@code tag:iframe}, {@code table:colspan}), значение — количество
 * и первый пример.
 */
public class MarkupReport {
	private static final int EXAMPLE_LENGTH = 200;

	private final Map<String, Integer> counts = new TreeMap<>();
	private final Map<String, String> examples = new LinkedHashMap<>();

	/** Отметить замечание {@code kind} с фрагментом исходника для примера. */
	public void note(String kind, String sample) {
		counts.merge(kind, 1, Integer::sum);
		examples.putIfAbsent(kind, sample == null ? "" : abbreviate(sample));
	}

	/** Отметить замечание с указанием, откуда текст (сущность и id) — пример будет с этой ссылкой. */
	public void note(String kind, String sample, String where) {
		counts.merge(kind, 1, Integer::sum);
		examples.putIfAbsent(kind, where + ": " + (sample == null ? "" : abbreviate(sample)));
	}

	public Map<String, Integer> getCounts() {
		return counts;
	}

	public Map<String, String> getExamples() {
		return examples;
	}

	public boolean isEmpty() {
		return counts.isEmpty();
	}

	/** Перенести замечания другого отчёта в этот (пример — с меткой {@code where}). */
	public void addAll(MarkupReport other, String where) {
		other.counts.forEach((kind, count) -> counts.merge(kind, count, Integer::sum));
		other.examples.forEach((kind, sample) -> examples.putIfAbsent(kind, where + ": " + sample));
	}

	private static String abbreviate(String value) {
		String flat = value.replaceAll("\\s+", " ").trim();
		return flat.length() <= EXAMPLE_LENGTH ? flat : flat.substring(0, EXAMPLE_LENGTH) + "…";
	}
}
