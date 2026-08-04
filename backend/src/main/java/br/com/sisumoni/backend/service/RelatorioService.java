package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Classificacao;
import br.com.sisumoni.backend.domain.Vaga;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.ClassificacaoRepository;
import br.com.sisumoni.backend.repository.VagaRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private static final Font FONTE_TITULO = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font FONTE_DEPARTAMENTO = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0x2A, 0x20, 0x10));
    private static final Font FONTE_VAGA = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FONTE_CABECALHO_TABELA = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font FONTE_CELULA = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Color COR_CABECALHO_TABELA = new Color(0x2A, 0x20, 0x10);

    // Cores por tipo de vaga — mesma paleta usada no frontend (tema claro)
    private static final Color COR_BOLSISTA = new Color(0x05, 0x96, 0x69);
    private static final Color COR_BOLSISTA_BG = new Color(0xE6, 0xF7, 0xEF);
    private static final Color COR_VOLUNTARIO = new Color(0x1D, 0x4E, 0xD8);
    private static final Color COR_VOLUNTARIO_BG = new Color(0xDC, 0xE8, 0xFD);
    private static final Color COR_LISTA_ESPERA = new Color(0x5B, 0x6B, 0x85);
    private static final Color COR_LISTA_ESPERA_BG = new Color(0xF2, 0xEF, 0xE7);
    private static final Color COR_ALERTA = new Color(0xB9, 0x1C, 0x1C);
    private static final Font FONTE_ALERTA = new Font(Font.HELVETICA, 10, Font.BOLD, COR_ALERTA);

    private final VagaRepository vagaRepository;
    private final ClassificacaoRepository classificacaoRepository;
    private final byte[] simboloPng;
    private final byte[] logoPng;

    public RelatorioService(VagaRepository vagaRepository, ClassificacaoRepository classificacaoRepository) {
        this.vagaRepository = vagaRepository;
        this.classificacaoRepository = classificacaoRepository;
        this.simboloPng = carregarRecurso("/relatorio/simbolo.png");
        this.logoPng = carregarRecurso("/relatorio/logo.png");
    }

    private byte[] carregarRecurso(String caminho) {
        try (InputStream in = getClass().getResourceAsStream(caminho)) {
            if (in == null) {
                throw new IllegalStateException("Recurso não encontrado: " + caminho);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] gerarRelatorio(boolean comNomesReais) {
        if (classificacaoRepository.existsByEmpateTrue()) {
            throw new RegraDeNegocioException(
                    "Existe empate pendente de resolução. Resolva antes de exportar o relatório.");
        }

        Map<String, List<Vaga>> vagasPorDepartamento = vagaRepository.findAllComRelacionamentos().stream()
                .collect(Collectors.groupingBy(v -> v.getDepartamento().getNome(), TreeMap::new, Collectors.toList()));

        try {
            Document documento = new Document(PageSize.A4, 40, 40, 50, 40);
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            PdfWriter.getInstance(documento, saida);
            documento.open();

            adicionarCabecalho(documento, comNomesReais ? "Classificação Final" : "Classificação Parcial");
            documento.add(new Paragraph(" "));

            for (Map.Entry<String, List<Vaga>> entry : vagasPorDepartamento.entrySet()) {
                Paragraph tituloDepartamento = new Paragraph(entry.getKey(), FONTE_DEPARTAMENTO);
                tituloDepartamento.setSpacingBefore(12);
                tituloDepartamento.setSpacingAfter(6);
                documento.add(tituloDepartamento);

                List<Vaga> vagasDoDepartamento = entry.getValue().stream()
                        .sorted(Comparator.comparing(Vaga::getDisciplina))
                        .toList();

                for (Vaga vaga : vagasDoDepartamento) {
                    adicionarVaga(documento, vaga, comNomesReais);
                }
            }

            documento.close();
            return saida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erro ao gerar o relatório em PDF", e);
        }
    }

    private void adicionarCabecalho(Document documento, String tituloTexto) throws DocumentException, IOException {
        PdfPTable cabecalho = new PdfPTable(new float[]{1f, 4f, 1f});
        cabecalho.setWidthPercentage(100);

        Image simbolo = Image.getInstance(simboloPng);
        simbolo.scaleToFit(45, 45);
        PdfPCell celulaSimbolo = new PdfPCell(simbolo);
        celulaSimbolo.setBorder(0);
        celulaSimbolo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaSimbolo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cabecalho.addCell(celulaSimbolo);

        PdfPCell celulaTitulo = new PdfPCell(new Phrase(tituloTexto, FONTE_TITULO));
        celulaTitulo.setBorder(0);
        celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cabecalho.addCell(celulaTitulo);

        Image logo = Image.getInstance(logoPng);
        logo.scaleToFit(90, 45);
        PdfPCell celulaLogo = new PdfPCell(logo);
        celulaLogo.setBorder(0);
        celulaLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cabecalho.addCell(celulaLogo);

        cabecalho.setSpacingAfter(14);
        documento.add(cabecalho);
    }

    private void adicionarVaga(Document documento, Vaga vaga, boolean comNomesReais) throws DocumentException {
        Paragraph tituloVaga = new Paragraph(vaga.getDisciplina() + " — Prof. " + vaga.getProfessor(), FONTE_VAGA);
        tituloVaga.setSpacingBefore(8);
        tituloVaga.setSpacingAfter(4);
        documento.add(tituloVaga);

        if (vaga.isAlertaChoqueHorario()) {
            Paragraph alerta = new Paragraph("ATENÇÃO! Verifique choque de horário", FONTE_ALERTA);
            alerta.setSpacingAfter(4);
            documento.add(alerta);
        }

        List<Classificacao> classificacoes = classificacaoRepository.findByVagaIdComEstudante(vaga.getId());
        if (classificacoes.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhum estudante classificado.", FONTE_CELULA);
            vazio.setSpacingAfter(6);
            documento.add(vazio);
            return;
        }

        PdfPTable tabela = new PdfPTable(comNomesReais
                ? new float[]{1.5f, 5f, 2.5f}
                : new float[]{1.5f, 5f, 2f, 2.5f});
        tabela.setWidthPercentage(100);
        tabela.setSpacingAfter(10);

        String[] cabecalhos = comNomesReais
                ? new String[]{"Posição", "Nome", "Tipo"}
                : new String[]{"Posição", "Nome", "Pontuação", "Tipo"};
        for (String cabecalho : cabecalhos) {
            PdfPCell celula = new PdfPCell(new Phrase(cabecalho, FONTE_CABECALHO_TABELA));
            celula.setBackgroundColor(COR_CABECALHO_TABELA);
            celula.setPadding(5);
            tabela.addCell(celula);
        }

        for (Classificacao c : classificacoes) {
            String nome = comNomesReais ? c.getEstudante().getNome() : c.getEstudante().getNomeFantasia();
            String tipo;
            Color corTexto;
            Color corFundo;
            switch (c.getTipo()) {
                case BOLSISTA -> {
                    tipo = "Bolsista";
                    corTexto = COR_BOLSISTA;
                    corFundo = COR_BOLSISTA_BG;
                }
                case VOLUNTARIO -> {
                    tipo = "Voluntário";
                    corTexto = COR_VOLUNTARIO;
                    corFundo = COR_VOLUNTARIO_BG;
                }
                default -> {
                    tipo = "Lista de Espera";
                    corTexto = COR_LISTA_ESPERA;
                    corFundo = COR_LISTA_ESPERA_BG;
                }
            }
            boolean listaEspera = c.getTipo() == Classificacao.Tipo.LISTA_ESPERA;

            tabela.addCell(celulaTexto(listaEspera ? "–" : String.valueOf(c.getPosicao()), corFundo, FONTE_CELULA));
            tabela.addCell(celulaTexto(nome, corFundo, FONTE_CELULA));
            if (!comNomesReais) {
                tabela.addCell(celulaTexto(fmtPontuacao(c.getPontuacao()), corFundo, FONTE_CELULA));
            }
            tabela.addCell(celulaTexto(tipo, corFundo, new Font(Font.HELVETICA, 10, Font.BOLD, corTexto)));
        }

        documento.add(tabela);
    }

    private PdfPCell celulaTexto(String texto, Color corFundo, Font fonte) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBackgroundColor(corFundo);
        celula.setPadding(5);
        return celula;
    }

    private String fmtPontuacao(java.math.BigDecimal pontuacao) {
        if (pontuacao == null) {
            return "—";
        }
        return pontuacao.setScale(4, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
