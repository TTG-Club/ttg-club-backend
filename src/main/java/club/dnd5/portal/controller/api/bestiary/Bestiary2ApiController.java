package club.dnd5.portal.controller.api.bestiary;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.bestiary.BeastApi;
import club.dnd5.portal.dto.api.bestiary.BeastDetailApi;
import club.dnd5.portal.dto.api.bestiary.BeastFilter;
import club.dnd5.portal.dto.api.bestiary.BeastRequesApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.ActionType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.CreatureFeat;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Бестиарий v2", description = "API для сущест из бестиария")
@RequestMapping("/api/v2/")
@RestController
public class Bestiary2ApiController {
    private final BestiaryRepository beastRepository;
    private final ImageRepository imageRepository;
    private final TokenRepository tokenRepository;
    @Operation(summary = "Получение краткого списка сушеств")
    @GetMapping(value = "/bestiary")
    public List<BeastApi> getBestiary(@ParameterObject BeastRequesApi request) {
        Specification<Creature> specification = null;
        Optional<BeastRequesApi> optionalRequest = Optional.ofNullable(request);
        if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
            specification = SpecificationUtil.getSearch(request);
        }
        Optional<BeastFilter> filter = Optional.ofNullable(request.getFilter());
        if (optionalRequest.map(BeastRequesApi::getFilter).isPresent()
                && optionalRequest.map(BeastRequesApi::getFilter).map(BeastFilter::getNpc).orElseGet(Collections::emptyList).isEmpty()) {
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

    @Operation(summary = "Получение сушества по английскому имени")
    @GetMapping(value = "/bestiary/{englishName}")
    public BeastDetailApi getBeast(@PathVariable String englishName) {
        Creature beast = beastRepository.findByEnglishName(englishName.replace('_', ' '))
                .orElseThrow(PageNotFoundException::new);
        BeastDetailApi beastApi = new BeastDetailApi(beast);
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

    @Operation(summary = "Добавление существа в бестиарий")
    @SecurityRequirement(name = "Bearer Authentication")
    @Secured({"ADMIN"})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v2/bestiary")
    public void createBeast(@RequestBody BeastDetailRequest request) {
        Creature beast = new Creature();
        Optional<Creature> exist = beastRepository.findByEnglishName(request.getName().getEng());
        if (exist.isPresent()) {
            throw new EntityExistsException();
        }
        beast.setName(request.getName().getRus());
        beast.setEnglishName(request.getName().getEng());
        beast.setAltName(request.getName().getAlt());
        beastRepository.save(beast);
    }

    @Operation(summary = "Обнавление существа из бестиарии")
    @SecurityRequirement(name = "Bearer Authentication")
    @Secured({"ADMIN"})
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/api/v2/bestiary")
    public void updateBeast(@RequestBody BeastDetailRequest request) {
        Creature beast = beastRepository.findById(request.getId())
                .orElseThrow(PageNotFoundException::new);
        beastRepository.save(beast);
    }
}
