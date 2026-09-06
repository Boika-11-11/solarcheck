package com.boika.solarcheck.repository;

import com.boika.solarcheck.model.Installer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallerRepository extends JpaRepository<Installer, Long> {

    List<Installer> findAllByOrderByNameAsc();

    List<Installer> findByCityIgnoreCaseOrderByNameAsc(String city);

    List<Installer> findByVerifiedTrueOrderByNameAsc();

    boolean existsByNameIgnoreCaseAndCityIgnoreCase(String name, String city);
}