package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.bestiary.BeastApi;
import club.dnd5.portal.dto.api.bestiary.BeastDetailApi;
import club.dnd5.portal.dto.api.bestiary.BeastRequesApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;

import java.util.List;

public interface BestiaryService {
    List<BeastApi> findAll(BeastRequesApi request);

    BeastDetailApi findOne(String englishName);

    BeastDetailApi create(BeastDetailRequest request);

    BeastDetailApi update(BeastDetailRequest request);
}
