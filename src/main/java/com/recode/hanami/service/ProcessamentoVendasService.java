package com.recode.hanami.service;

import com.recode.hanami.dto.DadosArquivoDTO;
import com.recode.hanami.entities.Cliente;
import com.recode.hanami.entities.Produto;
import com.recode.hanami.entities.Venda;
import com.recode.hanami.entities.Vendedor;
import com.recode.hanami.exception.DadosInvalidosException;
import com.recode.hanami.repository.ClienteRepository;
import com.recode.hanami.repository.ProdutoRepository;
import com.recode.hanami.repository.VendaRepository;
import com.recode.hanami.repository.VendedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProcessamentoVendasService {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final VendedorRepository vendedorRepository;
    private final VendaRepository vendaRepository;

    public ProcessamentoVendasService(ClienteRepository clienteRepository,
                                      ProdutoRepository produtoRepository,
                                      VendedorRepository vendedorRepository,
                                      VendaRepository vendaRepository) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.vendedorRepository = vendedorRepository;
        this.vendaRepository = vendaRepository;
    }

    @Transactional
    public void salvarDadosDoArquivo(List<DadosArquivoDTO> listaDtos) {

        for (DadosArquivoDTO dto : listaDtos) {

            Cliente cliente = converterParaCliente(dto);
            clienteRepository.save(cliente);

            Produto produto = converterParaProduto(dto);
            produtoRepository.save(produto);

            Vendedor vendedor = converterParaVendedor(dto);
            vendedorRepository.save(vendedor);

            Venda venda = converterParaVenda(dto, cliente, produto, vendedor);
            vendaRepository.save(venda);
        }
    }

    // Versão reativa: recebe um Flux de DTOs e realiza as operações de persistência
    public Mono<Long> salvarDadosDoArquivo(Flux<DadosArquivoDTO> listaDtosFlux) {
        return listaDtosFlux
                .flatMap(dto -> Mono.fromCallable(() -> {
                    Cliente cliente = converterParaCliente(dto);
                    clienteRepository.save(cliente);

                    Produto produto = converterParaProduto(dto);
                    produtoRepository.save(produto);

                    Vendedor vendedor = converterParaVendedor(dto);
                    vendedorRepository.save(vendedor);

                    Venda venda = converterParaVenda(dto, cliente, produto, vendedor);
                    vendaRepository.save(venda);

                    return 1L;
                }).subscribeOn(Schedulers.boundedElastic()))
                .reduce(0L, Long::sum);
    }

    private Cliente converterParaCliente(DadosArquivoDTO dto) {
        Cliente c = new Cliente();

        if (dto.clienteId() == null || dto.clienteId().trim().isEmpty()) {
            throw new DadosInvalidosException("ID do cliente não pode ser nulo ou vazio");
        }

        c.setId(dto.clienteId());
        c.setNomeCliente(dto.nomeCliente());
        c.setIdadeCliente(dto.idadeCliente());
        c.setGeneroCliente(dto.generoCliente());
        c.setCidadeCliente(dto.cidadeCliente());
        c.setEstadoCliente(dto.estadoCliente());

        c.setRendaEstimada(dto.rendaEstimada());

        return c;
    }

    private Produto converterParaProduto(DadosArquivoDTO dto) {
        Produto p = new Produto();

        if (dto.produtoId() == null || dto.produtoId().trim().isEmpty()) {
            throw new DadosInvalidosException("ID do produto não pode ser nulo ou vazio");
        }

        p.setId(dto.produtoId());
        p.setNomeProduto(dto.nomeProduto());
        p.setCategoria(dto.categoria());
        p.setMarca(dto.marca());
        p.setMargemLucro(dto.margemLucro());
        p.setPrecoUnitario(dto.precoUnitario());

        return p;
    }

    private Vendedor converterParaVendedor(DadosArquivoDTO dto) {
        Vendedor v = new Vendedor();

        if (dto.vendedorId() == null || dto.vendedorId().trim().isEmpty()) {
            throw new DadosInvalidosException("ID do vendedor não pode ser nulo ou vazio");
        }

        v.setId(dto.vendedorId());
        return v;
    }

    private Venda converterParaVenda(DadosArquivoDTO dto, Cliente cliente, Produto produto, Vendedor vendedor) {
        Venda venda = new Venda();

        if (dto.idTransacao() == null || dto.idTransacao().trim().isEmpty()) {
            throw new DadosInvalidosException("ID da transação não pode ser nulo ou vazio");
        }

        venda.setId(dto.idTransacao());
        venda.setDataVenda(dto.dataVenda());

        venda.setValorFinal(dto.valorFinal());
        venda.setSubtotal(dto.subtotal());

        venda.setDescontoPercent(dto.descontoPercent());
        venda.setQuantidade(dto.quantidade());
        venda.setCanalVenda(dto.canalVenda());
        venda.setFormaPagamento(dto.formaPagamento());

        venda.setRegiao(dto.regiao());
        venda.setStatusEntrega(dto.statusEntrega());
        venda.setTempoEntregaDias(dto.tempoEntregaDias());

        if (cliente == null || produto == null || vendedor == null) {
            throw new IllegalArgumentException("Cliente, Produto e Vendedor não podem ser nulos");
        }

        venda.setCliente(cliente);
        venda.setProduto(produto);
        venda.setVendedor(vendedor);

        return venda;
    }
}