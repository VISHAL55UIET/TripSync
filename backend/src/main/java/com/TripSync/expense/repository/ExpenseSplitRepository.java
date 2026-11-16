package com.TripSync.expense.repository;

import com.TripSync.expense.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit,Long> {

    List<ExpenseSplit> findByExpenseId(Long expenseId);
    List<ExpenseSplit> findByUserId(Long userId);
    List<ExpenseSplit> findByExpenseTripIdAndUserId(Long tripId,Long userId);
}