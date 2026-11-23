package club.dnd5.portal.service;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.FeatureOption;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.OptionTypeDefinition;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.repository.classes.ArchetypeTraitRepository;
import club.dnd5.portal.repository.classes.HeroClassTraitRepository;
import club.dnd5.portal.repository.classes.OptionTypeDefinitionRepository;
import club.dnd5.portal.repository.datatable.OptionRepository;
import club.dnd5.portal.util.markup.Markup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Раздел «Особенности классов» как копия вариантов умений.
 * <p>
 * Вариант правится только в умении класса или архетипа ({@code options} у умения, db/changelog 0048);
 * при сохранении класса записи таблицы {@code options} того же типа приводятся к вариантам умения:
 * недостающие создаются, совпадающие по английскому названию обновляются, а выпавшие из умения
 * теряют этот тип (запись без типов удаляется). Так адреса /options/&lt;slug&gt;, поиск и фильтры раздела
 * продолжают работать, а расхождений между умением и разделом нет.
 */
@Service
@RequiredArgsConstructor
public class FeatureOptionProjection {
	/** Требования записи раздела, когда их нет (так хранились прежние записи). */
	public static final String NO_PREREQUISITE = "Нет";

	private final OptionRepository optionRepository;
	private final OptionTypeDefinitionRepository typeRepository;
	private final HeroClassTraitRepository heroClassTraitRepository;
	private final ArchetypeTraitRepository archetypeTraitRepository;
	private final BookResolver bookResolver;

	/**
	 * Приводит варианты из запроса мастерской к хранимому виду: строки обрезаются, варианты без
	 * названия отбрасываются, повторы английского названия — тоже (остаётся первый).
	 *
	 * @param options варианты из запроса; может быть {@code null}
	 * @return варианты для сохранения
	 */
	public static List<FeatureOption> normalize(List<FeatureOption> options) {
		Map<String, FeatureOption> result = new LinkedHashMap<>();
		if (options == null) {
			return new ArrayList<>();
		}
		for (FeatureOption option : options) {
			if (option == null || !StringUtils.hasText(option.getName()) || !StringUtils.hasText(option.getEnglishName())) {
				continue;
			}
			String key = option.getEnglishName().trim().toLowerCase(Locale.ROOT);
			if (result.containsKey(key)) {
				continue;
			}
			FeatureOption normalized = new FeatureOption();
			normalized.setName(option.getName().trim());
			normalized.setEnglishName(option.getEnglishName().trim());
			normalized.setAltName(trimToNull(option.getAltName()));
			normalized.setLevel(option.getLevel());
			normalized.setPrerequisite(trimToNull(option.getPrerequisite()));
			normalized.setAdditional(trimToNull(option.getAdditional()));
			List<String> description = Markup.trimBlocks(option.getDescription());
			normalized.setDescription(description == null ? new ArrayList<>() : description);
			normalized.setRepeatable(option.isRepeatable());
			normalized.setHideInSubclasses(option.isHideInSubclasses());
			normalized.setMechanics(option.getMechanics());
			normalized.setActiveEffects(option.getActiveEffects());
			normalized.setSource(trimToNull(option.getSource()));
			normalized.setPage(option.getPage());
			result.put(key, normalized);
		}
		return new ArrayList<>(result.values());
	}

	/**
	 * Обновляет записи раздела по вариантам одного умения.
	 *
	 * @param typeCode тип списка вариантов умения; пусто — ничего не делается
	 * @param options варианты умения
	 * @param fallbackBook книга класса — для вариантов без своей книги
	 */
	public void sync(String typeCode, List<FeatureOption> options, Book fallbackBook) {
		if (!StringUtils.hasText(typeCode)) {
			return;
		}
		Optional<OptionTypeDefinition> found = typeRepository.findById(typeCode.trim());
		if (found.isEmpty()) {
			return;
		}
		OptionTypeDefinition type = found.get();
		Set<String> kept = new java.util.HashSet<>();
		for (FeatureOption option : options) {
			kept.add(option.getEnglishName().toLowerCase(Locale.ROOT));
			Option entity = optionRepository.findByEnglishName(option.getEnglishName()).orElseGet(Option::new);
			apply(entity, option, type, fallbackBook);
			optionRepository.save(entity);
		}
		for (Option orphan : optionRepository.findAllByType(type)) {
			if (kept.contains(orphan.getEnglishName().toLowerCase(Locale.ROOT))) {
				continue;
			}
			orphan.getOptionTypes().remove(type);
			if (orphan.getOptionTypes().isEmpty()) {
				optionRepository.delete(orphan);
			} else {
				optionRepository.save(orphan);
			}
		}
		optionRepository.flush();
	}

	/**
	 * Варианты типа из раздела «Особенности классов» в виде вариантов умения: для переноса в
	 * мастерской типов, которые миграция не привязала.
	 *
	 * @param type тип списка
	 * @return варианты по уровню и названию
	 */
	public List<FeatureOption> importFromSection(OptionTypeDefinition type) {
		return optionRepository.findAllByType(type).stream()
			.sorted(java.util.Comparator
				.comparing((Option option) -> option.getLevel() == null ? 0 : option.getLevel())
				.thenComparing(Option::getName))
			.map(FeatureOptionProjection::toFeatureOption)
			.collect(Collectors.toList());
	}

	/**
	 * Умение, где правятся варианты записи раздела: первое умение класса или архетипа с одним из
	 * её типов.
	 *
	 * @param option запись раздела
	 * @return адрес умения на странице класса и его название; пусто — запись правится сама
	 */
	public Optional<ManagingFeature> findManagingFeature(Option option) {
		Collection<String> codes = option.getOptionTypes() == null
			? List.of()
			: option.getOptionTypes().stream().map(OptionTypeDefinition::getCode).toList();
		for (String code : codes) {
			Optional<HeroClassTrait> classTrait = heroClassTraitRepository.findFirstByOptionsType(code);
			if (classTrait.isPresent()) {
				HeroClassTrait trait = classTrait.get();
				return Optional.of(new ManagingFeature(
					String.format("/classes/%s#c%d", trait.getHeroClass().getUrlName(), trait.getId()),
					trait.getName()));
			}
			Optional<ArchetypeTrait> archetypeTrait = archetypeTraitRepository.findFirstByOptionsType(code);
			if (archetypeTrait.isPresent()) {
				ArchetypeTrait trait = archetypeTrait.get();
				return Optional.of(new ManagingFeature(
					String.format("/classes/%s/%s#a%d", trait.getArchetype().getHeroClass().getUrlName(),
						trait.getArchetype().getUrlName(), trait.getId()),
					trait.getName()));
			}
		}
		return Optional.empty();
	}

	/** Умение, в котором правится вариант: адрес на странице класса и название. */
	public record ManagingFeature(String url, String name) {
	}

	private void apply(Option entity, FeatureOption option, OptionTypeDefinition type, Book fallbackBook) {
		entity.setName(option.getName());
		entity.setEnglishName(option.getEnglishName());
		entity.setAltName(option.getAltName());
		entity.setLevel(option.getLevel());
		entity.setPrerequisite(option.getPrerequisite() == null ? NO_PREREQUISITE : option.getPrerequisite());
		entity.setDescription(new ArrayList<>(option.getDescription()));
		entity.setMechanics(option.getMechanics());
		entity.setActiveEffects(option.getActiveEffects());
		entity.setPage(option.getPage());
		Book book = bookResolver.find(option.getSource()).orElse(null);
		if (book != null) {
			entity.setBook(book);
		} else if (entity.getBook() == null) {
			entity.setBook(fallbackBook);
		}
		if (entity.getOptionTypes() == null) {
			entity.setOptionTypes(new ArrayList<>());
		}
		if (!entity.getOptionTypes().contains(type)) {
			entity.getOptionTypes().add(type);
		}
	}

	private static FeatureOption toFeatureOption(Option option) {
		FeatureOption result = new FeatureOption();
		result.setName(option.getName());
		result.setEnglishName(option.getEnglishName());
		result.setAltName(option.getAltName());
		result.setLevel(option.getLevel());
		String prerequisite = trimToNull(option.getPrerequisite());
		result.setPrerequisite(NO_PREREQUISITE.equals(prerequisite) ? null : prerequisite);
		result.setDescription(option.getDescription() == null ? new ArrayList<>() : new ArrayList<>(option.getDescription()));
		result.setMechanics(option.getMechanics());
		result.setActiveEffects(option.getActiveEffects());
		result.setSource(option.getBook() == null ? null : option.getBook().getSource());
		result.setPage(option.getPage());
		return result;
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
