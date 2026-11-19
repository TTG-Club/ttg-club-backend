package club.dnd5.portal.util;

import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.CreatureFeat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Разметка описаний существа в html для ответа API не должна менять саму сущность:
 * иначе при создании и правке в мастерской (внутри транзакции) html записывался в базу
 * вместо исходного markdown.
 */
class MarkdownUtilTest {
	private static final String SOURCE = "<p>{@atk mw} {@hit 4} к попаданию. {@h} 5 ({@damage 1к6 + 2}) урона. {@beast_name} рычит.</p>";

	private static Creature goblin() {
		Creature creature = new Creature();
		creature.setName("Гоблин");
		return creature;
	}

	@Test
	void actionHtmlDoesNotChangeEntity() {
		Action action = new Action();
		action.setMarkdown(true);
		action.setDescription(SOURCE);

		String html = MarkdownUtil.html(action, goblin());

		assertThat(action.getDescription()).isEqualTo(SOURCE);
		assertThat(html)
			.contains("<em>Рукопашная атака оружием:</em>")
			.contains("<dice-roller label=\"Атака\" formula=\"к20 + 4\">+4</dice-roller>")
			.contains("<dice-roller label=\"Урон\" formula=\"1к6 + 2\"/>")
			.contains("Гоблин рычит")
			.doesNotContain("{@");
	}

	@Test
	void featHtmlDoesNotChangeEntity() {
		CreatureFeat feat = new CreatureFeat();
		feat.setMarkdown(true);
		feat.setDescription(SOURCE);

		String html = MarkdownUtil.html(feat, goblin());

		assertThat(feat.getDescription()).isEqualTo(SOURCE);
		// внутри предложения имя существа — со строчной буквы, после точки — как есть
		assertThat(html).contains(". Гоблин рычит").doesNotContain("{@");
	}

	@Test
	void notMarkdownReturnedAsIs() {
		Action action = new Action();
		action.setDescription(SOURCE);

		assertThat(MarkdownUtil.html(action, goblin())).isEqualTo(SOURCE);
	}
}
