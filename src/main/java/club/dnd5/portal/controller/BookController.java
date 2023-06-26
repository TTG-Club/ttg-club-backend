package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.repository.datatable.BookRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Hidden
@Controller
public class BookController {
	@Autowired
	private BookRepository bookRepository;

	@GetMapping("/books")
	public String getRules(Model model) {
		model.addAttribute("metaTitle", "Источники (Books) D&D 5e");
		model.addAttribute("metaUrl", "https://ttg.club/books");
		model.addAttribute("menuTitle", "Источники");
		return "spa";
	}

	@GetMapping("/books/{name}")
	public String getRule(Model model, @PathVariable String name) {
		Book book = bookRepository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s) | Источники (Books) D&D 5e", book.getName(), book.getEnglishName()));
		model.addAttribute("metaUrl", String.format("https://ttg.club/books/%s", book.getUrlName()));
		model.addAttribute("selectedBook", "name");
		model.addAttribute("menuTitle", "Источники");
		return "spa";
	}
}
