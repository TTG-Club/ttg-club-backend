package club.dnd5.portal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FoundryVersion {
	V10(10),
	V11(11);

	@Getter
	private final int value;
}

