package com.TripSync.debt.repository;

import com.TripSync.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DebtCalculationRepository extends JpaRepository<Trip, Long> {

    interface UserAmountProjection {
        Long getUserId();
        String getFullName();
        BigDecimal getTotalAmount();
    }

    // 1. Saare trip members fetch karne ke liye
    @Query("""
        SELECT tm.user.id AS userId, tm.user.fullName AS fullName, CAST(0 AS bigdecimal) AS totalAmount
        FROM TripMember tm
        WHERE tm.trip.id = :tripId
    """)
    List<UserAmountProjection> findTripMembers(@Param("tripId") Long tripId);

    // 2. Har member ne total kitna expense pay kiya (+)
    @Query("""
        SELECT e.paidBy.id AS userId, e.paidBy.fullName AS fullName, COALESCE(SUM(e.amount), 0) AS totalAmount
        FROM Expense e
        WHERE e.trip.id = :tripId
        GROUP BY e.paidBy.id, e.paidBy.fullName
    """)
    List<UserAmountProjection> sumPaidByUser(@Param("tripId") Long tripId);

    // 3. Har member ka total owed share kitna hai (-)
    @Query("""
        SELECT es.user.id AS userId, es.user.fullName AS fullName, COALESCE(SUM(es.shareAmount), 0) AS totalAmount
        FROM ExpenseSplit es
        WHERE es.expense.trip.id = :tripId
        GROUP BY es.user.id, es.user.fullName
    """)
    List<UserAmountProjection> sumOwedByUser(@Param("tripId") Long tripId);

    // 4. Settlements mein kitna pay kiya (+)
    @Query("""
        SELECT s.payer.id AS userId, s.payer.fullName AS fullName, COALESCE(SUM(s.amount), 0) AS totalAmount
        FROM Settlement s
        WHERE s.trip.id = :tripId
        GROUP BY s.payer.id, s.payer.fullName
    """)
    List<UserAmountProjection> sumSettlementsPaid(@Param("tripId") Long tripId);

    // 5. Settlements mein kitna receive kiya (-)
    @Query("""
        SELECT s.payee.id AS userId, s.payee.fullName AS fullName, COALESCE(SUM(s.amount), 0) AS totalAmount
        FROM Settlement s
        WHERE s.trip.id = :tripId
        GROUP BY s.payee.id, s.payee.fullName
    """)
    List<UserAmountProjection> sumSettlementsReceived(@Param("tripId") Long tripId);
}