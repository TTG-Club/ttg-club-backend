package club.dnd5.portal.model.god;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Rank {
	ABSOLUTE("абсолютное божество") {
		@Override
		public String getName(GodSex sex) {
			return "абсолютное";
		}
	},
	GREAT("великое божество") {
		@Override
		public String getName(GodSex sex) {
			switch (sex) {
			case MALE:
				return "великий";
			case FEMALE:
				return "великая";
			default:
				return "великое";
			}
		}
	},
	MIDDLE ("среднее божество"){
		@Override
		public String getName(GodSex sex) {
			switch (sex) {
			case MALE:
				return "средний";
			case FEMALE:
				return "средняя";
			default:
				return "среднее";
			}
		}
	},
	LESS ("младшее божество"){
		@Override
		public String getName(GodSex sex) {
			switch (sex) {
			case MALE:
				return "младший";
			case FEMALE:
				return "младшая";
			default:
				return "меньшее";
			}
		}
	},
	HALF("полу-бог") {
		@Override
		public String getName(GodSex sex) {
			return "полу - ";
		}
	},
	QUASI ("квази-бог"){
		@Override
		public String getName(GodSex sex) {
			return "квази - ";
		}
	},
	DEAD("мертвое божество") {
		@Override
		public String getName(GodSex sex) {
			switch (sex) {
			case MALE:
				return "мертвый";
			case FEMALE:
				return "мертвая";
			default:
				return "мертвое";
			}
		}
	};

	private final String name;

	public abstract String getName(GodSex sex);

	public static Rank parse(String value) {
		for (Rank rank: values()) {
			if (rank.getName().equals(value)) {
				return rank;
			}
		}
		throw new IllegalArgumentException();
	}
}
