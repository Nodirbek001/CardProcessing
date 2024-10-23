package uz.ns.cardprocessing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.ns.cardprocessing.entity.Transaction;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface PerevodRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionId(UUID transactionId);
    Optional<Transaction> findByIdempotencyKey(UUID idempotencyKey);

    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
}
