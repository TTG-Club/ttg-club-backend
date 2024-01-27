package club.dnd5.portal.dto.api;

import club.dnd5.portal.dto.api.classes.NameApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryApi {
    public Collection<NameApi> entities;
}
