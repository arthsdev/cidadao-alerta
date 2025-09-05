package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por gerar arquivos CSV de reclamações.
 */
@Service
@RequiredArgsConstructor
public class CsvService {

    private final ReclamacaoRepository repository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Gera um CSV de reclamações aplicando os filtros fornecidos.
     */
    public ResponseEntity<Resource> gerarResponseCsv(
            StatusReclamacao status,
            Long usuarioId,
            CategoriaReclamacao categoria,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        List<Reclamacao> reclamacoes = buscarReclamacoesFiltradas(status, usuarioId, categoria, dataInicio, dataFim);
        return montarResponseCsv(reclamacoes);
    }

    public List<Reclamacao> buscarReclamacoesFiltradas(
            StatusReclamacao status,
            Long usuarioId,
            CategoriaReclamacao categoria,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return repository.buscarReclamacoesPorFiltrosCompletos(
                status,
                usuarioId,
                categoria,
                dataInicio,
                dataFim
        );
    }

    private ResponseEntity<Resource> montarResponseCsv(List<Reclamacao> reclamacoes) {
        ByteArrayInputStream csvStream = gerarCsv(reclamacoes);
        InputStreamResource file = new InputStreamResource(csvStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reclamacoes.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(file);
    }

    private ByteArrayInputStream gerarCsv(List<Reclamacao> reclamacoes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osWriter)) {

            escreverBomUtf8(out);
            escreverCabecalho(writer);
            escreverLinhas(writer, reclamacoes);

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new CsvGenerationException("Erro ao gerar CSV para as reclamações", e);
        }
    }

    private void escreverBomUtf8(ByteArrayOutputStream out) {
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);
    }

    private void escreverCabecalho(PrintWriter writer) {
        writer.println("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario");
    }

    private void escreverLinhas(PrintWriter writer, List<Reclamacao> reclamacoes) {
        reclamacoes.forEach(r -> writer.println(converterReclamacaoParaLinhaCsv(r)));
    }

    private String converterReclamacaoParaLinhaCsv(Reclamacao r) {
        String lat = r.getLocalizacao() != null && r.getLocalizacao().getLatitude() != null
                ? r.getLocalizacao().getLatitude().toString() : "";
        String lon = r.getLocalizacao() != null && r.getLocalizacao().getLongitude() != null
                ? r.getLocalizacao().getLongitude().toString() : "";

        String[] campos = {
                r.getId() != null ? r.getId().toString() : "",
                escaparCsv(r.getTitulo()),
                escaparCsv(r.getDescricao()),
                r.getCategoriaReclamacao() != null ? r.getCategoriaReclamacao().name() : "",
                r.getStatus() != null ? r.getStatus().name() : "",
                lat,
                lon,
                r.getDataCriacao() != null ? r.getDataCriacao().format(DATE_FORMATTER) : "",
                r.getUsuario() != null ? escaparCsv(r.getUsuario().getNome()) : ""
        };

        return String.join(";", campos);
    }

    private String escaparCsv(String valor) {
        if (valor == null) return "";
        String escaped = valor.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
}
