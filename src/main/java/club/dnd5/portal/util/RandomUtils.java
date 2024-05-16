package club.dnd5.portal.util;

import club.dnd5.portal.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.*;

public class RandomUtils {
	private static final Random random = new Random();

	private RandomUtils() {}

	public static <T> List<T> getRandomObjectListFromList(List<T> entityList, int sizeList) {
		if (entityList.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "The list is empty");
		}
		// Чтобы избегать дубликатов в коллекции
		Set<Integer> selectedIndices = new HashSet<>();
		List<T> resultList = new ArrayList<>();
		while (resultList.size() < sizeList) {
			int randomIndex = random.nextInt(entityList.size());
			// Проверка находится ли уже под таким индексом объект (то есть не рандомили мы объект, который уже есть)
			if (!selectedIndices.contains(randomIndex)) {
				resultList.add(entityList.get(randomIndex));
				selectedIndices.add(randomIndex);
			}
		}
		return resultList;
	}
}
