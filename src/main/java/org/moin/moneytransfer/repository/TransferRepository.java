package org.moin.moneytransfer.repository;

import org.moin.moneytransfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, String> {
    List<Transfer> findAllByUserId(String userId);
    List<Transfer> findAllByUserIdAndTransferredAtBetween(String userId, ZonedDateTime start, ZonedDateTime end);

}
