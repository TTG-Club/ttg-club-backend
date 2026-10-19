package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.vttg.VttgChangesResponse;
import club.dnd5.portal.dto.api.vttg.VttgChangesStatus;
import club.dnd5.portal.service.vttg.VttgExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Компендиум редакции 2014 для VTTG. Ручки повторяют контракт нового сайта, поэтому
 * VTTG качает их тем же кодом, что и компендиум 2024, только с другого хоста.
 */
@RequiredArgsConstructor
@Tag(name = "VTTG", description = "Выгрузка компендиума редакции 2014 для VTTG")
@RestController
@RequestMapping(value = "/api/v2/vttg", produces = MediaType.APPLICATION_JSON_VALUE)
public class VttgApiController {
	private final VttgExportService exportService;

	@Operation(summary = "Есть ли изменения компендиума для VTTG",
		description = "С начала времён — число всех записей, с курсора — ноль. Изменение контента видно "
			+ "по schemaVersion: при его смене VTTG перекачивает компендиум целиком.")
	@GetMapping("/changes/status")
	public VttgChangesStatus getChangesStatus(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
		return exportService.status(since);
	}

	@Operation(summary = "Записи компендиума для VTTG",
		description = "С начала времён — все расы, предыстории, черты и заклинания в формате VTTG, "
			+ "с курсора — пустой список.")
	@GetMapping("/changes")
	public VttgChangesResponse getChanges(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
		return exportService.changes(since);
	}
}
