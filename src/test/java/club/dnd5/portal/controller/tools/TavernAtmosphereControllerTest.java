package club.dnd5.portal.controller.tools;

import club.dnd5.portal.model.tavern.Atmosphere;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.repository.tavern.AtmosphereRepository;
import club.dnd5.portal.repository.tavern.OwnerSecretRepository;
import club.dnd5.portal.repository.tavern.OwnerTraitRepository;
import club.dnd5.portal.repository.tavern.OwnerWeaknessRepository;
import club.dnd5.portal.repository.tavern.RaceHabitatChanceRepository;
import club.dnd5.portal.repository.tavern.RandomEventRepository;
import club.dnd5.portal.repository.tavern.TavernaDishRepository;
import club.dnd5.portal.repository.tavern.TavernaDrinkRepository;
import club.dnd5.portal.repository.tavern.TavernaNameRepository;
import club.dnd5.portal.repository.tavern.TavernaPrefixNameRepository;
import club.dnd5.portal.repository.tavern.TopicDiscussedRepository;
import club.dnd5.portal.repository.tavern.VisitorRepository;
import club.dnd5.portal.service.NameGeneratorService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.UrlHandlerFilter;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TavernAtmosphereControllerTest {
	@ParameterizedTest
	@ValueSource(strings = {"/tools/tavern/atmosphere", "/tools/tavern/atmosphere/"})
	void shouldReturnAtmosphereWithAndWithoutTrailingSlash(String path) throws Exception {
		AtmosphereRepository atmosphereRepository = mock(AtmosphereRepository.class);
		Atmosphere atmosphere = new Atmosphere();
		atmosphere.setName("Оживлённо");
		atmosphere.setDescription("Зал полон посетителей.");
		atmosphere.setVisitors(7);
		when(atmosphereRepository.findAll()).thenReturn(Collections.singletonList(atmosphere));

		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(createController(atmosphereRepository))
			.addFilters(UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build())
			.build();

		mockMvc.perform(get(path))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("data-visitors=\"7\"")))
			.andExpect(content().string(containsString("Оживлённо")));
	}

	private TavernToolController createController(AtmosphereRepository atmosphereRepository) {
		return new TavernToolController(
			mock(TavernaNameRepository.class),
			mock(TavernaPrefixNameRepository.class),
			atmosphereRepository,
			mock(TavernaDishRepository.class),
			mock(TavernaDrinkRepository.class),
			mock(VisitorRepository.class),
			mock(TopicDiscussedRepository.class),
			mock(RandomEventRepository.class),
			mock(OwnerTraitRepository.class),
			mock(OwnerWeaknessRepository.class),
			mock(OwnerSecretRepository.class),
			mock(RaceRepository.class),
			mock(RaceHabitatChanceRepository.class),
			mock(NameGeneratorService.class)
		);
	}
}
