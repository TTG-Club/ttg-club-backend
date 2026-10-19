package club.dnd5.portal.service.vttg;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Дерево разделов компендиума VTTG для ответа {@code /changes}.
 *
 * <p>Листы называют папку ({@code section}) и тип записей ({@code dataKind}); слаги
 * совпадают с {@code section} записей. Отображение заклинаний — то же, что у нового
 * сайта: группировка и фильтр по кругу, концентрация и класс.</p>
 */
final class VttgSections {
	private VttgSections() {
	}

	static List<Map<String, Object>> tree() {
		return Arrays.asList(
			leaf(VttgSpellMapper.SECTION, "Заклинания", "tabler:sparkles", "spell", spellView()),
			leaf(VttgRaceMapper.SECTION, "Расы", "tabler:users", "species", null),
			leaf(VttgBackgroundMapper.SECTION, "Предыстории", "tabler:book", "background", null),
			leaf(VttgFeatMapper.SECTION, "Черты", "tabler:star", "feat", null)
		);
	}

	private static Map<String, Object> leaf(String section, String name, String icon, String dataKind,
											Map<String, Object> view) {
		Map<String, Object> node = new LinkedHashMap<>();
		node.put("section", section);
		node.put("name", name);
		node.put("icon", icon);
		node.put("dataKind", dataKind);
		if (view != null) {
			node.put("view", view);
		}
		return node;
	}

	private static Map<String, Object> spellView() {
		Map<String, Object> groupBy = new LinkedHashMap<>();
		groupBy.put("path", "level");
		groupBy.put("format", "spellLevel");

		Map<String, Object> concentration = new LinkedHashMap<>();
		concentration.put("label", "Концентрация");
		concentration.put("path", "concentration");
		concentration.put("icon", "tabler:eye");
		concentration.put("color", "warning");

		Map<String, Object> ritual = new LinkedHashMap<>();
		ritual.put("label", "Ритуал");
		ritual.put("path", "ritual");
		ritual.put("icon", "tabler:book");
		ritual.put("color", "info");

		Map<String, Object> toggles = new LinkedHashMap<>();
		toggles.put("id", "props");
		toggles.put("label", "Свойства");
		toggles.put("type", "toggles");
		toggles.put("toggles", Arrays.asList(concentration, ritual));

		Map<String, Object> view = new LinkedHashMap<>();
		view.put("layout", "filtered");
		view.put("groupBy", groupBy);
		view.put("filters", Arrays.asList(
			enumFilter("level", "Круг", "level", "spellLevel", "badges"),
			toggles,
			enumFilter("class", "Класс", "classKeys", "spellClass", "list")
		));
		return view;
	}

	private static Map<String, Object> enumFilter(String id, String label, String path, String format, String style) {
		Map<String, Object> filter = new LinkedHashMap<>();
		filter.put("id", id);
		filter.put("label", label);
		filter.put("type", "enum");
		filter.put("path", path);
		filter.put("format", format);
		filter.put("style", style);
		return filter;
	}
}
