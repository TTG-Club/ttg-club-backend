package club.dnd5.portal.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Отображаемое имя пользователя — тело PATCH и ответ GET. Ограничения совпадают с новым
 * сайтом (core-app): 2–24 символа, буквы (в т.ч. кириллица), цифры, пробелы, дефис,
 * подчёркивание. В ответе на GET имя может быть {@code null} — тогда показывается логин.
 */
@Getter
@Setter
@NoArgsConstructor
public class DisplayNameDto {
    @NotBlank(message = "Отображаемое имя обязательно")
    @Size(min = 2, max = 24, message = "От 2 до 24 символов")
    @Pattern(
            regexp = "^[\\w\\p{L}\\s-]+$",
            message = "Только буквы, цифры, пробелы, дефисы и подчёркивания")
    private String displayName;

    public DisplayNameDto(String displayName) {
        this.displayName = displayName;
    }
}
