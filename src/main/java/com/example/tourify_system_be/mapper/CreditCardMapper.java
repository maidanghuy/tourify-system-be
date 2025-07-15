package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.request.CreditCardRequest;
import com.example.tourify_system_be.dto.response.CreditCardResponse;
import com.example.tourify_system_be.entity.CreditCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditCardMapper {

    CreditCardResponse toCreditCardResponse(CreditCard creditCard);
    CreditCard toCreditCard(CreditCardResponse creditCardResponse);

    @Mapping(target = "cardID", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    CreditCard toCreditCard(CreditCardRequest creditCardRequest);
}
