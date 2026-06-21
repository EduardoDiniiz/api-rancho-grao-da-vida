package com.rancho.api.config;

import com.rancho.api.animal.*;
import com.rancho.api.animalservico.AnimalServico;
import com.rancho.api.animalservico.AnimalServicoRepository;
import com.rancho.api.animalservico.AnimalServicoStatus;
import com.rancho.api.baia.Baia;
import com.rancho.api.baia.BaiaRepository;
import com.rancho.api.baia.BaiaStatus;
import com.rancho.api.cliente.Cliente;
import com.rancho.api.cliente.ClienteRepository;
import com.rancho.api.hospedagem.Hospedagem;
import com.rancho.api.hospedagem.HospedagemRepository;
import com.rancho.api.hospedagem.HospedagemStatus;
import com.rancho.api.pagamento.Pagamento;
import com.rancho.api.pagamento.PagamentoRepository;
import com.rancho.api.pagamento.PagamentoStatus;
import com.rancho.api.servico.Servico;
import com.rancho.api.servico.ServicoRepository;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import com.rancho.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seed de dados para ambiente local (profile "local").
 * Senha de todos os usuarios: Teste@123
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;
    private final AnimalRepository animalRepository;
    private final BaiaRepository baiaRepository;
    private final ServicoRepository servicoRepository;
    private final AnimalServicoRepository animalServicoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final HospedagemRepository hospedagemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Banco ja possui dados, seed ignorado.");
            return;
        }

        log.info("Iniciando seed de dados locais...");
        String senha = passwordEncoder.encode("Teste@123");

        // ===== USUARIOS =====
        userRepository.save(User.builder()
                .name("Administrador do Rancho").email("admin@rancho.com.br")
                .login("admin").password(senha).role(Role.ADMIN).active(true).build());
        userRepository.save(User.builder()
                .name("Operador do Rancho").email("operador@rancho.com.br")
                .login("operador").password(senha).role(Role.OPERADOR).active(true).build());

        // ===== CLIENTES =====
        Cliente joao = clienteRepository.save(Cliente.builder()
                .nome("Joao Vaqueiro").cpfCnpj("52998224725")
                .telefone("83999000001").email("joao@email.com")
                .endereco("Sitio Boa Vista, Zona Rural").active(true).build());
        Cliente maria = clienteRepository.save(Cliente.builder()
                .nome("Maria Amazona").cpfCnpj("11144477735")
                .telefone("83999000002").email("maria@email.com")
                .endereco("Fazenda Sao Jorge").active(true).build());

        // ===== BAIAS =====
        Baia baia1 = baiaRepository.save(Baia.builder()
                .identificacao("B-01").localizacao("Galpao A").capacidade(1)
                .status(BaiaStatus.LIVRE).build());
        Baia baia2 = baiaRepository.save(Baia.builder()
                .identificacao("B-02").localizacao("Galpao A").capacidade(1)
                .status(BaiaStatus.LIVRE).build());
        baiaRepository.save(Baia.builder()
                .identificacao("B-03").localizacao("Galpao B").capacidade(1)
                .status(BaiaStatus.MANUTENCAO).observacao("Reforma do piso").build());

        // ===== ANIMAIS =====
        Animal relampago = animalRepository.save(Animal.builder()
                .cliente(joao).nome("Relampago").sexo(Sexo.MACHO).esporte(Esporte.VAQUEJADA)
                .dataNascimento(LocalDate.of(2019, 5, 10)).registro("VQ-1001")
                .status(AnimalStatus.ATIVO).build());
        Animal estrela = animalRepository.save(Animal.builder()
                .cliente(maria).nome("Estrela").sexo(Sexo.FEMEA).esporte(Esporte.TRES_TAMBORES)
                .dataNascimento(LocalDate.of(2020, 8, 22)).registro("TT-2002")
                .status(AnimalStatus.ATIVO).build());

        // ===== SERVICOS =====
        Servico aluguel = servicoRepository.save(Servico.builder()
                .nome("Aluguel da baia").descricao("Locacao mensal da baia")
                .valorPadrao(new BigDecimal("600.00")).active(true).build());
        Servico racao = servicoRepository.save(Servico.builder()
                .nome("Racao").descricao("Fornecimento de racao")
                .valorPadrao(new BigDecimal("460.00")).active(true).build());
        servicoRepository.save(Servico.builder()
                .nome("Suplemento").descricao("Suplementacao alimentar")
                .valorPadrao(new BigDecimal("150.00")).active(true).build());

        // ===== HOSPEDAGEM =====
        baia1.setStatus(BaiaStatus.OCUPADA);
        baiaRepository.save(baia1);
        hospedagemRepository.save(Hospedagem.builder()
                .animal(relampago).cliente(joao).baia(baia1)
                .dataEntrada(LocalDate.now().minusMonths(2))
                .status(HospedagemStatus.ATIVO).build());

        baia2.setStatus(BaiaStatus.OCUPADA);
        baiaRepository.save(baia2);
        hospedagemRepository.save(Hospedagem.builder()
                .animal(estrela).cliente(maria).baia(baia2)
                .dataEntrada(LocalDate.now().minusMonths(1))
                .status(HospedagemStatus.ATIVO).build());

        // ===== SERVICOS CONTRATADOS + COBRANCAS =====
        contratar(relampago, aluguel, new BigDecimal("600.00"), 30);
        contratar(relampago, racao, new BigDecimal("460.00"), 30);
        contratar(estrela, aluguel, new BigDecimal("700.00"), 30);

        log.info("Seed concluido!");
        log.info("Login admin    -> usuario: admin    | senha: Teste@123");
        log.info("Login operador -> usuario: operador | senha: Teste@123");
    }

    private void contratar(Animal animal, Servico servico, BigDecimal valor, int recorrencia) {
        LocalDate inicio = LocalDate.now().minusDays(10);
        AnimalServico as = animalServicoRepository.save(AnimalServico.builder()
                .animal(animal).servico(servico).valor(valor)
                .dataInicio(inicio).proximoVencimento(inicio.plusDays(recorrencia))
                .recorrenciaDias(recorrencia).status(AnimalServicoStatus.ATIVO).build());

        // cobranca paga do mes anterior
        pagamentoRepository.save(Pagamento.builder()
                .animalServico(as).animal(animal).descricao(servico.getNome())
                .valor(valor).vencimento(inicio).dataPagamento(inicio)
                .formaPagamento(com.rancho.api.pagamento.FormaPagamento.PIX)
                .status(PagamentoStatus.PAGO).build());

        // cobranca pendente atual
        pagamentoRepository.save(Pagamento.builder()
                .animalServico(as).animal(animal).descricao(servico.getNome())
                .valor(valor).vencimento(as.getProximoVencimento())
                .status(PagamentoStatus.PENDENTE).build());
    }
}
