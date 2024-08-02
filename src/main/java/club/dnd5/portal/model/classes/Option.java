package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "options")
public class Option extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	@ElementCollection(targetClass = OptionType.class)
	@JoinTable(name = "option_types", joinColumns = @JoinColumn(name = "option_id"))
	@Column(name = "option_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<OptionType> optionTypes;

	private String prerequisite;
	private Integer level;

	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	@AllArgsConstructor
	@Getter
	public enum OptionType {
		WILD_SHAPE("Формы Дикого Облика: Друид", "Druid", false),
		ARCANE_SHOT("Магические выстрелы: Воин Мистический Лучник", "Fighter", "Arcane_Archer", false),
		METAMAGIC("Метамагия: Чародей", "Sorcerer", false),
		ELDRITCH_INVOCATION("Воззвания: Колдун","Warlock", false),
		BONE("Договоры: Колдун", "Warlock", false),
		ELEMENTAL_DISCIPLINE("Стихийные практики: Монах Пути четырех стихий", "Monk", "Four_Elements", false),
		ARTIFICER_INFUSION("Инфузии: Изобретатель","Artificer", false),
		RUNE("Руны: Рунический рыцарь", "Fighter", "Rune", false),
		MANEUVER("Маневры: Воин Мастер боевых искуств", "Fighter","Battle_Master", false),
		FIGHTING_STYLE("Боевые стили: Воин", "Fighter", false),
		FIGHTING_STYLE_RANGER("Боевые стили: Следопыт", "Ranger", false),
		FIGHTING_STYLE_PALADIN("Боевые стили: Паладин", "Paladin", false),
		FIGHTING_STYLE_BARD("Боевые стили: Бард Колллегии Мечей", "Bard", "Swords", false),
		FIGHTING_STYLE_BLOODHANTER("Боевые стили: Кровавый охотник", "Blood Hunter", true),
		BLOOD_CURSE("Проклятья крови: Кровавый охотник", "Blood Hunter", true),
		MUTAGEN("Мутагены: Кровавый охотник Ордена мутантов", "Blood Hunter", true),
		PHILOSOPHICAL_SCHOOL("Философские школы: Волшебник Философ Академии","Wizard", true),
		BOMB_FORMULA("Формулы бомб", "Alchemist", true),
		DISCOVERIES("Открытия","Alchemist", true),
		TRANSPLANTS("Трансплантаты","Alchemist", true),
		BONUS_DISCIPLINES("Псионические дисциплины: Мистик","Mystic", false),
		PSIONIC_TALANT("Псионические таланты: Мистик", "Mystic", false),
		ACADEMIC_DISCIPLINES("Научные дисциплиныЖ Савант", "Savant", true),
		TOTEMS("Тотемы: Шаман", "Shaman", true),
		FIGHTING_STYLE_ALT_FIGHTER("Боевые стили: Воин", "Fighter", true),
		MARTIAL_EXPLOITS("Боевые приёмы: Адьтернативный воин", "Alternate Fighter", true),
		SIGNATURE_TECHNIQUES("Фирменные техники: Альтернативный монах", "Alternate Monk", true);

		private final String name;
		private final  String className;
		private String arhetypeName;
		private final boolean homebrew;

		OptionType(String name, String className, boolean homebrew){
			this.name = name;
			this.className = className;
			this.homebrew = homebrew;
		}

		public static OptionType parse(String type) {
			return Arrays.stream(values())
					.filter(t -> t.name.equals(type))
					.findFirst()
					.orElseThrow(IllegalArgumentException::new);
		}

		public String getDisplayName() {
			return name.contains(":") ? name.substring(0, name.indexOf(":")) : name;
		}
	}
}
