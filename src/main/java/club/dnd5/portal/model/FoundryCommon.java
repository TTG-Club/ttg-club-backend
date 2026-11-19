package club.dnd5.portal.model;

import lombok.Getter;


public interface FoundryCommon {
	/** Описание HTML-ом (биография в экспорте Foundry). */
	String getDescriptionHtml();

	String getName();

	String getEnglishName();
}
