package uz.ns.cardprocessing.service.imp;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.ns.cardprocessing.entity.Currency;
import uz.ns.cardprocessing.entity.Transaction;
import uz.ns.cardprocessing.repository.PerevodRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class HistoryService {
    private final PerevodRepository perevodRepository;

    public HistoryService(PerevodRepository perevodRepository) {
        this.perevodRepository = perevodRepository;
    }

    public Page<Transaction> getFilteredTransactions(String card_id, Map<String, String> params,
                                                     int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Transaction> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.containsKey("transaction_id")) {
                predicates.add(criteriaBuilder.equal(root.get("transaction_id"), params.get("transaction_id")));
            }
            if (params.containsKey("external_id")) {
                predicates.add(criteriaBuilder.equal(root.get("external_id"), params.get("external_id")));
            }
            if (params.containsKey("cart_id")) {
                if (card_id.equals(params.get("cart_id"))) {
                    return null;
                }
                predicates.add(criteriaBuilder.equal(root.get("cart_id"), params.get("cart_id")));
            }
            if (params.containsKey("description")) {
                predicates.add(criteriaBuilder.equal(root.get("description"), (params.get("description"))));
            }
            if (params.containsKey("amount")) {
                predicates.add(criteriaBuilder.equal(root.get("amount"), Long.parseLong(params.get("amount"))));
            }
            if (params.containsKey("currency")) {
                predicates.add(criteriaBuilder.equal(root.get("currency"), Currency.valueOf(params.get("currency"))));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return perevodRepository.findAll(spec, pageable);
    }
}
