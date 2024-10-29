package uz.ns.cardprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.ns.cardprocessing.entity.Card;
import uz.ns.cardprocessing.entity.CardStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<List<Card>> findByUserIdAndCardStatus(Long user_id, CardStatus cardStatus);

    Optional<Card> findByIdempotencyKey(UUID idempotencyKey);

}
