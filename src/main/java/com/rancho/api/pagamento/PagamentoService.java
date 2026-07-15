package com.rancho.api.pagamento;

import com.rancho.api.animal.Animal;
import com.rancho.api.animal.AnimalRepository;
import com.rancho.api.animalservico.AnimalServico;
import com.rancho.api.animalservico.AnimalServicoRepository;
import com.rancho.api.animalservico.AnimalServicoStatus;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.hospedagem.Hospedagem;
import com.rancho.api.hospedagem.HospedagemRepository;
import com.rancho.api.hospedagem.HospedagemStatus;
import com.rancho.api.pagamento.dto.CobrancaAvulsaDTO;
import com.rancho.api.pagamento.dto.PagamentoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final AnimalServicoRepository animalServicoRepository;
    private final AnimalRepository animalRepository;
    private final HospedagemRepository hospedagemRepository;

    /**
     * Gera uma cobranca pendente para o servico contratado no proximo vencimento.
     * Usado na criacao do AnimalServico e na confirmacao de pagamentos recorrentes.
     */
    @Transactional
    public Pagamento gerarCobranca(AnimalServico as) {
        Pagamento pagamento = Pagamento.builder()
                .animalServico(as)
                .animal(as.getAnimal())
                .descricao(as.getServico().getNome())
                .valor(as.getValor())
                .vencimento(as.getProximoVencimento())
                .status(PagamentoStatus.PENDENTE)
                .build();
        return pagamentoRepository.save(pagamento);
    }

    /**
     * Gera a cobranca mensal de uma hospedagem, com vencimento no proximo
     * vencimento previsto. Usada no registro de entrada e na recorrencia mensal.
     */
    @Transactional
    public Pagamento gerarCobrancaHospedagem(Hospedagem h) {
        Pagamento pagamento = Pagamento.builder()
                .hospedagem(h)
                .animal(h.getAnimal())
                .descricao("Hospedagem - Baia " + h.getBaia().getIdentificacao())
                .valor(h.getValorMensal())
                .vencimento(h.getProximoVencimento())
                .status(PagamentoStatus.PENDENTE)
                .build();
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public PagamentoResponseDTO cobrancaAvulsa(CobrancaAvulsaDTO dto) {
        Animal animal = null;
        if (dto.animalId() != null) {
            animal = animalRepository.findById(dto.animalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Animal", dto.animalId()));
        }
        Pagamento pagamento = Pagamento.builder()
                .animal(animal)
                .descricao(dto.descricao())
                .valor(dto.valor())
                .vencimento(dto.vencimento())
                .status(PagamentoStatus.PENDENTE)
                .build();
        return toDTO(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponseDTO registrarPagamento(Long id, LocalDate dataPagamento, FormaPagamento forma) {
        Pagamento pagamento = getPagamento(id);
        if (pagamento.getStatus() == PagamentoStatus.PAGO) {
            throw new BusinessException("Pagamento ja realizado");
        }
        if (pagamento.getStatus() == PagamentoStatus.CANCELADO) {
            throw new BusinessException("Pagamento cancelado nao pode receber baixa");
        }

        pagamento.setStatus(PagamentoStatus.PAGO);
        pagamento.setDataPagamento(dataPagamento != null ? dataPagamento : LocalDate.now());
        pagamento.setFormaPagamento(forma);
        Pagamento salvo = pagamentoRepository.save(pagamento);

        // RN007 - recalcular proximo vencimento e gerar proxima cobranca recorrente
        AnimalServico as = pagamento.getAnimalServico();
        if (as != null && as.getStatus() == AnimalServicoStatus.ATIVO) {
            LocalDate base = pagamento.getVencimento();
            as.setProximoVencimento(base.plusDays(as.getRecorrenciaDias()));
            animalServicoRepository.save(as);
            gerarCobranca(as);
        }

        // Recorrencia mensal da hospedagem enquanto o animal continuar hospedado
        Hospedagem hosp = pagamento.getHospedagem();
        if (hosp != null && hosp.getStatus() == HospedagemStatus.ATIVO && hosp.getValorMensal() != null) {
            LocalDate base = pagamento.getVencimento();
            hosp.setProximoVencimento(base.plusMonths(1));
            hospedagemRepository.save(hosp);
            gerarCobrancaHospedagem(hosp);
        }

        return toDTO(salvo);
    }

    @Transactional
    public PagamentoResponseDTO estornar(Long id) {
        Pagamento pagamento = getPagamento(id);
        if (pagamento.getStatus() != PagamentoStatus.PAGO) {
            throw new BusinessException("Apenas pagamentos pagos podem ser estornados");
        }
        pagamento.setStatus(PagamentoStatus.PENDENTE);
        pagamento.setDataPagamento(null);
        pagamento.setFormaPagamento(null);
        return toDTO(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponseDTO cancelar(Long id) {
        Pagamento pagamento = getPagamento(id);
        pagamento.setStatus(PagamentoStatus.CANCELADO);
        return toDTO(pagamentoRepository.save(pagamento));
    }

    @Transactional(readOnly = true)
    public Page<PagamentoResponseDTO> findAll(PagamentoStatus status, Long animalId, Long clienteId,
                                              LocalDate inicio, LocalDate fim, Pageable pageable) {
        return pagamentoRepository.search(status, animalId, clienteId, inicio, fim, pageable).map(this::toDTO);
    }

    /**
     * Atualiza pagamentos pendentes vencidos para ATRASADO.
     */
    @Transactional
    public int atualizarAtrasados() {
        var atrasados = pagamentoRepository
                .findByStatusAndVencimentoBefore(PagamentoStatus.PENDENTE, LocalDate.now());
        atrasados.forEach(p -> p.setStatus(PagamentoStatus.ATRASADO));
        pagamentoRepository.saveAll(atrasados);
        return atrasados.size();
    }

    private Pagamento getPagamento(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", id));
    }

    private PagamentoResponseDTO toDTO(Pagamento p) {
        Animal animal = p.getAnimal() != null ? p.getAnimal()
                : (p.getAnimalServico() != null ? p.getAnimalServico().getAnimal() : null);
        String servicoNome = p.getAnimalServico() != null
                ? p.getAnimalServico().getServico().getNome() : p.getDescricao();
        return new PagamentoResponseDTO(
                p.getId(),
                p.getAnimalServico() != null ? p.getAnimalServico().getId() : null,
                animal != null ? animal.getId() : null,
                animal != null ? animal.getNome() : null,
                animal != null ? animal.getCliente().getId() : null,
                animal != null ? animal.getCliente().getNome() : null,
                servicoNome,
                p.getDescricao(),
                p.getValor(),
                p.getVencimento(),
                p.getDataPagamento(),
                p.getFormaPagamento(),
                p.getStatus());
    }
}
