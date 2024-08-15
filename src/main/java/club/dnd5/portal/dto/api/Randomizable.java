package club.dnd5.portal.dto.api;


import java.util.List;

public interface Randomizable {
	boolean getRandom();
	List<String> getBooks();
	List<String> getCategories();
}
