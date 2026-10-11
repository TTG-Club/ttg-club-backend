package club.dnd5.portal.dto.user;

import lombok.Data;

@Data
public class SignUpDto {
    private String username;
    private String email;
    private String password;
    /** Пользователь принял условия публичной оферты. */
    private Boolean offerAccepted;
    /** Редакция оферты, которую видел пользователь. */
    private String offerVersion;
    /** Пользователь дал согласие на обработку персональных данных. */
    private Boolean personalDataConsent;
    /** Редакция политики конфиденциальности, которую видел пользователь. */
    private String privacyPolicyVersion;
}
