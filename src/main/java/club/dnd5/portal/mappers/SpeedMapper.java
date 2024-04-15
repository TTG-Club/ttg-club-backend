package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.creature.Creature;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface SpeedMapper {
	SpeedMapper INSTANCE = Mappers.getMapper(SpeedMapper.class);

	final int STANDARD_MOVEMENT = 30;

	@Named("mapSpeed")
	default void speed(Collection<NameValueApi> speeds, @MappingTarget Creature entity) {
		for (NameValueApi speed : speeds) {
			switch (speed.getName()) {
				case "скорость":
					byte speedMovement = convertObjectToByte(speed.getValue());
					entity.setSpeed(speedMovement);
					break;
				case "полет":
					short flyValue = convertObjectToShort(speed.getValue());
					entity.setFlySpeed(flyValue);
					break;
				case "плаванье":
					short swimmingSpeed = convertObjectToShort(speed.getValue());
					entity.setSwimmingSpped(swimmingSpeed);
					break;
				case "лазанье":
					short climbingSpeed = convertObjectToShort(speed.getValue());
					entity.setClimbingSpeed(climbingSpeed);
					break;
				case "копание":
					short diggingSpeed = convertObjectToShort(speed.getValue());
					entity.setDiggingSpeed(diggingSpeed);
					break;
			}
		}
	}

	// Helper methods for converting values to short and byte
	@Named("objectToShort")
	static short convertObjectToShort(Object value) {
		if (value instanceof Number) {
			return ((Number) value).shortValue();
		} else if (value instanceof String) {
			try {
				return Short.parseShort((String) value);
			} catch (NumberFormatException e) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Error parsing value to short");
			}
		} else {
			return STANDARD_MOVEMENT;
		}
	}

	@Named("objectToByte")
	static byte convertObjectToByte(Object value) {
		if (value instanceof Number) {
			return ((Number) value).byteValue();
		} else if (value instanceof String) {
			try {
				return Byte.parseByte((String) value);
			} catch (NumberFormatException e) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Error parsing value to byte");
			}
		} else {
			return STANDARD_MOVEMENT;
		}
	}
}
