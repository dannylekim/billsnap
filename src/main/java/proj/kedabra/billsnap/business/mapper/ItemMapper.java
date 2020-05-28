package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.model.entities.Item;

@Mapper(uses = AccountMapper.class)
public interface ItemMapper {

    @Mapping(source = "id", target = "itemId")
    ItemPercentageSplitDTO toItemPercentageSplitDTO(Item item);

    Item toEntity(ItemDTO itemDTO);
}
