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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<Long> salvarDadosDoArquivo(Flux<DadosArquivoDTO> listaDtosFlux) {
        return listaDtosFlux
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .concatMap(dto ->
                        clienteRepository.save(converterParaCliente(dto))
                        .zipWhen(c -> produtoRepository.save(converterParaProduto(dto)))
                        .zipWhen(
                                tuple -> vendedorRepository.save(converterParaVendedor(dto))
                        )
                        .flatMap(tuple -> vendaRepository.save(converterParaVenda(dto)))
                )
                .collectList()
                .flatMap(vendas -> Mono.just((long) vendas.size()));
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

    private Venda converterParaVenda(DadosArquivoDTO dto) {
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

        venda.setClienteId(dto.clienteId());
        venda.setProdutoId(dto.produtoId());
        venda.setVendedorId(dto.vendedorId());

        return venda;
    }
}