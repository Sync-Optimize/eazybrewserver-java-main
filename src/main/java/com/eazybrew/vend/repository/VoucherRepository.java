package com.eazybrew.vend.repository;

import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.model.Voucher;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    /**
     * Find an active voucher for a staff member
     * 
     * @param staff the staff member
     * @param recordStatus the record status (usually ACTIVE)
     * @return an optional containing the voucher if found
     */
    Optional<Voucher> findByStaffAndRecordStatus(Staff staff, RecordStatusConstant recordStatus);

    /**
     * Find all active vouchers for a company
     * 
     * @param company the company
     * @param recordStatus the record status (usually ACTIVE)
     * @return a list of vouchers
     */
    List<Voucher> findByCompanyAndRecordStatus(Company company, RecordStatusConstant recordStatus);

    /**
     * Find all active vouchers for a company, paginated
     * 
     * @param company the company
     * @param recordStatus the record status (usually ACTIVE)
     * @param pageable pagination information
     * @return a page of vouchers
     */
    Page<Voucher> findByCompanyAndRecordStatus(Company company, RecordStatusConstant recordStatus, Pageable pageable);

    /**
     * Find all active vouchers for a staff member
     * 
     * @param staff the staff member
     * @param recordStatus the record status (usually ACTIVE)
     * @param pageable pagination information
     * @return a page of vouchers
     */
    Page<Voucher> findByStaffAndRecordStatus(Staff staff, RecordStatusConstant recordStatus, Pageable pageable);

    /**
     * Find an active voucher for a staff member in a company
     * 
     * @param staff the staff member
     * @param company the company
     * @param recordStatus the record status (usually ACTIVE)
     * @return an optional containing the voucher if found
     */
    Optional<Voucher> findByStaffAndCompanyAndRecordStatus(Staff staff, Company company, RecordStatusConstant recordStatus);

    /**
     * Find all vouchers for a company with a specific record status where the given date falls between startDate and endDate
     * 
     * @param company the company
     * @param recordStatus the record status (usually ACTIVE)
     * @param date the date to check against the voucher's validity period
     * @return a list of vouchers that are valid for the given date
     */
    @Query("SELECT v FROM Voucher v WHERE v.company = :company AND v.recordStatus = :recordStatus " +
           "AND :date >= v.startDate AND :date <= v.endDate")
    List<Voucher> findByCompanyAndRecordStatusAndDateBetweenStartAndEnd(
            @Param("company") Company company, 
            @Param("recordStatus") RecordStatusConstant recordStatus, 
            @Param("date") LocalDate date);

    /**
     * Find all active vouchers for staff members in a list
     * 
     * @param staffList the list of staff members
     * @param recordStatus the record status (usually ACTIVE)
     * @return a list of vouchers
     */
    List<Voucher> findByStaffInAndRecordStatus(List<Staff> staffList, RecordStatusConstant recordStatus);

    /**
     * Find all active vouchers for staff members in a list for a specific company
     * 
     * @param staffList the list of staff members
     * @param company the company
     * @param recordStatus the record status (usually ACTIVE)
     * @return a list of vouchers
     */
    List<Voucher> findByStaffInAndCompanyAndRecordStatus(List<Staff> staffList, Company company, RecordStatusConstant recordStatus);
}
