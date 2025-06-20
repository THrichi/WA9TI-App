package com.application.wa9ti.repositories;

import com.application.wa9ti.dtos.FavoriteStoreDTO;
import com.application.wa9ti.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStoreIdAndDateBetween(Long storeId, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.store = :store")
    void deleteByStore(@Param("store") Store store);


    @Query("SELECT a FROM Appointment a WHERE a.employee.id = :employeeId AND a.date = :date")
    List<Appointment> findByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId AND " +
            "(a.date > CURRENT_DATE OR (a.date = CURRENT_DATE AND a.startTime > CURRENT_TIME)) " +
            "ORDER BY a.date ASC, a.startTime ASC")
    List<Appointment> findUpcomingAppointmentsByClient(@Param("clientId") Long clientId);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId AND " +
            "(a.date < CURRENT_DATE OR (a.date = CURRENT_DATE AND a.startTime < CURRENT_TIME)) " +
            "ORDER BY a.date DESC, a.startTime DESC")
    List<Appointment> findPastAppointmentsByClient(@Param("clientId") Long clientId);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId ORDER BY a.date DESC, a.startTime DESC")
    List<Appointment> findByClientId(@Param("clientId") Long clientId);

    boolean existsByClientIdAndStoreIdAndStatus(Long clientId, Long storeId, Appointment.Status status);

    List<Appointment> findByClientEmail(String email);

    boolean existsByServiceAndStatusIn(Service service, List<Appointment.Status> statuses);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.employee = :employee AND a.store.id = :storeId AND a.status IN :statuses")
    boolean existsByEmployeeAndStoreIdAndStatusIn(
            @Param("employee") Employee employee,
            @Param("storeId") Long storeId,
            @Param("statuses") List<Appointment.Status> statuses
    );



    @Query("""
        SELECT a FROM Appointment a
        LEFT JOIN a.client c
        LEFT JOIN c.user u
        WHERE a.store.id = :storeId
          AND (a.date > CURRENT_DATE OR (a.date = CURRENT_DATE AND a.startTime >= CURRENT_TIME))
          AND (
            (:keyword IS NULL OR :keyword = '') OR 
            (LOWER(a.clientName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
            (LOWER(a.clientEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
            (LOWER(a.clientPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
            (c IS NOT NULL AND (
                LOWER(c.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ))
          )
        ORDER BY CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END,
                 a.date ASC,
                 a.startTime ASC
    """)
    Page<Appointment> findAppointmentsSorted(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT a FROM Appointment a
        LEFT JOIN a.client c
        LEFT JOIN c.user u
        WHERE a.store.id = :storeId
          AND (a.date < CURRENT_DATE OR (a.date = CURRENT_DATE AND a.startTime < CURRENT_TIME))
          AND (
            (:keyword IS NULL OR :keyword = '') OR 
            (LOWER(a.clientName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
            (LOWER(a.clientEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
            (LOWER(a.clientPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
            (c IS NOT NULL AND (
                LOWER(c.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ))
          )
        ORDER BY a.date DESC, a.startTime DESC
    """)
    Page<Appointment> findArchivedAppointments(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );




    @Query("""
    SELECT a FROM Appointment a 
    WHERE a.date = :date 
    AND a.status = 'CONFIRMED' 
    OR  a.status = 'PENDING' 
    AND a.startTime BETWEEN :startTime AND :endTime
""")
    List<Appointment> findConfirmedAppointmentsInTimeRange(LocalDate date, LocalTime startTime, LocalTime endTime);


    @Query("""
            SELECT a FROM Appointment a
            LEFT JOIN a.client c
            LEFT JOIN c.user u
            WHERE a.store.id = :storeId
              AND a.date = CURRENT_DATE
              AND a.status <> 'COMPLETED'
              AND (
                (LOWER(a.clientEmail) = LOWER(:keyword)) OR  
                (LOWER(a.clientPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
                (LOWER(a.clientName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
                (c IS NOT NULL AND (
                    LOWER(c.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                    LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
                ))
              )
            ORDER BY a.startTime ASC
        """)
    List<Appointment> findTodayAppointmentsToHonor(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword
    );


    @Query("""
    SELECT new com.application.wa9ti.dtos.FavoriteStoreDTO(
        a.store.id, a.store.name, a.store.storeUrl, a.store.type, a.store.image, COUNT(a)
    )
    FROM Appointment a 
    JOIN a.store s
    JOIN s.owner o
    JOIN o.subscription sub
    WHERE a.client.user.email = :clientEmail
    AND sub.status NOT IN (com.application.wa9ti.models.Subscription.SubscriptionStatus.EXPIRED, 
                           com.application.wa9ti.models.Subscription.SubscriptionStatus.CANCELED)
    GROUP BY s.id, s.name, s.storeUrl, s.type, s.image
    ORDER BY COUNT(a) DESC
    LIMIT 3
    """)
    List<FavoriteStoreDTO> findTop3StoresByClient(@Param("clientEmail") String clientEmail);



    @Query("SELECT a.date, a.status, COUNT(a) FROM Appointment a " +
            "WHERE a.store.id = :storeId AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY a.date, a.status ORDER BY a.date ASC")
    List<Object[]> getAppointmentsCountByDateAndStatus(@Param("storeId") Long storeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a " +
            "WHERE a.store.id = :storeId AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY a.status ORDER BY COUNT(a) DESC")
    List<Object[]> getAppointmentsCountByStatus(@Param("storeId") Long storeId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);


    @Query("SELECT a.date, SUM(a.price) FROM Appointment a " +
            "WHERE a.store.id = :storeId AND a.status = 'COMPLETED' " +
            "AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY a.date ORDER BY a.date ASC")
    List<Object[]> getAppointmentsIncomeByDate(@Param("storeId") Long storeId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);


    @Query("SELECT a.date, COUNT(DISTINCT a.client.id) FROM Appointment a " +
            "WHERE a.store.id = :storeId " +
            "AND a.date = (SELECT MIN(a2.date) FROM Appointment a2 WHERE a2.client.id = a.client.id AND a2.store.id = :storeId) " +
            "AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY a.date ORDER BY a.date ASC")
    List<Object[]> getNewClientsByDate(@Param("storeId") Long storeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);



    @Query("SELECT a.service.name, COUNT(a) FROM Appointment a " +
            "WHERE a.store.id = :storeId AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY a.service.name ORDER BY COUNT(a) DESC")
    List<Object[]> getServiceBookings(@Param("storeId") Long storeId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);


    @Query("SELECT a.client.user.name, a.client.user.email, a.client.user.phone, a.client.image, COUNT(a) " +
            "FROM Appointment a " +
            "WHERE a.store.id = :storeId AND a.date BETWEEN :startDate AND :endDate " +
            "AND a.status = 'COMPLETED' " +
            "GROUP BY a.client.user.name, a.client.user.email, a.client.user.phone, a.client.image " +
            "ORDER BY COUNT(a) DESC LIMIT :limit")
    List<Object[]> getTopClients(@Param("storeId") Long storeId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("limit") int limit);


    @Query("SELECT AVG(clientAppointments.nbRdv) FROM " +
            "(SELECT COUNT(a) as nbRdv FROM Appointment a " +
            "WHERE a.store.id = :storeId " +
            "GROUP BY a.client.id) as clientAppointments")
    Double getAverageAppointmentsPerClient(@Param("storeId") Long storeId);


    @Query("SELECT a.employee.user.name, a.employee.image, COUNT(a) " +
            "FROM Appointment a " +
            "WHERE a.store.id = :storeId AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY a.employee.user.name, a.employee.image " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> getEmployeeAppointments(@Param("storeId") Long storeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);


    @Query("SELECT a.date, a.startTime, COUNT(a) " +
            "FROM Appointment a " +
            "WHERE a.store.id = :storeId AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY a.date, a.startTime " +
            "ORDER BY a.date ASC, a.startTime ASC")
    List<Object[]> getPopularTimes(@Param("storeId") Long storeId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);


    List<Appointment> findByStoreIdAndStatusIn(Long storeId, List<Appointment.Status> statuses);

    boolean existsByStoreAndDateAfter(Store store, LocalDate date);
}
