package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.mechanics.MagicItemMechanics;
import jakarta.validation.Valid;
import club.dnd5.portal.model.items.MagicThingType;
import club.dnd5.portal.model.items.Rarity;
import club.dnd5.portal.model.items.MagicItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;
import club.dnd5.portal.util.StringUtil;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class MagicItemSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	@NotNull
	private Rarity rarity;
	@NotNull
	private MagicThingType type;
	private Boolean customization;
	/** Блоки разметки core-api; HTML-строку (старые ревизии) переводит десериализатор. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> custSpecial;
	/** Блоки разметки core-api; HTML-строку (старые ревизии) переводит десериализатор. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> special;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@NotEmpty
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
	private Boolean consumed;
	private Integer charge;
	private Boolean curse;
	private Integer cost;
	private Byte bonus;

	/** Механика для листа персонажа (модель core-api), сохраняется целиком; {@code null} — оставить как есть. */
	@Valid
	private MagicItemMechanics mechanics;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	/**
	 * Немагическая основа — оружие: адреса страниц оружия без префикса, например {@code longsword}.
	 * Список заменяет сохранённый целиком, порядок сохраняется; {@code null} — оставить как есть.
	 */
	private List<@NotBlank String> weapons;

	/**
	 * Немагическая основа — доспехи: адреса страниц доспехов без префикса, например {@code chain_mail}.
	 * Список заменяет сохранённый целиком, порядок сохраняется; {@code null} — оставить как есть.
	 */
	private List<@NotBlank String> armors;

	public MagicItemSaveApi(MagicItem item) {
		name = item.getName();
		englishName = item.getEnglishName();
		altName = item.getAltName();
		rarity = item.getRarity();
		type = item.getType();
		customization = item.getCustomization();
		custSpecial = item.getCustSpecial();
		special = item.getSpecial();
		description = item.getDescription();
		consumed = item.isConsumed();
		charge = item.getCharge();
		curse = item.getCurse();
		cost = item.getCost();
		bonus = item.getBonus();
		mechanics = item.getMechanics();
		source = item.getBook() == null ? null : item.getBook().getSource();
		weapons = item.getWeapons() == null ? null : item.getWeapons().stream()
			.map(weapon -> StringUtil.getUrl(weapon.getEnglishName()))
			.collect(Collectors.toList());
		armors = item.getArmors() == null ? null : item.getArmors().stream()
			.map(armor -> StringUtil.getUrl(armor.getEnglishName()))
			.collect(Collectors.toList());
	}
}
