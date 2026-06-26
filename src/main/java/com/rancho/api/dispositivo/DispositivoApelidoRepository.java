package com.rancho.api.dispositivo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispositivoApelidoRepository extends JpaRepository<DispositivoApelido, String> {

    List<DispositivoApelido> findByDeviceIdIn(List<String> deviceIds);
}
