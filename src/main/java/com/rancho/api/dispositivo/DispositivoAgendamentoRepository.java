package com.rancho.api.dispositivo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispositivoAgendamentoRepository extends JpaRepository<DispositivoAgendamento, String> {

    List<DispositivoAgendamento> findByDeviceIdIn(List<String> deviceIds);

    List<DispositivoAgendamento> findByAtivoTrue();
}
