package com.TripSync.debt.service;

import com.TripSync.debt.dto.SimplifiedDebtResponse;
import com.TripSync.debt.repository.DebtCalculationRepository;
import com.TripSync.debt.repository.DebtCalculationRepository.UserAmountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DebtSimplificationService {

    private final DebtCalculationRepository debtCalculationRepository;

    private static class Node {
        Long userId;
        String userName;
        BigDecimal balance;

        Node(Long userId, String userName, BigDecimal balance) {
            this.userId = userId;
            this.userName = userName;
            this.balance = balance;
        }
    }

    @Transactional(readOnly = true)
    public List<SimplifiedDebtResponse> simplifyDebts(Long tripId) {
        Map<Long, BigDecimal> netBalanceMap = new HashMap<>();
        Map<Long, String> userNames = new HashMap<>();

        // Initialize all trip members
        for (UserAmountProjection m : debtCalculationRepository.findTripMembers(tripId)) {
            netBalanceMap.put(m.getUserId(), BigDecimal.ZERO);
            userNames.put(m.getUserId(), m.getFullName());
        }

        // + Expenses paid
        for (UserAmountProjection p : debtCalculationRepository.sumPaidByUser(tripId)) {
            netBalanceMap.merge(p.getUserId(), p.getTotalAmount(), BigDecimal::add);
        }

        // - Expense shares owed
        for (UserAmountProjection o : debtCalculationRepository.sumOwedByUser(tripId)) {
            netBalanceMap.merge(o.getUserId(), o.getTotalAmount().negate(), BigDecimal::add);
        }

        // + Settlements paid
        for (UserAmountProjection sp : debtCalculationRepository.sumSettlementsPaid(tripId)) {
            netBalanceMap.merge(sp.getUserId(), sp.getTotalAmount(), BigDecimal::add);
        }

        // - Settlements received
        for (UserAmountProjection sr : debtCalculationRepository.sumSettlementsReceived(tripId)) {
            netBalanceMap.merge(sr.getUserId(), sr.getTotalAmount().negate(), BigDecimal::add);
        }

        // Max-Heaps for Greedy Min-Cash-Flow
        PriorityQueue<Node> creditors = new PriorityQueue<>((a, b) -> b.balance.compareTo(a.balance));
        PriorityQueue<Node> debtors = new PriorityQueue<>((a, b) -> b.balance.compareTo(a.balance));

        BigDecimal threshold = new BigDecimal("0.01");
        for (Map.Entry<Long, BigDecimal> entry : netBalanceMap.entrySet()) {
            BigDecimal bal = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            if (bal.compareTo(threshold) >= 0) {
                creditors.add(new Node(entry.getKey(), userNames.get(entry.getKey()), bal));
            } else if (bal.compareTo(threshold.negate()) <= 0) {
                debtors.add(new Node(entry.getKey(), userNames.get(entry.getKey()), bal.abs()));
            }
        }

        List<SimplifiedDebtResponse> transactions = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Node creditor = creditors.poll();
            Node debtor = debtors.poll();

            BigDecimal settleAmount = creditor.balance.min(debtor.balance).setScale(2, RoundingMode.HALF_UP);

            transactions.add(new SimplifiedDebtResponse(
                    debtor.userId,
                    debtor.userName,
                    creditor.userId,
                    creditor.userName,
                    settleAmount
            ));

            BigDecimal remCred = creditor.balance.subtract(settleAmount);
            BigDecimal remDebt = debtor.balance.subtract(settleAmount);

            if (remCred.compareTo(threshold) >= 0) {
                creditors.add(new Node(creditor.userId, creditor.userName, remCred));
            }
            if (remDebt.compareTo(threshold) >= 0) {
                debtors.add(new Node(debtor.userId, debtor.userName, remDebt));
            }
        }

        return transactions;
    }
}