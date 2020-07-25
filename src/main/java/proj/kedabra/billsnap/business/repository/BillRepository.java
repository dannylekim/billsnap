package proj.kedabra.billsnap.business.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;

public interface BillRepository extends PagingAndSortingRepository<Bill, Long> {

    Bill getBillById(Long id);

    @Query(value = "SELECT b " +
            "FROM Bill as b, AccountBill as ba, Account as a " +
            "where b.created >= :startDate " +
            "and b.created < :endDate " +
            "and (b.category = :category or :category is null) " +
            "and b.status in (:statuses) " +
            "and ba.account.id = a.id " +
            "and ba.status = :invitationStatus " +
            "and a.email = :email " +
            "and b.id = ba.bill.id")
    Stream<Bill> findBillsPageable(@Param("startDate") ZonedDateTime startDate,
                                   @Param("endDate") ZonedDateTime endDate,
                                   @Nullable @Param("category") String category,
                                   @Param("statuses") List<BillStatusEnum> statuses,
                                   @Param("invitationStatus") InvitationStatusEnum invitationStatus,
                                   @Param("email") String email,
                                   Pageable pageable);
}

