package club.dnd5.portal.controller.api.user;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.user.ExcludedSourcesDto;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.datatable.BookRepository;
import club.dnd5.portal.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Источники по умолчанию: книги, которые пользователь снял в профиле. Фронт снимает их
 * в фильтре источников каждого раздела, пока посетитель не выберет источники сам.
 * Список снятых книг приходит и в {@code /api/v1/user/info}, чтобы SSR знал его сразу.
 */
@Tag(name = "Профиль пользователя", description = "Источники по умолчанию")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user/sources")
public class UserSourcesController {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Operation(summary = "Все книги группами по типу; снятые по умолчанию отмечены default=false")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<FilterApi> getSources() {
        return currentUser()
                .map(user -> ResponseEntity.ok(buildFilter(user.getExcludedSources())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Сохранение книг, снятых в фильтре источников по умолчанию")
    @PutMapping
    @Transactional
    public ResponseEntity<FilterApi> updateSources(@Valid @RequestBody ExcludedSourcesDto request) {
        Optional<User> current = currentUser();
        if (current.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Неизвестные ключи отбрасываются: в таблице остаются только существующие книги
        Set<String> known = bookRepository.findAll().stream()
                .map(Book::getSource)
                .collect(Collectors.toSet());

        User user = current.get();
        user.getExcludedSources().clear();
        request.getExcluded().stream()
                .filter(known::contains)
                .forEach(user.getExcludedSources()::add);
        userRepository.save(user);

        return ResponseEntity.ok(buildFilter(user.getExcludedSources()));
    }

    /** Книги группами по типу, как источники в фильтрах разделов. */
    private FilterApi buildFilter(Set<String> excluded) {
        List<FilterApi> sources = new ArrayList<>();
        for (TypeBook typeBook : TypeBook.values()) {
            List<Book> books = bookRepository.findAllByTypeOrderByYearAscSourceAsc(typeBook);
            if (books.isEmpty()) {
                continue;
            }

            FilterApi group = new FilterApi(typeBook.getName(), typeBook.name());
            group.setValues(books.stream()
                    .map(book -> new FilterValueApi(book.getSource(), book.getSource(),
                            !excluded.contains(book.getSource()), book.getName()))
                    .collect(Collectors.toList()));
            sources.add(group);
        }

        FilterApi filter = new FilterApi();
        filter.setSources(sources);
        return filter;
    }

    /** Текущий пользователь из локальной таблицы по имени принципала (логин/email). */
    private Optional<User> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return userRepository.findByEmailOrUsername(authentication.getName(), authentication.getName());
    }
}
