package club.dnd5.portal.service;

import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.god.God;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.MagicItem;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.trait.Trait;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Статистика сайта: общее число материалов для счётчика на главной.
 */
@Service
public class StatisticsService {
	/**
	 * Что считается материалом: разделы сайта, у которых есть страница отдельной записи.
	 * <p>
	 * Подклассы и разновидности рас считаются отдельно — у них свои страницы.
	 * Признака скрытой записи у этих сущностей нет ({@code Race.view} управляет только
	 * тем, где показывается разновидность, а не её видимостью), поэтому считаются все строки.
	 * <p>
	 * Не входят: драгоценности и таблицы инструментов (нет страницы записи), источники,
	 * а также служебное — пользователи, закладки, токены, меню, партнёры, новости, видео.
	 */
	static final List<Class<?>> COUNTED_ENTITIES = List.of(
		HeroClass.class,  // classes — классы
		Archetype.class,  // archetypes — подклассы
		Race.class,       // races — расы, происхождения и их разновидности
		Trait.class,      // traits — черты
		Background.class, // backgrounds — предыстории
		Option.class,     // options — особенности классов
		Spell.class,      // spells — заклинания
		Weapon.class,     // weapons — оружие
		Armor.class,      // armors — доспехи
		Equipment.class,  // equipments — снаряжение
		MagicItem.class,  // artifactes — магические предметы
		Creature.class,   // creatures — бестиарий
		God.class,        // gods — боги
		Rule.class,       // rules — правила и термины
		Screen.class      // screens — ширма (справочник)
	);

	/** Число меняется редко, а главную открывают часто — пересчитываем не чаще раза в 10 минут. */
	static final Duration CACHE_TTL = Duration.ofMinutes(10);

	private final EntityManager entityManager;
	private final LongSupplier clockMillis;

	private volatile CachedCount cached;

	@Autowired
	public StatisticsService(EntityManager entityManager) {
		this(entityManager, System::currentTimeMillis);
	}

	StatisticsService(EntityManager entityManager, LongSupplier clockMillis) {
		this.entityManager = entityManager;
		this.clockMillis = clockMillis;
	}

	/**
	 * @return общее число материалов сайта; значение кешируется в памяти на {@link #CACHE_TTL}
	 */
	@Transactional(readOnly = true)
	public Long countAllMaterials() {
		long now = clockMillis.getAsLong();
		CachedCount current = cached;
		if (current != null && now < current.expiresAt()) {
			return current.value();
		}
		// Одновременный пересчёт из нескольких потоков безвреден: результат одинаковый
		long total = COUNTED_ENTITIES.stream()
			.mapToLong(this::count)
			.sum();
		cached = new CachedCount(total, now + CACHE_TTL.toMillis());
		return total;
	}

	private long count(Class<?> entity) {
		return entityManager
			.createQuery("SELECT COUNT(e) FROM " + entity.getName() + " e", Long.class)
			.getSingleResult();
	}

	private record CachedCount(long value, long expiresAt) {
	}
}
