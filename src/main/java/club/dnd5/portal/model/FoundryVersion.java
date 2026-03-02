package club.dnd5.portal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoundryVersion {
	V10(10),
	V11(11);

	private final int value;
}

