package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;
import proj.kedabra.billsnap.presentation.resources.AccountResource;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AccountMapper {

    Account toEntity(AccountDTO accountDTO);

    AccountDTO toDTO(Account account);

    AccountDTO toDTO(AccountCreationResource accountCreationResource);

    AccountResource toResource(AccountDTO accountDTO);

}
