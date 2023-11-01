package club.dnd5.portal.controller.api;

import club.dnd5.portal.model.SiteConfiguration;
import club.dnd5.portal.repository.SiteConfigurationRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@Tag(name = "Конфигурация", description = "API для получения конфигураций")
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class SiteConfigurationApiController {
	private final SiteConfigurationRepository siteConfigurationRepository;

	@GetMapping
	public ResponseEntity<List<SiteConfiguration>> getAllConfigurations() {
		List<SiteConfiguration> siteConfigurations = new ArrayList<>();
		siteConfigurationRepository.findAll().forEach(siteConfigurations::add);
		return new ResponseEntity<>(siteConfigurations, HttpStatus.OK);
	}

	@GetMapping(params = "key")
	public ResponseEntity<List<SiteConfiguration>> getConfigByKeys(@RequestParam List<String> keys) {
		List<SiteConfiguration> siteConfigurations = siteConfigurationRepository.findAllById(keys);
		return new ResponseEntity<>(siteConfigurations, HttpStatus.OK);
	}
}
