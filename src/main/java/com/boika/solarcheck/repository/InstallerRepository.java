package com.boika.solarcheck.repository;

import com.boika.solarcheck.model.Installer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstallerRepository extends JpaRepository<Installer, Long> {

    List<Installer> findAllByOrderByNameAsc();

    List<Installer> findByCityIgnoreCaseOrderByNameAsc(String city);

    List<Installer> findByVerifiedTrueOrderByNameAsc();

    boolean existsByNameIgnoreCaseAndCityIgnoreCase(String name, String city);

    @Query("""
           SELECT i FROM Installer i
           WHERE (:province IS NULL OR LOWER(i.province) = LOWER(CAST(:province AS string)))
             AND (:search IS NULL
                  OR LOWER(i.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                  OR LOWER(i.city) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
           ORDER BY i.name ASC
           """)
    List<Installer> search(@Param("search") String search, @Param("province") String province);

    @Query("SELECT DISTINCT i.province FROM Installer i WHERE i.province IS NOT NULL ORDER BY i.province")
    List<String> findAllProvinces();
}