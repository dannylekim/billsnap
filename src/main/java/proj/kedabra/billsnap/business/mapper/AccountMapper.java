package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BaseAccountDTO;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;
import proj.kedabra.billsnap.presentation.resources.AccountResource;
import proj.kedabra.billsnap.presentation.resources.BaseAccountResource;
import proj.kedabra.billsnap.utils.annotations.ObfuscateArgs;


@Mapper
public interface AccountMapper {

    Account toEntity(AccountDTO accountDTO);

    AccountDTO toDTO(Account account);

    @ObfuscateArgs
    AccountDTO toDTO(AccountCreationResource accountCreationResource);

    AccountResource toResource(AccountDTO accountDTO);

    BaseAccountDTO toDTO(BaseAccountResource baseAccountResource);

    @Mapping(source = "editInfo.firstName", target = "firstName")
    @Mapping(source = "editInfo.middleName", target = "middleName")
    @Mapping(source = "editInfo.lastName", target = "lastName")
    @Mapping(source = "editInfo.gender", target = "gender")
    @Mapping(source = "editInfo.phoneNumber", target = "phoneNumber")
    @Mapping(source = "editInfo.birthDate", target = "birthDate")
    @Mapping(source = "editInfo.location", target = "location")
    void updateAccount(@MappingTarget Account account, BaseAccountDTO editInfo);

}
