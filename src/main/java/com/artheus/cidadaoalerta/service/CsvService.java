package com.artheus.cidadaoalerta.service;

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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por gerar arquivos CSV de reclamações.
 * Busca registros filtrados no repository e monta um CSV compatível com Excel.
 */
@Service
@RequiredArgsConstructor
public class CsvService {

    private final ReclamacaoRepository repository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Gera um CSV de reclamações aplicando os filtros fornecidos.
     *
     * @param status filtro pelo status da reclamação
     * @param usuarioId filtro pelo ID do usuário
     * @param categoria filtro pela categoria da reclamação
     * @param dataInicio filtro a partir desta data (inclusive)
     * @param dataFim filtro até esta data (inclusive)
     * @return ResponseEntity contendo o CSV pronto para download
     */
    public ResponseEntity<Resource> gerarResponseCsv(
            StatusReclamacao status,
            Long usuarioId,
            CategoriaReclamacao categoria,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        List<Reclamacao> reclamacoes = buscarReclamacoesFiltradas(status, usuarioId, categoria, dataInicio, dataFim);
        return montarResponseCsv(reclamacoes);
    }

    /** Busca reclamações filtradas no repository */
    private List<Reclamacao> buscarReclamacoesFiltradas(
            StatusReclamacao status,
            Long usuarioId,
            CategoriaReclamacao categoria,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        return repository.buscarReclamacoesPorFiltrosCompletos(status, usuarioId, categoria, dataInicio, dataFim);
    }

    /** Monta o ResponseEntity com o CSV gerado */
    private ResponseEntity<Resource> montarResponseCsv(List<Reclamacao> reclamacoes) {
        ByteArrayInputStream csvStream = gerarCsv(reclamacoes);
        InputStreamResource file = new InputStreamResource(csvStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reclamacoes.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(file);
    }

    /** Gera o CSV a partir da lista de reclamações */
    private ByteArrayInputStream gerarCsv(List<Reclamacao> reclamacoes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osWriter)) {

            writeBomUtf8(out);
            writeHeader(writer);

            for (Reclamacao r : reclamacoes) {
                writer.println(convertReclamacaoToCsvLine(r));
            }

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CSV", e);
        }
    }

    /** Escreve BOM UTF-8 para compatibilidade com Excel */
    private void writeBomUtf8(ByteArrayOutputStream out) {
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);
    }

    /** Escreve cabeçalho do CSV */
    private void writeHeader(PrintWriter writer) {
        writer.println("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario");
    }

    /** Converte uma reclamação em uma linha CSV */
    private String convertReclamacaoToCsvLine(Reclamacao r) {
        String lat = r.getLocalizacao() != null && r.getLocalizacao().getLatitude() != null
                ? r.getLocalizacao().getLatitude().toString() : "";
        String lon = r.getLocalizacao() != null && r.getLocalizacao().getLongitude() != null
                ? r.getLocalizacao().getLongitude().toString() : "";

        String[] campos = new String[]{
                r.getId() != null ? r.getId().toString() : "",
                escapeCsv(r.getTitulo()),
                escapeCsv(r.getDescricao()),
                r.getCategoriaReclamacao() != null ? r.getCategoriaReclamacao().name() : "",
                r.getStatus() != null ? r.getStatus().name() : "",
                lat,
                lon,
                r.getDataCriacao() != null ? r.getDataCriacao().format(DATE_FORMATTER) : "",
                r.getUsuario() != null ? escapeCsv(r.getUsuario().getNome()) : ""
        };

        return String.join(";", campos);
    }

    /** Escapa valores especiais para CSV */
    private String escapeCsv(String valor) {
        if (valor == null) return "";
        String escaped = valor.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
}
