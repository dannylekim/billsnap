package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.repository.ItemRepository;
import proj.kedabra.billsnap.business.service.ItemService;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    @Autowired
    public ItemServiceImpl(final ItemRepository itemRepository, final ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Item getItem(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorMessageEnum.ITEM_ID_DOES_NOT_EXIST.getMessage(id.toString())));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editNewItems(Bill bill, Account account, EditBillDTO editBill) {
        final Set<Item> items = new HashSet<>();

        editBill.getItems().forEach(it -> {
            if (it.getId() == null) {
                final var item = itemMapper.toEntity(it);
                item.setBill(bill);

                var accountItem = new AccountItem();
                accountItem.setAccount(account);
                accountItem.setPercentage(new BigDecimal(100));
                accountItem.setItem(item);

                item.getAccounts().add(accountItem);
                items.add(item);
            } else {
                final var existingItem = getItem(it.getId());

                if (!existingItem.getBill().getId().equals(bill.getId())) {
                    throw new IllegalArgumentException("Item does not belong to the bill");
                }

                itemMapper.updateItem(it, existingItem);
                items.add(existingItem);
            }
        });

        bill.getItems().clear();
        bill.getItems().addAll(items);
    }
}
