package club.dnd5.portal.controller.api;

import club.dnd5.portal.model.SiteConfiguration;
import club.dnd5.portal.repository.SiteConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class SiteConfigurationApiController {
	private final SiteConfigurationRepository siteConfigurationRepository;

	@GetMapping
	public ResponseEntity<Map<String, String>> getAllConfig() {
		Iterable<SiteConfiguration> siteConfigurations = siteConfigurationRepository.findAll();
		Map<String, String> configMap = new HashMap<>();
		siteConfigurations.forEach(config -> configMap.put(config.getKey(), config.getValue()));
		return new ResponseEntity<>(configMap, HttpStatus.OK);
	}

	@GetMapping(params = "key")
	public ResponseEntity<Map<String, String>> getConfigByKeys(@RequestParam List<String> key) {
		Map<String, String> configMap = new HashMap<>();
		for (String k : key) {
			SiteConfiguration siteConfiguration = siteConfigurationRepository.findById(k).orElse(null);
			if (siteConfiguration != null) {
				configMap.put(siteConfiguration.getKey(), siteConfiguration.getValue());
			}
		}
		return new ResponseEntity<>(configMap, HttpStatus.OK);
	}
}
