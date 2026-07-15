package club.dnd5.portal.controller.tools;

import club.dnd5.portal.dto.api.tools.name.GeneratedNameApi;
import club.dnd5.portal.dto.api.tools.name.NameGenerationFormat;
import club.dnd5.portal.dto.api.tools.name.NameGenerationRequest;
import club.dnd5.portal.dto.api.tools.name.NameGenerationType;
import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.model.tavern.Atmosphere;
import club.dnd5.portal.model.tavern.OwnerSecret;
import club.dnd5.portal.model.tavern.OwnerTrait;
import club.dnd5.portal.model.tavern.OwnerWeakness;
import club.dnd5.portal.model.tavern.TavernaCategory;
import club.dnd5.portal.model.tavern.TavernaDish;
import club.dnd5.portal.model.tavern.TavernaDrink;
import club.dnd5.portal.model.tavern.RandomEvent;
import club.dnd5.portal.model.tavern.TavernaName;
import club.dnd5.portal.model.tavern.TavernaPrefixName;
import club.dnd5.portal.model.tavern.TavernaType;
import club.dnd5.portal.model.tavern.RaceHabitatChance;
import club.dnd5.portal.model.tavern.TopicDiscussed;
import club.dnd5.portal.model.tavern.Visitor;
import club.dnd5.portal.model.tavern.VisitorChance;
import club.dnd5.portal.repository.tavern.AtmosphereRepository;
import club.dnd5.portal.repository.tavern.OwnerSecretRepository;
import club.dnd5.portal.repository.tavern.OwnerTraitRepository;
import club.dnd5.portal.repository.tavern.OwnerWeaknessRepository;
import club.dnd5.portal.repository.tavern.TavernaDishRepository;
import club.dnd5.portal.repository.tavern.TavernaDrinkRepository;
import club.dnd5.portal.repository.tavern.TavernaNameRepository;
import club.dnd5.portal.repository.tavern.RandomEventRepository;
import club.dnd5.portal.repository.tavern.TavernaPrefixNameRepository;
import club.dnd5.portal.repository.tavern.RaceHabitatChanceRepository;
import club.dnd5.portal.repository.tavern.TopicDiscussedRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.repository.tavern.VisitorRepository;
import club.dnd5.portal.service.NameGeneratorService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Hidden
@Controller
public class TavernToolController {
	private static final Random rnd = new Random();

	private final TavernaNameRepository nameRepo;
	private final TavernaPrefixNameRepository prefixRepo;
	private final AtmosphereRepository atmosphereRepo;
	private final TavernaDishRepository dishRepo;
	private final TavernaDrinkRepository drinkRepo;
	private final VisitorRepository visitorRepo;
	private final TopicDiscussedRepository topicRepo;
	private final RandomEventRepository eventRepo;
	private final OwnerTraitRepository ownerTraitRepo;
	private final OwnerWeaknessRepository ownerWeaknessRepo;
	private final OwnerSecretRepository ownerSecretRepo;
	private final RaceRepository raceRepository;
	private final RaceHabitatChanceRepository raceHabitatRepo;
	private final NameGeneratorService nameGeneratorService;

	private final Set<String> generatedNames = new HashSet<>();

	// Кэш рас (id + англ. слаг): список не меняется за время жизни приложения, а findAll
	// по всем расам с их связями дорогой, поэтому грузим один раз.
	private volatile List<RacePick> raceCache;

	// Максимум visitors среди всех атмосфер — используется как 100%-заполненность зала.
	private volatile Integer maxAtmosphereVisitors;

	// Базовый вес расы: для местности/расы без явной строки в taverna_race_habitates
	// (человек, неизвестные расы, а также любая раса в незаданной местности).
	private static final int BASE_RACE_WEIGHT = 3;

	// Кэш правил «раса → местность → вес» из taverna_race_habitates. Отсортирован по
	// длине ключа (сначала самые специфичные), данные не меняются за время жизни приложения.
	private volatile List<RaceHabitatWeights> raceHabitatCache;

	@GetMapping("/tools/tavern/name")
	@ResponseBody
	public String getTreasuryTool(String tavernaType) {
		List<TavernaName> tavernaNames = nameRepo.findAll();
		List<TavernaPrefixName> prefixes = prefixRepo.findAll();
		TavernaType type;
		if (tavernaType == null) {
			type = TavernaType.values()[rnd.nextInt(TavernaType.values().length)];
		} else {
			type = TavernaType.valueOf(tavernaType);
		}
		String tavernName;
		do {
			int index = rnd.nextInt(tavernaNames.size());
			TavernaName tavernaName = tavernaNames.get(index);
			index = rnd.nextInt(prefixes.size());
			TavernaPrefixName prefix = prefixes.get(index);
			if (prefix.getObjectType() != null) {
				tavernaNames = tavernaNames.stream()
						.filter(n -> n.getObjectType() == prefix.getObjectType())
						.collect(Collectors.toList());
				index = rnd.nextInt(tavernaNames.size());
				tavernaName = tavernaNames.get(index);
			}
			int nameType = rnd.nextInt(100);
			if (nameType > 85) {
				tavernaNames = tavernaNames.stream()
						.filter(n -> n.getNames() != null)
						.collect(Collectors.toList());
				index = rnd.nextInt(tavernaNames.size());
				TavernaName name = tavernaNames.get(index);
				tavernName = type.getName() + " \"";
				switch (rnd.nextInt(5)) {
				case 0:
					tavernName += "Три ";
					break;
				case 1:
					tavernName += "Четыре ";
					break;
				default:
					tavernName += name.getSex() == Sex.FEMALE ? "Две " : "Два ";
					break;
				}
				tavernName += tavernaNames.get(index).getNames() + "\"";
			} else if (nameType > 65) {
				// «{имя} [префикс] {имя2 в родительном}» — напр. «Череп Красного Дракона»
				List<TavernaName> withGenitive = tavernaNames.stream()
						.filter(n -> n.getNames() != null)
						.collect(Collectors.toList());
				TavernaName first = withGenitive.get(rnd.nextInt(withGenitive.size()));
				int secondIndex = rnd.nextInt(withGenitive.size());
				if (withGenitive.size() > 1 && withGenitive.get(secondIndex) == first) {
					secondIndex = (secondIndex + 1) % withGenitive.size();
				}
				TavernaName second = withGenitive.get(secondIndex);
				String middle = "";
				if (rnd.nextInt(100) < 45) {
					List<TavernaPrefixName> compatible = prefixes.stream()
							.filter(p -> p.getObjectType() == null || p.getObjectType() == second.getObjectType())
							.collect(Collectors.toList());
					if (!compatible.isEmpty()) {
						TavernaPrefixName pfx = compatible.get(rnd.nextInt(compatible.size()));
						middle = genitivePrefix(pfx.getName(second.getSex()), second.getSex()) + " ";
					}
				}
				tavernName = type.getName() + " \"" + first.getName() + " " + middle + second.getNames() + "\"";
			} else if (nameType > 50) {
				index = rnd.nextInt(tavernaNames.size());
				TavernaName tavernaName2 = tavernaNames.get(index);
				tavernName = type.getName() + " \"" + tavernaName.getName() + " и " + tavernaName2.getName() + "\"";
			} else if (nameType > 40) {
				index = rnd.nextInt(tavernaNames.size());
				TavernaName tavernaName2 = tavernaNames.get(index);
				tavernName = type.getName() + " \"" + prefix.getName(tavernaName.getSex()) + " " + tavernaName.getName()
						+ " и " + tavernaName2.getName() + "\"";
			} else {
				tavernName = type.getName() + " \"" + prefix.getName(tavernaName.getSex()) + " " + tavernaName.getName()
						+ "\"";
			}
		} while (generatedNames.contains(tavernName));
		if (generatedNames.size() < 500) {
			generatedNames.add(tavernName);
		} else {
			generatedNames.clear();
		}
		return tavernName;
	}

	@GetMapping("/tools/tavern/atmosphere/")
	@ResponseBody
	public String getAtmosphere() {
		List<Atmosphere> atmospheres = atmosphereRepo.findAll();
		Atmosphere atmosphere = atmospheres.get(rnd.nextInt(atmospheres.size()));
		// data-visitors — уровень заполненности зала, от него зависит число посетителей за столиками
		return "<div data-visitors=\"" + atmosphere.getVisitors() + "\"><h5>Атмосфера: " + atmosphere.getName()
				+ "</h5> <br>" + atmosphere.getDescription() + "</div>";
	}

	@GetMapping("/tools/tavern/menu")
	@ResponseBody
	public String getMenu(@RequestParam(required = false) String tavernaType,
			@RequestParam(required = false) String habitat,
			@RequestParam(required = false) String serviceLevel) {
		HabitatType habitatType = resolveHabitat(habitat);
		TavernaType type = resolveType(tavernaType);
		TavernaCategory category = resolveCategory(serviceLevel);

		List<TavernaDish> dishes = dishRepo.findByHabitat(habitatType);
		if (dishes.isEmpty()) {
			dishes = dishRepo.findAll();
		}
		dishes = filterByCategory(dishes, category, TavernaDish::getCategory);
		List<TavernaDrink> drinks = drinkRepo.findByHabitat(habitatType);
		if (drinks.isEmpty()) {
			drinks = drinkRepo.findAll();
		}
		drinks = filterByCategory(drinks, category, TavernaDrink::getCategory);

		List<TavernaDish> pickedDishes = pickRandom(dishes, dishCount(type));
		List<TavernaDrink> pickedDrinks = pickRandom(drinks, 4);

		if (pickedDishes.isEmpty() && pickedDrinks.isEmpty()) {
			return "<h5>Меню</h5> <br>Меню этого заведения пока пустует.";
		}

		// Категорию у позиций не выводим (уровень обслуживания един для всего меню и
		// показан в шапке), но по нему проставляем цену — как в настоящем меню.
		StringBuilder sb = new StringBuilder("<h5>Меню</h5>");
		if (!pickedDishes.isEmpty()) {
			sb.append("<p><b>Кухня</b></p><ul>");
			for (TavernaDish dish : pickedDishes) {
				sb.append("<li>").append(dish.getName())
						.append(" — ").append(menuPrice(category, true)).append("</li>");
			}
			sb.append("</ul>");
		}
		if (!pickedDrinks.isEmpty()) {
			sb.append("<p><b>Напитки</b></p><ul>");
			for (TavernaDrink drink : pickedDrinks) {
				sb.append("<li>").append(drink.getName())
						.append(" — ").append(menuPrice(category, false)).append("</li>");
			}
			sb.append("</ul>");
		}
		return sb.toString();
	}

	@GetMapping("/tools/tavern/rumors")
	@ResponseBody
	public String getRumors(@RequestParam(required = false) Integer atmosphereVisitors) {
		String header = "<h5>Тема случайно услышанного разговора</h5> <br>";
		// visitors у темы — минимум посетителей, при котором её могут обсуждать
		List<TopicDiscussed> topics = atmosphereVisitors == null
				? topicRepo.findAll()
				: topicRepo.findByVisitorsLessThanEqual(atmosphereVisitors);
		if (topics.isEmpty()) {
			topics = topicRepo.findAll();
		}
		if (topics.isEmpty()) {
			return header + "Сегодня в зале тихо — ни одной свежей сплетни.";
		}
		TopicDiscussed topic = topics.get(rnd.nextInt(topics.size()));
		return header + topic.getName();
	}

	@GetMapping("/tools/tavern/event")
	@ResponseBody
	public String getEvent(@RequestParam(required = false) Integer atmosphereVisitors) {
		String header = "<h5>Случайное событие</h5> <br>";
		// visitors у события — минимум посетителей, при котором оно может произойти
		List<RandomEvent> events = atmosphereVisitors == null
				? eventRepo.findAll()
				: eventRepo.findByVisitorsLessThanEqual(atmosphereVisitors);
		if (events.isEmpty()) {
			events = eventRepo.findAll();
		}
		if (events.isEmpty()) {
			return header + "Пока всё идёт своим чередом — ничего необычного.";
		}
		RandomEvent event = events.get(rnd.nextInt(events.size()));
		return header + event.getDescription();
	}

	@GetMapping("/tools/tavern/tables")
	@ResponseBody
	public String getTables(@RequestParam(required = false) String tavernaType,
			@RequestParam(required = false) Integer atmosphereVisitors,
			@RequestParam(required = false) String serviceLevel,
			@RequestParam(required = false) String habitat) {
		TavernaType type = resolveType(tavernaType);
		TavernaCategory category = resolveCategory(serviceLevel);
		// Местность влияет на то, какие расы чаще встречаются за столиками. Если не
		// передана — расы выбираются равномерно (прежнее поведение).
		HabitatType habitatType = resolveHabitatOrNull(habitat);

		int totalTables = tableCount(type);
		int occupied = occupiedTables(totalTables, atmosphereVisitors);

		StringBuilder sb = new StringBuilder("<h5>Столики и посетители</h5>");
		sb.append("<p>Всего столиков: <b>").append(totalTables)
				.append("</b>, из них занято: <b>").append(occupied).append("</b>.</p>");

		List<Visitor> visitors = visitorRepo.findAll();
		if (occupied > 0 && !visitors.isEmpty()) {
			int[] perTable = new int[occupied];
			int totalPeople = 0;
			for (int i = 0; i < occupied; i++) {
				perTable[i] = 1 + rnd.nextInt(6); // за столиком 1–6 посетителей
				totalPeople += perTable[i];
			}
			List<GeneratedNameApi> people = generateVisitorEntries(totalPeople, habitatType);
			// Пул уже содержит несколько рас. Группируем его по расам и раздаём осознанно:
			// часть столиков делаем мономрасовыми, часть — смешанными (без лишних запросов).
			List<List<GeneratedNameApi>> raceGroups = groupByRace(people);
			sb.append("<ul>");
			for (int i = 0; i < occupied; i++) {
				List<GeneratedNameApi> seated = seatTable(raceGroups, perTable[i]);
				int seatedIndex = 0;
				sb.append("<li>Столик ").append(i + 1)
						.append(" (посетителей: ").append(perTable[i]).append("):<ul>");
				for (int j = 0; j < perTable[i]; j++) {
					Visitor visitor = pickVisitor(visitors, type, category);
					String visitorType = visitor == null ? "случайный посетитель" : visitor.getName();
					GeneratedNameApi person = seatedIndex < seated.size() ? seated.get(seatedIndex++) : null;
					sb.append("<li>");
					if (person != null) {
						sb.append(person.getValue()).append(" (");
						if (person.getRace() != null) {
							sb.append(person.getRace()).append(", ");
						}
						sb.append(visitorType).append(')');
					} else {
						sb.append(visitorType);
					}
					sb.append("</li>");
				}
				sb.append("</ul></li>");
			}
			sb.append("</ul>");
		}
		return sb.toString();
	}

	@GetMapping("/tools/tavern/bartender")
	@ResponseBody
	public String getBartender(@RequestParam(required = false) String habitat) {
		// Раса хозяина зависит от местности так же, как и у посетителей.
		HabitatType habitatType = resolveHabitatOrNull(habitat);
		Sex ownerSex = rnd.nextBoolean() ? Sex.MALE : Sex.FEMALE;
		GeneratedNameApi owner = generateOwner(ownerSex, habitatType);
		String name = owner != null ? owner.getValue() : "Безымянный";
		String title = ownerSex == Sex.FEMALE ? "Хозяйка заведения" : "Хозяин заведения";

		// Каждое свойство — с новой строки через <br> (как в atmosphere/rumors): подряд
		// идущие <p> в SPA схлопывались в одну строку.
		StringBuilder sb = new StringBuilder("<h5>").append(title).append("</h5>");
		sb.append("<b>").append(name).append("</b>");
		if (owner != null && owner.getRace() != null) {
			sb.append("<br>Раса: ").append(owner.getRace());
		}

		OwnerTrait trait = pickOne(ownerTraitRepo.findBySexOrSexIsNull(ownerSex));
		if (trait != null) {
			sb.append("<br>Черта характера: ").append(trait.getDescription());
		}
		OwnerWeakness weakness = pickOne(ownerWeaknessRepo.findBySexOrSexIsNull(ownerSex));
		if (weakness != null) {
			sb.append("<br>Слабость: ").append(weakness.getDescription());
		}
		OwnerSecret secret = pickOne(ownerSecretRepo.findBySexOrSexIsNull(ownerSex));
		if (secret != null) {
			sb.append("<br>Секрет: ").append(secret.getDescription());
		}
		return sb.toString();
	}

	private GeneratedNameApi generateOwner(Sex ownerSex, HabitatType habitat) {
		// Расу выбираем с учётом местности и передаём её id, чтобы генератор имён грузил
		// имена только для неё (иначе ленивая подгрузка имён всех рас даёт N+1 и таймаут).
		List<RacePick> races = getRaces();
		for (int attempt = 0; attempt < 10 && !races.isEmpty(); attempt++) {
			Integer raceId = pickRaceByHabitat(races, habitat).id;
			try {
				NameGenerationRequest request = new NameGenerationRequest();
				request.setType(NameGenerationType.SINGLE);
				request.setFormat(NameGenerationFormat.ANY);
				request.setCount(1);
				request.setRaceId(raceId);
				request.setSexes(EnumSet.of(ownerSex, Sex.UNISEX));

				List<GeneratedNameApi> names = nameGeneratorService.generate(request);
				if (!names.isEmpty()) {
					return names.get(0);
				}
			} catch (RuntimeException ignored) {
				// у выбранной расы нет подходящих имён — пробуем другую
			}
		}
		return null;
	}

	// Имена и расы посетителей за столиками. Генерируем пачками (GROUP) по несколько
	// случайных рас: это даёт разнообразие рас среди гостей и при этом ограничивает
	// число обращений к генератору (иначе ленивая подгрузка имён по каждому даёт N+1).
	// Расу пачки выбираем с учётом местности: в горах чаще дварфы, в подземье — дроу
	// и дуэргары, в лесу — эльфы и т.д. (см. raceHabitatWeight).
	private List<GeneratedNameApi> generateVisitorEntries(int count, HabitatType habitat) {
		if (count <= 0) {
			return Collections.emptyList();
		}
		List<RacePick> races = getRaces();
		if (races.isEmpty()) {
			return Collections.emptyList();
		}
		List<GeneratedNameApi> result = new ArrayList<>();
		Set<String> usedNames = new HashSet<>();
		// не больше 12 обращений: хватает на разнообразие рас без риска таймаута
		for (int attempt = 0; attempt < 12 && result.size() < count; attempt++) {
			Integer raceId = pickRaceByHabitat(races, habitat).id;
			int chunk = Math.min(count - result.size(), 6);
			try {
				NameGenerationRequest request = new NameGenerationRequest();
				request.setType(NameGenerationType.GROUP);
				request.setFormat(NameGenerationFormat.ANY);
				request.setCount(chunk);
				request.setRaceId(raceId);
				request.setSexes(EnumSet.of(Sex.MALE, Sex.FEMALE, Sex.UNISEX));

				for (GeneratedNameApi name : nameGeneratorService.generate(request)) {
					if (usedNames.add(name.getValue())) {
						result.add(name);
						if (result.size() == count) {
							break;
						}
					}
				}
			} catch (RuntimeException ignored) {
				// у выбранной расы недостаточно уникальных имён — пробуем другую
			}
		}
		return result;
	}

	// Группирует сгенерированный пул посетителей по расам. Пустые/неизвестные расы
	// собираются в одну группу. Списки внутри — изменяемые: из них «вычёрпываются»
	// гости при рассадке.
	private List<List<GeneratedNameApi>> groupByRace(List<GeneratedNameApi> people) {
		Map<String, List<GeneratedNameApi>> byRace = new LinkedHashMap<>();
		for (GeneratedNameApi person : people) {
			String race = person.getRace() == null ? "" : person.getRace();
			byRace.computeIfAbsent(race, r -> new ArrayList<>()).add(person);
		}
		return new ArrayList<>(byRace.values());
	}

	// Набирает посетителей за один столик из сгруппированного по расам пула.
	// В ~55% случаев столик получается мономрасовым (если есть раса, которой хватает
	// на всех), иначе — смешанный состав: гости берутся по кругу из разных рас.
	// Взятые гости удаляются из групп, чтобы не попасть за другой столик.
	private List<GeneratedNameApi> seatTable(List<List<GeneratedNameApi>> raceGroups, int need) {
		List<GeneratedNameApi> seated = new ArrayList<>();
		boolean preferMono = rnd.nextInt(100) < 55;
		if (preferMono) {
			List<List<GeneratedNameApi>> fitting = raceGroups.stream()
					.filter(group -> group.size() >= need)
					.collect(Collectors.toList());
			if (!fitting.isEmpty()) {
				List<GeneratedNameApi> group = fitting.get(rnd.nextInt(fitting.size()));
				while (seated.size() < need && !group.isEmpty()) {
					seated.add(group.remove(group.size() - 1));
				}
				return seated;
			}
			// нет расы, которой хватило бы на весь столик, — собираем смешанный
		}
		// смешанный столик: по одному гостю из каждой непустой расы по кругу
		while (seated.size() < need) {
			boolean tookAny = false;
			for (List<GeneratedNameApi> group : raceGroups) {
				if (group.isEmpty()) {
					continue;
				}
				seated.add(group.remove(group.size() - 1));
				tookAny = true;
				if (seated.size() == need) {
					break;
				}
			}
			if (!tookAny) {
				break; // пул исчерпан — оставшиеся места заполнит «случайный посетитель»
			}
		}
		return seated;
	}

	private List<RacePick> getRaces() {
		List<RacePick> cached = raceCache;
		if (cached == null) {
			// один запрос: id + слаг рас, у которых есть имена (без N+1 по всем расам)
			cached = raceRepository.findRacesWithNames().stream()
					.map(r -> new RacePick(r.getId(), r.getEnglishName()))
					.collect(Collectors.toList());
			raceCache = cached;
		}
		return cached;
	}

	// Выбор расы с учётом местности: вес расы для местности задаёт raceHabitatWeight.
	// Если суммарный вес нулевой (напр. местность не передана и все веса базовые) —
	// работает как равномерный выбор.
	private RacePick pickRaceByHabitat(List<RacePick> races, HabitatType habitat) {
		int totalWeight = 0;
		for (RacePick race : races) {
			totalWeight += raceHabitatWeight(race.englishName, habitat);
		}
		if (totalWeight <= 0) {
			return races.get(rnd.nextInt(races.size()));
		}
		int roll = rnd.nextInt(totalWeight);
		for (RacePick race : races) {
			roll -= raceHabitatWeight(race.englishName, habitat);
			if (roll < 0) {
				return race;
			}
		}
		return races.get(races.size() - 1);
	}

	// Вес расы для данной местности по данным taverna_race_habitates. Без местности или
	// для расы без правил (человек, неизвестные) — базовый вес. Для расы с правилом берётся
	// вес её местности, иначе вес строки habitat=NULL (базовый вес расы), иначе BASE.
	private int raceHabitatWeight(String englishName, HabitatType habitat) {
		if (habitat == null || englishName == null) {
			return BASE_RACE_WEIGHT;
		}
		String slug = englishName.toLowerCase();
		// правила отсортированы по длине ключа: сначала самое специфичное совпадение,
		// чтобы «half-elf» не попал под «elf», а «deep-gnome» — под «gnome».
		for (RaceHabitatWeights rule : getRaceHabitatWeights()) {
			if (slug.contains(rule.key)) {
				Integer chance = rule.byHabitat.get(habitat);
				if (chance != null) {
					return chance;
				}
				return rule.fallback != null ? rule.fallback : BASE_RACE_WEIGHT;
			}
		}
		return BASE_RACE_WEIGHT;
	}

	private List<RaceHabitatWeights> getRaceHabitatWeights() {
		List<RaceHabitatWeights> cached = raceHabitatCache;
		if (cached == null) {
			Map<String, RaceHabitatWeights> byKey = new LinkedHashMap<>();
			for (RaceHabitatChance row : raceHabitatRepo.findAll()) {
				if (row.getEnglishName() == null) {
					continue;
				}
				RaceHabitatWeights rule = byKey.computeIfAbsent(
						row.getEnglishName().toLowerCase(), RaceHabitatWeights::new);
				if (row.getHabitat() == null) {
					rule.fallback = row.getChance();
				} else {
					rule.byHabitat.put(row.getHabitat(), row.getChance());
				}
			}
			cached = new ArrayList<>(byKey.values());
			// длинные ключи первыми — самое специфичное совпадение по подстроке
			cached.sort(Comparator.comparingInt((RaceHabitatWeights r) -> r.key.length()).reversed());
			raceHabitatCache = cached;
		}
		return cached;
	}

	private Visitor pickVisitor(List<Visitor> visitors, TavernaType type, TavernaCategory category) {
		int totalWeight = 0;
		for (Visitor visitor : visitors) {
			totalWeight += visitorWeight(visitor, type, category);
		}
		if (totalWeight <= 0) {
			return visitors.get(rnd.nextInt(visitors.size()));
		}
		int roll = rnd.nextInt(totalWeight);
		for (Visitor visitor : visitors) {
			roll -= visitorWeight(visitor, type, category);
			if (roll < 0) {
				return visitor;
			}
		}
		return visitors.get(visitors.size() - 1);
	}

	// Вес посетителя для заведения данного типа и уровня обслуживания (категории).
	// Строка шанса учитывается, если совпадает по типу и по категории; при этом
	// незаданные (null) тип или категория в строке считаются подходящими к любому —
	// так старые данные без категории продолжают работать по типу, как раньше.
	private int visitorWeight(Visitor visitor, TavernaType type, TavernaCategory category) {
		if (visitor.getChance() == null) {
			return 0;
		}
		int weight = 0;
		for (VisitorChance chance : visitor.getChance()) {
			boolean typeMatches = chance.getTavernaType() == null || chance.getTavernaType() == type;
			boolean categoryMatches = category == null
					|| chance.getTavernaCategory() == null
					|| chance.getTavernaCategory() == category;
			if (typeMatches && categoryMatches) {
				weight += chance.getChance();
			}
		}
		return weight;
	}

	// Число занятых столиков зависит от атмосферы: доля от максимума заполненности зала.
	// Если атмосфера не передана — прежнее случайное заполнение 40–90%.
	private int occupiedTables(int totalTables, Integer atmosphereVisitors) {
		if (totalTables == 0) {
			return 0;
		}
		if (atmosphereVisitors == null) {
			int random = (int) Math.round(totalTables * (0.4 + rnd.nextDouble() * 0.5));
			return Math.min(Math.max(1, random), totalTables);
		}
		if (atmosphereVisitors <= 0) {
			return 0; // тихий/пустой зал
		}
		int maxVisitors = getMaxAtmosphereVisitors();
		double ratio = maxVisitors > 0 ? Math.min(1.0, (double) atmosphereVisitors / maxVisitors) : 0.5;
		int occupied = (int) Math.round(totalTables * ratio);
		return Math.min(Math.max(1, occupied), totalTables);
	}

	private int getMaxAtmosphereVisitors() {
		Integer cached = maxAtmosphereVisitors;
		if (cached == null) {
			cached = atmosphereRepo.findAll().stream()
					.mapToInt(Atmosphere::getVisitors)
					.max()
					.orElse(0);
			maxAtmosphereVisitors = cached;
		}
		return cached;
	}

	// Число блюд в меню зависит от типа заведения.
	private int dishCount(TavernaType type) {
		switch (type) {
		case BEER:
		case GAMBLING_DEN:
			return 1 + rnd.nextInt(2); // 1–2
		case CAFE:
			return 2 + rnd.nextInt(2); // 2–3
		case HOTEL:
			return 5 + rnd.nextInt(2); // 5–6
		case RESTAURANT:
			return 6 + rnd.nextInt(3); // 6–8
		case INN:
		default:
			return 3 + rnd.nextInt(2); // 3–4
		}
	}

	private int tableCount(TavernaType type) {
		switch (type) {
		case BEER:
			return 4 + rnd.nextInt(5); // 4–8
		case CAFE:
			return 4 + rnd.nextInt(5); // 4–8
		case HOTEL:
			return 8 + rnd.nextInt(9); // 8–16
		case RESTAURANT:
			return 8 + rnd.nextInt(7); // 8–14
		case GAMBLING_DEN:
			return 6 + rnd.nextInt(9); // 6–14
		case INN:
		default:
			return 6 + rnd.nextInt(7); // 6–12
		}
	}

	private HabitatType resolveHabitat(String habitat) {
		if (habitat == null || habitat.isEmpty()) {
			HabitatType[] values = HabitatType.values();
			return values[rnd.nextInt(values.length)];
		}
		return HabitatType.valueOf(habitat);
	}

	// В отличие от resolveHabitat не подставляет случайную местность: null означает
	// «местность не задана» — расы посетителей тогда выбираются равномерно.
	private HabitatType resolveHabitatOrNull(String habitat) {
		if (habitat == null || habitat.isEmpty()) {
			return null;
		}
		try {
			return HabitatType.valueOf(habitat);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private TavernaType resolveType(String tavernaType) {
		if (tavernaType == null || tavernaType.isEmpty()) {
			TavernaType[] values = TavernaType.values();
			return values[rnd.nextInt(values.length)];
		}
		return TavernaType.valueOf(tavernaType);
	}

	// Цена позиции меню по уровню обслуживания (категории заведения). Диапазоны:
	//   Дешёвое  — блюдо 1–4 мм.,  напиток 1–2 мм.;
	//   Обычное  — блюдо 1–4 см.,  напиток 1–2 см.;
	//   Дорогое  — блюдо 1–3 зм.,  напиток 1–3 зм.;
	//   Элитное  — блюдо 5–10 зм., напиток 3–10 зм.
	private String menuPrice(TavernaCategory category, boolean dish) {
		TavernaCategory cat = category != null ? category : TavernaCategory.ORDINARY;
		String coin;
		int min;
		int max;
		switch (cat) {
		case CHEAP:
			coin = "мм.";
			min = 1;
			max = dish ? 4 : 2;
			break;
		case EXPENSIVE:
			coin = "зм.";
			min = 1;
			max = 3;
			break;
		case ELITE:
			coin = "зм.";
			min = dish ? 5 : 3;
			max = 10;
			break;
		case ORDINARY:
		default:
			coin = "см.";
			min = 1;
			max = dish ? 4 : 2;
			break;
		}
		int amount = min + rnd.nextInt(max - min + 1);
		return amount + " " + coin;
	}

	// Уровень обслуживания — значение TavernaCategory (уровень цен меню).
	// null или неизвестное значение — категорию не ограничиваем.
	private TavernaCategory resolveCategory(String serviceLevel) {
		if (serviceLevel == null || serviceLevel.isEmpty()) {
			return null;
		}
		try {
			return TavernaCategory.valueOf(serviceLevel);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	// Оставляет позиции нужной категории; если таких нет — возвращает исходный
	// список, чтобы меню не осталось пустым для редких сочетаний территории и уровня.
	private <T> List<T> filterByCategory(List<T> source, TavernaCategory category,
			Function<T, TavernaCategory> categoryGetter) {
		if (category == null) {
			return source;
		}
		List<T> filtered = source.stream()
				.filter(item -> category == categoryGetter.apply(item))
				.collect(Collectors.toList());
		return filtered.isEmpty() ? source : filtered;
	}

	// Склоняет прилагательное-префикс из именительного падежа в родительный,
	// согласуя с родом второго слова: «Красный» → «Красного», «Злой» → «Злого»,
	// «Поющий» → «Поющего», «Красная» → «Красной», «Синяя» → «Синей».
	private String genitivePrefix(String adjective, Sex sex) {
		if (sex == Sex.FEMALE) {
			if (adjective.endsWith("аяся")) {
				return adjective.substring(0, adjective.length() - 4) + "ейся";
			}
			if (adjective.endsWith("яя")) {
				return adjective.substring(0, adjective.length() - 2) + "ей";
			}
			if (adjective.endsWith("ая")) {
				char stemLast = adjective.charAt(adjective.length() - 3);
				String ending = stemLast == 'ж' || stemLast == 'ш' || stemLast == 'щ' || stemLast == 'ч'
						? "ей"
						: "ой";
				return adjective.substring(0, adjective.length() - 2) + ending;
			}
			return adjective;
		}
		if (adjective.endsWith("ийся")) {
			return adjective.substring(0, adjective.length() - 4) + "егося";
		}
		if (adjective.endsWith("ый") || adjective.endsWith("ой")) {
			return adjective.substring(0, adjective.length() - 2) + "ого";
		}
		if (adjective.endsWith("ое")) {
			return adjective.substring(0, adjective.length() - 2) + "ого";
		}
		if (adjective.endsWith("ее")) {
			return adjective.substring(0, adjective.length() - 2) + "его";
		}
		if (adjective.endsWith("ий")) {
			char stemLast = adjective.charAt(adjective.length() - 3);
			String ending = stemLast == 'к' || stemLast == 'г' || stemLast == 'х' ? "ого" : "его";
			return adjective.substring(0, adjective.length() - 2) + ending;
		}
		return adjective;
	}

	private <T> T pickOne(List<T> list) {
		return list.isEmpty() ? null : list.get(rnd.nextInt(list.size()));
	}

	private <T> List<T> pickRandom(List<T> source, int max) {
		if (source.isEmpty()) {
			return Collections.emptyList();
		}
		List<T> copy = new ArrayList<>(source);
		Collections.shuffle(copy, rnd);
		return copy.subList(0, Math.min(max, copy.size()));
	}

	// Раса из кэша: id для генератора имён и англ. слаг для взвешивания по местности.
	private static final class RacePick {
		private final Integer id;
		private final String englishName;

		private RacePick(Integer id, String englishName) {
			this.id = id;
			this.englishName = englishName;
		}
	}

	// Правила весов одной группы рас (ключ — подстрока слага) по местностям.
	// byHabitat — вес для конкретных местностей; fallback — вес строки habitat=NULL
	// (базовый вес расы в прочих местностях), может отсутствовать.
	private static final class RaceHabitatWeights {
		private final String key;
		private final Map<HabitatType, Integer> byHabitat = new EnumMap<>(HabitatType.class);
		private Integer fallback;

		private RaceHabitatWeights(String key) {
			this.key = key;
		}
	}
}
