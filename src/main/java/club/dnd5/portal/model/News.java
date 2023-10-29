package club.dnd5.portal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@RequiredArgsConstructor
@Getter
@Setter
@Table(name = "news")
public class News {
	@Id
	private Long id;
	private String title;
	private String text;
	private boolean enabled;
	private Date date;
	private String image;
}
