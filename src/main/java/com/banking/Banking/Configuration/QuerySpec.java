package com.banking.Banking.Configuration;

import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class QuerySpec {
    public static Specification<Transaction> hasType(OperationTypes type) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), type);
    }
    public static Specification<Transaction> belongsInCards(List<Long> cardIds) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.in(root.join("senderCard", JoinType.LEFT).get("id")).value(cardIds),
                        criteriaBuilder.in(root.join("receiverCard", JoinType.LEFT).get("id")).value(cardIds)
        );
    }
    public static Specification<Transaction> belongsToCard(Long cardId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.in(root.join("senderCard", JoinType.LEFT).get("id")).value(cardId),
                        criteriaBuilder.in(root.join("receiverCard", JoinType.LEFT).get("id")).value(cardId)
                );
    }
    public static Specification<Transaction> timestampBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("timestamp"), start, end);
    }
}
