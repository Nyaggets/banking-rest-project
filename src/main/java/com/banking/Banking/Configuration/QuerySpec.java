package com.banking.Banking.Configuration;

import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class QuerySpec {
    public static Specification<Transaction> hasType(List<OperationTypes> types) {
        return (root, query, criteriaBuilder) -> {
            if (types.contains(OperationTypes.TRANSFER_IN) && types.contains(OperationTypes.TRANSFER_OUT))
                return root.get("type").in(types);
            return criteriaBuilder.and(
                    root.get("type").in(types),
                    criteriaBuilder.isFalse(root.get("isInternal"))
            );
        };
    }
    public static Specification<Transaction> belongsInCards(List<Long> cardIds) {
        return (root, query, criteriaBuilder) ->
                root.get("clientCard").get("id").in(cardIds);
    }
    public static Specification<Transaction> belongsToCard(Long cardId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("clientCard").get("id"), cardId);
    }
    public static Specification<Transaction> timestampBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("timestamp"), start, end);
    }
}
