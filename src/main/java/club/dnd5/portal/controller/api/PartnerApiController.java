package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.PartnerApi;
import club.dnd5.portal.repository.PartnersRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Партнеры", description = "API по партнерам")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/partners")
public class PartnerApiController {
	private final PartnersRepository partnersRepository;

	@GetMapping
	@ResponseStatus(code = HttpStatus.OK)
	public List<PartnerApi> getPartners() {
		return partnersRepository
			.findAll()
			.stream()
			.map(PartnerApi::new)
			.collect(Collectors.toList());
	}
}
