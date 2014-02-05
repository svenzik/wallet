package com.playtech.wallet.repository;

import com.playtech.wallet.domain.TimedMethodStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

/**
 * Used to persist method execution statistics
 */
public interface MethodStatisticsRepository extends JpaRepository<TimedMethodStatistics, BigInteger>{
}