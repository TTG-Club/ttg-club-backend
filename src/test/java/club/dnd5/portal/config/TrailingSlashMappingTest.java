package club.dnd5.portal.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TrailingSlashConfig} отрезает слеш в конце запроса до маршрутизации, поэтому ручка
 * со слешем в конце пути недостижима и всегда отвечает 404.
 */
class TrailingSlashMappingTest {
	private static final AntPathMatcher PATHS = new AntPathMatcher();

	@Test
	void mappingsShouldNotEndWithSlash() throws ClassNotFoundException {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
		List<String> broken = new ArrayList<>();

		for (BeanDefinition definition : scanner.findCandidateComponents("club.dnd5.portal")) {
			Class<?> type = Class.forName(definition.getBeanClassName());
			String[] prefixes = paths(AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class));

			for (Method method : type.getDeclaredMethods()) {
				RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
				if (mapping == null) {
					continue;
				}
				for (String prefix : prefixes) {
					for (String path : paths(mapping)) {
						String full = PATHS.combine(prefix, path);
						if (full.length() > 1 && full.endsWith("/")) {
							broken.add(type.getSimpleName() + "#" + method.getName() + ": " + full);
						}
					}
				}
			}
		}

		assertTrue(broken.isEmpty(), "Пути со слешем в конце недостижимы: " + broken);
	}

	private static String[] paths(RequestMapping mapping) {
		if (mapping == null || mapping.path().length == 0) {
			return new String[] {""};
		}
		return mapping.path();
	}
}
