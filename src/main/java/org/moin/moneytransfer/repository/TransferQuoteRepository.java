package org.moin.moneytransfer.repository;

import org.moin.moneytransfer.entity.TransferQuote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferQuoteRepository extends JpaRepository<TransferQuote, String> {
    // 추가적인 메서드가 필요한 경우 정의
}
