package club.dnd5.portal.dto.fvtt.export;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.creature.Skill;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FSkills {
	private FSkill acr;
	private FSkill arc;
	private FSkill ath;
	private FSkill dec;
	private FSkill his;
	private FSkill ins;
	private FSkill itm;
	private FSkill inv;
	private FSkill med;
	private FSkill nat;
	private FSkill prc;
	private FSkill prf;
	private FSkill per;
	private FSkill rel;
	private FSkill slt;
	private FSkill ste;
	private FSkill sur;

	public FSkills(List<Skill> skills) {
		Map<SkillType, Byte> skillMap = skills.parallelStream()
				.filter(s -> s.getType() != null)
				.collect(Collectors.toMap(Skill::getType, skill -> skill.getBonus()));
		acr = new FSkill();
		if (skillMap.containsKey(SkillType.ACROBATICS)) {
			acr.setValue((byte) 1);
		}
		acr.setAbility("dex");

		arc = new FSkill();
		if (skillMap.containsKey(SkillType.ARCANA)) {
			arc.setValue((byte) 1);
		}
		arc.setAbility("int");

		ath = new FSkill();
		if (skillMap.containsKey(SkillType.ATHLETICS)) {
			ath.setValue((byte) 1);
		}
		ath.setAbility("str");

		dec = new FSkill();
		if (skillMap.containsKey(SkillType.DECEPTION)) {
			dec.setValue((byte) 1);
		}
		dec.setAbility("cha");

		his = new FSkill();
		if (skillMap.containsKey(SkillType.HISTORY)) {
			his.setValue((byte) 1);
		}
		his.setAbility("int");

		ins = new FSkill();
		if (skillMap.containsKey(SkillType.INSIGHT)) {
			ins.setValue((byte) 1);
		}
		ins.setAbility("wis");

		itm = new FSkill();
		if (skillMap.containsKey(SkillType.INTIMIDATION)) {
			itm.setValue((byte) 1);
		}
		itm.setAbility("cha");

		inv = new FSkill();
		if (skillMap.containsKey(SkillType.INVESTIGATION)) {
			inv.setValue((byte) 1);
		}
		inv.setAbility("int");

		med = new FSkill();
		if (skillMap.containsKey(SkillType.MEDICINE)) {
			med.setValue((byte) 1);
		}
		med.setAbility("wis");

		nat = new FSkill();
		if (skillMap.containsKey(SkillType.NATURE)) {
			nat.setValue((byte) 1);
		}
		nat.setAbility("int");

		prc = new FSkill();
		if (skillMap.containsKey(SkillType.PERCEPTION)) {
			prc.setValue((byte) 1);
		}
		prc.setAbility("wis");

		prf = new FSkill();
		if (skillMap.containsKey(SkillType.PERFORMANCE)) {
			prf.setValue((byte) 1);
		}
		prf.setAbility("cha");

		per = new FSkill();
		if (skillMap.containsKey(SkillType.PERSUASION)) {
			per.setValue((byte) 1);
		}
		per.setAbility("cha");

		rel = new FSkill();
		if (skillMap.containsKey(SkillType.RELIGION)) {
			rel.setValue((byte) 1);
		}
		rel.setAbility("int");

		slt = new FSkill();
		if (skillMap.containsKey(SkillType.SLEIGHT_OF_HAND)) {
			slt.setValue((byte) 1);
		}
		slt.setAbility("dex");

		ste = new FSkill();
		if (skillMap.containsKey(SkillType.STEALTH)) {
			ste.setValue((byte) 1);
		}
		ste.setAbility("dex");

		sur = new FSkill();
		if (skillMap.containsKey(SkillType.SURVIVAL)) {
			sur.setValue((byte) 1);
		}
		sur.setAbility("wis");
	}
}
