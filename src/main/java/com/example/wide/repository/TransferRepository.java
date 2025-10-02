package com.example.wide.repository;

import com.example.wide.entities.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query(value = """
        SELECT t.* FROM transfer t WHERE from_account = :accountNumber
        """,
            countQuery = """
        SELECT count(1) FROM transfer WHERE from_account = :accountNumber
        """, nativeQuery = true)
    Page<Transfer> findTransfersByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

}
