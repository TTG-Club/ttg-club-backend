package club.dnd5.portal.model;

public enum FoundryVersion {
	V10,
	V11;

	public int getValue() {
		switch (this) {
			case V10:
				return 10;
			case V11:
				return 11;
			default:
				throw new IllegalArgumentException("Unexpected enum constant: " + this);
		}
	}
}
