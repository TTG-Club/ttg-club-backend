package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.BeastApi;
import club.dnd5.portal.dto.api.bestiary.BeastDetailApi;
import club.dnd5.portal.dto.api.bestiary.BeastFilter;
import club.dnd5.portal.dto.api.bestiary.BeastRequesApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.bestiary.request.DescriptionRequest;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.ActionType;
import club.dnd5.portal.model.creature.Condition;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.CreatureFeat;
import club.dnd5.portal.model.creature.CreatureRace;
import club.dnd5.portal.model.creature.SavingThrow;
import club.dnd5.portal.model.creature.Skill;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.LanguageRepository;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.TagBestiaryDatatableRepository;
import club.dnd5.portal.util.ChallengeRating;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class BestiaryServiceImpl implements BestiaryService {
    private final BestiaryRepository beastRepository;
    private final ImageRepository imageRepository;
    private final TokenRepository tokenRepository;
    private final BookResolver bookResolver;
    private final LanguageRepository languageRepository;
    private final TagBestiaryDatatableRepository tagRepository;

    @Override
    public List<BeastApi> findAll(BeastRequesApi request) {
        Specification<Creature> specification = null;
        Optional<BeastRequesApi> optionalRequest = Optional.ofNullable(request);
        if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
            specification = SpecificationUtil.getSearch(request);
        }
        Optional<BeastFilter> filter = Optional.ofNullable(request.getFilter());
        if (optionalRequest.map(BeastRequesApi::getFilter).isPresent()
                && optionalRequest.map(BeastRequesApi::getFilter)
                .map(BeastFilter::getNpc)
                .orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification,
                    (root, query, cb) -> cb.notEqual(root.get("raceId"), 102));
        }
        if (!filter.map(BeastFilter::getChallengeRatings).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification,
                    (root, query, cb) -> root.get("challengeRating")
                            .in(request.getFilter().getChallengeRatings()));
        }
        if (!filter.map(BeastFilter::getTypes).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(
                    specification, (root, query, cb) -> root.get("type").in(request.getFilter().getTypes()));
        }
        if (!filter.map(BeastFilter::getSizes).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(
                    specification, (root, query, cb) -> root.get("size").in(request.getFilter().getSizes()));
        }
        if (!filter.map(BeastFilter::getTags).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
                Join<Object, Object> join = root.join("races", JoinType.INNER);
                query.distinct(true);
                return cb.and(join.get("id").in(request.getFilter().getTags()));
            });
        }
        if (!filter.map(BeastFilter::getBooks).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
                Join<Book, Spell> join = root.join("book", JoinType.INNER);
                return join.get("source").in(request.getFilter().getBooks());
            });
        }
        if (!filter.map(BeastFilter::getMoving).orElseGet(Collections::emptyList).isEmpty()) {
            if (request.getFilter().getMoving().contains("fly")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("flySpeed")));
            }
            if (request.getFilter().getMoving().contains("hover")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("hover")));
            }
            if (request.getFilter().getMoving().contains("climbs")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("climbingSpeed")));
            }
            if (request.getFilter().getMoving().contains("swim")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("swimmingSpped")));
            }
            if (request.getFilter().getMoving().contains("digger")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("diggingSpeed")));
            }
        }
        if (!filter.map(BeastFilter::getEnvironments).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
                Join<Object, Object> join = root.join("habitates", JoinType.INNER);
                query.distinct(true);
                return join.in(request.getFilter().getEnvironments());
            });
        }
        if (!filter.map(BeastFilter::getVulnerabilityDamage).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
                Join<Object, Object> join = root.join("vulnerabilityDamages", JoinType.INNER);
                query.distinct(true);
                return join.in(request.getFilter().getVulnerabilityDamage());
            });
        }
        if (!filter.map(BeastFilter::getResistanceDamage).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
                Join<Object, Object> join = root.join("resistanceDamages", JoinType.INNER);
                query.distinct(true);
                return join.in(request.getFilter().getResistanceDamage());
            });
        }
        if (!filter.map(BeastFilter::getImmunityDamage).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
                Join<Object, Object> join = root.join("immunityDamages", JoinType.INNER);
                query.distinct(true);
                return join.in(request.getFilter().getImmunityDamage());
            });
        }
        if (!filter.map(BeastFilter::getImmunityCondition).orElseGet(Collections::emptyList).isEmpty()) {
            specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
                Join<Object, Object> join = root.join("immunityStates", JoinType.INNER);
                query.distinct(true);
                return join.in(request.getFilter().getImmunityCondition());
            });
        }
        if (!filter.map(BeastFilter::getSenses).orElseGet(Collections::emptyList).isEmpty()) {
            if (request.getFilter().getSenses().contains("darkvision")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("darkvision")));
            }
            if (request.getFilter().getSenses().contains("trysight")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("trysight")));
            }
            if (request.getFilter().getSenses().contains("blindsight")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("blindsight")));
            }
            if (request.getFilter().getSenses().contains("tremmor")) {
                specification = SpecificationUtil.getAndSpecification(specification,
                        (root, query, cb) -> cb.isNotNull(root.get("vibration")));
            }
        }
        if (!filter.map(BeastFilter::getFeatures).orElseGet(Collections::emptyList).isEmpty()) {
            Specification<Creature> addSpec = null;
            if (request.getFilter().getFeatures().contains("lair")) {
                addSpec = SpecificationUtil.getOrSpecification(null, (root, query, cb) ->
                        cb.isNotNull(root.get("lair"))
                );
                request.getFilter().getFeatures().remove("lair");
            }
            if (request.getFilter().getFeatures().contains("legendary")) {
                addSpec = SpecificationUtil.getOrSpecification(addSpec, (root, query, cb) -> {
                    Join<Action, Creature> join = root.join("actions", JoinType.INNER);
                    query.distinct(true);
                    return cb.equal(join.get("actionType"), ActionType.LEGENDARY);
                });
                request.getFilter().getFeatures().remove("legendary");
            }
            for (String featureName : request.getFilter().getFeatures()) {
                addSpec = SpecificationUtil.getOrSpecification(addSpec, (root, query, cb) -> {
                    Join<CreatureFeat, Creature> join = root.join("feats", JoinType.INNER);
                    query.distinct(true);
                    return cb.like(join.get("name"), "%" + featureName + "%");
                });
            }
            specification = SpecificationUtil.getAndSpecification(specification, addSpec);
        }
        Pageable pageable = PageAndSortUtil.getPageable(request);
        return beastRepository.findAll(specification, pageable).toList()
                .stream()
                .map(BeastApi::new)
                .collect(Collectors.toList());
    }

    @Override
    public BeastDetailApi findOne(String englishName) {
        Creature beast = beastRepository.findByEnglishName(englishName.replace('_', ' '))
                .orElseThrow(PageNotFoundException::new);
        BeastDetailApi beastApi = new BeastDetailApi(beast, beastRepository::countByActionId);
        Collection<String> images = new ArrayList<>();
        tokenRepository.findByRefIdAndType(beast.getId(), "круглый")
                .stream()
                .findFirst()
                .ifPresent(token -> images.add(token.getUrl()));
        images.addAll(imageRepository.findAllByTypeAndRefId(ImageType.CREATURE, beast.getId()));
        if (!images.isEmpty()) {
            beastApi.setImages(images);
        }
        return beastApi;
    }

    @Transactional
    @Override
    public BeastDetailApi create(BeastDetailRequest request) {
        Creature beast = new Creature();
        Optional<Creature> exist = beastRepository.findByEnglishName(request.getName().getEng());
        if (exist.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creature with the same englishName already exists");
        }
        mapping(beast, request);
        if (beast.getBook() == null) {
            beast.setBook(bookResolver.getCustomBook());
        }
        return new BeastDetailApi(beastRepository.saveAndFlush(beast));
    }

    @Transactional
    @Override
    public BeastDetailApi update(BeastDetailRequest request) {
        Creature beast = beastRepository.findById(request.getId())
                .orElseThrow(PageNotFoundException::new);
        beastRepository.findByEnglishName(request.getName().getEng())
                .filter(existing -> !existing.getId().equals(request.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creature with the same englishName already exists");
                });
        mapping(beast, request);
        return new BeastDetailApi(beastRepository.saveAndFlush(beast));
    }

    private void mapping(Creature beast, BeastDetailRequest request) {
        beast.setName(request.getName().getRus().trim());
        beast.setEnglishName(request.getName().getEng().trim());
        beast.setAltName(trimToNull(request.getName().getAlt()));
        beast.setSize(request.getSize());
        beast.setType(request.getType());
        beast.setAlignment(request.getAlignment());
        beast.setAC(request.getArmorClass());
        beast.setBonusAC(trimToNull(request.getArmorText()));

        beast.setCountDiceHp(request.getHits().getDiceCount());
        beast.setAverageHp((short) 0);
        beast.setDiceHp(null);
        beast.setBonusHP(null);
        beast.setSuffixHP(trimToNull(request.getHits().getText()));
        beast.setChallengeRating(trimToNull(request.getChallengeRating()));
        beast.setExp(ChallengeRating.getExp(beast.getChallengeRating()));
        beast.setRaceId(request.isNpc() ? 102 : 0);
        beast.setDescription(trimToNull(request.getDescription()));
        beast.setReaction(trimToNull(request.getReaction()));
        beast.setLegendary(request.getLegendary() == null ? null : trimToNull(request.getLegendary().getDescription()));

        if (request.getAbility() != null) {
            beast.setStrength((byte) request.getAbility().getStr());
            beast.setDexterity((byte) request.getAbility().getDex());
            beast.setConstitution((byte) request.getAbility().getCon());
            beast.setIntellect((byte) request.getAbility().getIntellect());
            beast.setWizdom((byte) request.getAbility().getWiz());
            beast.setCharisma((byte) request.getAbility().getCha());
        }

        mapSpeed(beast, request);
        mapSenses(beast, request);
        beast.setArmorTypes(new ArrayList<>(nullToEmpty(request.getArmors()).stream()
                .map(ArmorType::valueOf)
                .collect(Collectors.toList())));
        beast.setResistanceDamages(new ArrayList<>(nullToEmpty(request.getDamageResistances())));
        beast.setImmunityDamages(new ArrayList<>(nullToEmpty(request.getDamageImmunities())));
        beast.setVulnerabilityDamages(new ArrayList<>(nullToEmpty(request.getDamageVulnerabilities())));
        beast.setImmunityStates(nullToEmpty(request.getConditionImmunities()).stream()
                .map(Condition::valueOf)
                .collect(Collectors.toList()));
        beast.setHabitates(new ArrayList<>(nullToEmpty(request.getEnvironment())));
        beast.setSavingThrows(mapSavingThrows(request.getSavingThrows()));
        beast.setSkills(mapSkills(request.getSkills()));
        beast.setFeats(mapFeats(request.getFeats()));
        beast.setActions(mapActions(request));
        beast.setLanguages(mapLanguages(request.getLanguages()));
        beast.setRaces(mapTags(request.getTags()));
        bookResolver.find(request.getSource()).ifPresent(beast::setBook);
    }

    private void mapSpeed(Creature beast, BeastDetailRequest request) {
        beast.setSpeed(toByteSpeed(getSpeedValue(request, null)));
        beast.setFlySpeed(getShortSpeedValue(request, "летая"));
        beast.setHover(hasAdditional(request, "летая", "парит") ? beast.getFlySpeed() : null);
        beast.setSwimmingSpped(getShortSpeedValue(request, "плавая"));
        beast.setClimbingSpeed(getShortSpeedValue(request, "лазая"));
        beast.setDiggingSpeed(getShortSpeedValue(request, "копая"));
        beast.setSpeedText(nullToEmpty(request.getSpeed()).stream()
                .filter(speed -> !(speed.getValue() instanceof Number))
                .map(NameValueApi::getName)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null));
    }

    private BigDecimal getSpeedValue(BeastDetailRequest request, String name) {
        return nullToEmpty(request.getSpeed()).stream()
                .filter(speed -> Objects.equals(speed.getName(), name))
                .map(NameValueApi::getValue)
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(this::toBigDecimal)
                .findFirst()
                .orElse(BigDecimal.valueOf(30));
    }

    private Short getShortSpeedValue(BeastDetailRequest request, String name) {
        BigDecimal value = getSpeedValue(request, name);
        return value.compareTo(BigDecimal.valueOf(30)) == 0 ? null : toShortSpeed(value);
    }

    private BigDecimal toBigDecimal(Number value) {
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Speed must be a finite number", exception);
        }
    }

    private byte toByteSpeed(BigDecimal value) {
        try {
            return value.byteValueExact();
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Speed must be an integer in byte range", exception);
        }
    }

    private short toShortSpeed(BigDecimal value) {
        try {
            return value.shortValueExact();
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Speed must be an integer in short range", exception);
        }
    }

    private boolean hasAdditional(BeastDetailRequest request, String name, String additional) {
        return nullToEmpty(request.getSpeed()).stream()
                .filter(speed -> Objects.equals(speed.getName(), name))
                .map(NameValueApi::getAdditional)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .anyMatch(additional::equalsIgnoreCase);
    }

    private void mapSenses(Creature beast, BeastDetailRequest request) {
        beast.setPassivePerception(parsePassivePerception(request));
        beast.setPassivePerceptionBonus(null);
        beast.setDarkvision(getSenseValue(request, "тёмное зрение"));
        beast.setTrysight(getSenseValue(request, "истинное зрение"));
        beast.setBlindsight(getSenseValue(request, "слепое зрение"));
        beast.setBlindsightRadius(hasSenseAdditional(request, "слепое зрение") ? 1 : null);
        beast.setVibration(getSenseValue(request, "чувство вибрации"));
    }

    private byte parsePassivePerception(BeastDetailRequest request) {
        if (request.getSenses() == null || !StringUtils.hasText(request.getSenses().getPassivePerception())) {
            return 10;
        }
        String passivePerception = request.getSenses().getPassivePerception();
        int valueEnd = 0;
        while (valueEnd < passivePerception.length()) {
            char current = passivePerception.charAt(valueEnd);
            if (current != '-' && !Character.isDigit(current)) {
                break;
            }
            valueEnd++;
        }
        String value = passivePerception.substring(0, valueEnd);
        return StringUtils.hasText(value) ? Byte.parseByte(value) : 10;
    }

    private Integer getSenseValue(BeastDetailRequest request, String name) {
        if (request.getSenses() == null) {
            return null;
        }
        return nullToEmpty(request.getSenses().getSenses()).stream()
                .filter(sense -> name.equalsIgnoreCase(sense.getName()))
                .map(NameValueApi::getValue)
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::intValue)
                .findFirst()
                .orElse(null);
    }

    private boolean hasSenseAdditional(BeastDetailRequest request, String name) {
        if (request.getSenses() == null) {
            return false;
        }
        return nullToEmpty(request.getSenses().getSenses()).stream()
                .filter(sense -> name.equalsIgnoreCase(sense.getName()))
                .map(NameValueApi::getAdditional)
                .anyMatch(Objects::nonNull);
    }

    private List<SavingThrow> mapSavingThrows(Collection<NameValueApi> request) {
        return nullToEmpty(request).stream()
                .map(value -> {
                    SavingThrow savingThrow = new SavingThrow();
                    savingThrow.setAbility(AbilityType.valueOf(value.getKey().toString()));
                    savingThrow.setBonus(toByte(value.getValue()));
                    savingThrow.setAdditionalBonus(value.getAdditional() == null ? null : value.getAdditional().toString());
                    return savingThrow;
                })
                .collect(Collectors.toList());
    }

    private List<Skill> mapSkills(Collection<NameValueApi> request) {
        return nullToEmpty(request).stream()
                .map(value -> {
                    Skill skill = new Skill();
                    skill.setType(SkillType.valueOf(value.getKey().toString()));
                    skill.setBonus(toByte(value.getValue()));
                    skill.setAdditionalBonus(value.getAdditional() == null ? null : value.getAdditional().toString());
                    return skill;
                })
                .collect(Collectors.toList());
    }

    private List<CreatureFeat> mapFeats(Collection<DescriptionRequest> request) {
        return nullToEmpty(request).stream()
                .filter(item -> item.getName() != null && StringUtils.hasText(item.getName().getRus()))
                .map(item -> {
                    CreatureFeat feat = new CreatureFeat();
                    feat.setName(item.getName().getRus().trim());
                    feat.setEnglishName(trimToNull(item.getName().getEng()));
                    feat.setDescription(trimToNull(item.getDescription()));
                    feat.setMarkdown(item.getMarkdown() == null || item.getMarkdown());
                    return feat;
                })
                .collect(Collectors.toList());
    }

    private List<Action> mapActions(BeastDetailRequest request) {
        List<Action> actions = new ArrayList<>();
        actions.addAll(mapActions(request.getActions(), ActionType.ACTION));
        actions.addAll(mapActions(request.getReactions(), ActionType.REACTION));
        actions.addAll(mapActions(request.getBonusActions(), ActionType.BONUS));
        actions.addAll(mapActions(request.getMysticalActions(), ActionType.MYSTICAL));
        if (request.getLegendary() != null) {
            actions.addAll(mapNameValueActions(request.getLegendary().getList(), ActionType.LEGENDARY));
        }
        return actions;
    }

    private List<Action> mapActions(Collection<DescriptionRequest> request, ActionType type) {
        return nullToEmpty(request).stream()
                .filter(item -> item.getName() != null && StringUtils.hasText(item.getName().getRus()))
                .map(item -> createAction(item.getName().getRus(), item.getName().getEng(), item.getDescription(), item.getMarkdown(), type))
                .collect(Collectors.toList());
    }

    private List<Action> mapNameValueActions(Collection<NameValueApi> request, ActionType type) {
        return nullToEmpty(request).stream()
                .filter(item -> StringUtils.hasText(item.getName()))
                .map(item -> createAction(item.getName(), null, item.getValue() == null ? null : item.getValue().toString(), true, type))
                .collect(Collectors.toList());
    }

    private Action createAction(String name, String englishName, String description, Boolean markdown, ActionType type) {
        Action action = new Action();
        action.setName(name.trim());
        action.setEnglishName(trimToNull(englishName));
        action.setDescription(StringUtils.hasText(description) ? description.trim() : "");
        action.setActionType(type);
        action.setMarkdown(markdown == null || markdown);
        return action;
    }

    private List<Language> mapLanguages(Collection<String> languages) {
        if (languages == null || languages.isEmpty()) {
            return new ArrayList<>();
        }
        return languageRepository.findByNameIn(languages);
    }

    private List<CreatureRace> mapTags(Collection<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(tagRepository.findByNameIn(tags));
    }

    private byte toByte(Object value) {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        return Byte.parseByte(value.toString());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> Collection<T> nullToEmpty(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }
}
