package proj.kedabra.billsnap.business.service;

import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;

public interface ItemService {

    Item getItem(Long id);

    void editNewItems(Bill bill, Account account, EditBillDTO editBill);
}
