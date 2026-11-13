package club.dnd5.portal.service;

import club.dnd5.portal.model.classes.OptionTypeDefinition;
import club.dnd5.portal.repository.classes.OptionTypeDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Превращает код типа особенностей из запроса в запись справочника. */
@Service
@RequiredArgsConstructor
public class OptionTypeResolver {
	private final OptionTypeDefinitionRepository repository;

	/** {@code null} для пустого кода; 400, если такого типа нет в справочнике. */
	public OptionTypeDefinition resolve(String code) {
		if (!StringUtils.hasText(code)) {
			return null;
		}
		return repository.findById(code.trim())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown option type: " + code));
	}

	public List<OptionTypeDefinition> resolveAll(Collection<String> codes) {
		List<OptionTypeDefinition> result = new ArrayList<>();
		if (codes == null) {
			return result;
		}
		for (String code : codes) {
			OptionTypeDefinition type = resolve(code);
			if (type != null && !result.contains(type)) {
				result.add(type);
			}
		}
		return result;
	}
}
