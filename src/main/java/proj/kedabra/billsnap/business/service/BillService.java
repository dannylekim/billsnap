package proj.kedabra.billsnap.business.service;

import java.util.List;
import java.util.stream.Stream;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.GetBillPaginationDTO;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

public interface BillService {

    Bill createBillToAccount(BillDTO billDTO, Account account, List<Account> accountList);

    Bill associateItemsToAccountBill(AssociateBillDTO associateBillDTO);

    Stream<Bill> getAllBillsByAccountPageable(GetBillPaginationDTO billPaginationDTO);

    Stream<PaymentOwed> getAllAmountOwedByStatusAndAccount(BillStatusEnum status, Account account);

    Bill getBill(Long id);

    Bill inviteRegisteredToBill(Bill bill, List<Account> accounts);

    void verifyBillStatus(Bill bill, BillStatusEnum status);

    void verifyUserIsBillResponsible(Bill bill, String userEmail);

    Bill startBill(Long id);

    Bill editBill(Long id, Account account, EditBillDTO editBill);

}
