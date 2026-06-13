package com.banking.Banking.Configuration;

import com.banking.Banking.Entity.OperationTypeEnum;
import com.banking.Banking.Entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class QuerySpec {
    public static Specification<Transaction> removeTransferDuplicates() {
        return (root, query, criteriaBuilder) -> {
            Predicate isInternalTransferIn = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("operationType"), OperationTypeEnum.TRANSFER_IN),
                    criteriaBuilder.equal(root.get("isInternal"), true)
            );
            return criteriaBuilder.not(isInternalTransferIn);
        };
    }
    public static Specification<Transaction> hasType(List<OperationTypeEnum> types) {
        return (root, query, criteriaBuilder) -> {
            Predicate typeIn = root.get("operationType").in(types);
            if (types.contains(OperationTypeEnum.TRANSFER_IN) && types.contains(OperationTypeEnum.TRANSFER_OUT))
                return typeIn;

            return criteriaBuilder.and(
                    typeIn,
                    criteriaBuilder.equal(root.get("isInternal"), false)
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
