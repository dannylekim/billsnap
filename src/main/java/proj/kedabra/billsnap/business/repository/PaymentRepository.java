package proj.kedabra.billsnap.business.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.model.entities.Account;
import proj.kedabra.billsnap.model.entities.Bill;
import proj.kedabra.billsnap.model.projections.PaymentOwed;

import java.util.stream.Stream;

public interface PaymentRepository extends CrudRepository<Bill, Long> {

    @Query( value = "SELECT account.email, SUM(item.cost) as amount " +
            "FROM bill as b, item, bills_vs_accounts as bva, items_vs_accounts as iva, account " +
            "WHERE b.id = bva.bill_id " +
            "AND b.id = item.bill_id " +
            "AND iva.item_id = item.id " +
            "AND account.id = iva.account_id " +
            "AND bva.account_id = :#{#account.getId()} " +
            "AND b.status = :#{#status.toString()} " +
            "GROUP BY account.email",
            nativeQuery = true)
    Stream<PaymentOwed> getAllAmountOwedByStatusAndAccount(@Param("status") BillStatusEnum status, @Param("account") Account account);

}