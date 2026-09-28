package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.dominio.EntidadeBase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Parameter;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.query.QueryParameter;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ConsoleConsultas {

    private static final List<String> SQL_CAPTURADO = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LINHAS_EXIBIDAS = 200;
    private static final int LARGURA_MAXIMA = 48;
    private static final Path PASTA_CONSULTAS = Path.of("consultas");
    private static final ZoneId FUSO = ZoneId.of("America/Fortaleza");
    private static final Pattern PARAMETRO = Pattern.compile("(?<![:\\w]):([A-Za-z_]\\w*)");
    private static final Pattern AGORA = Pattern.compile("(?i)agora(?:([+-])(\\d+)([dhm]))?");
    private static final Set<String> COMANDOS_DE_ALTERACAO = Set.of("update", "delete", "insert");

    private static final java.util.logging.Logger LOG_HIBERNATE = java.util.logging.Logger.getLogger("org.hibernate");

    private record ConsultaNomeada(String nome, boolean nativa, String descricao, String texto) {
        List<String> parametros() {
            return parametrosDoTexto(texto);
        }
    }

    private record Resultado(List<String> colunas, List<List<String>> linhas, int total, Integer alteradas) {
        static Resultado alteracao(int quantidade) {
            return new Resultado(List.of(), List.of(), 0, quantidade);
        }
    }

    private final BufferedReader entrada;
    private final boolean interativo;
    private final Map<String, ConsultaNomeada> nomeadas;
    private EntityManagerFactory emf;
    private boolean modoSql;
    private boolean mostrarSql = true;
    private Integer paginaNumero;
    private Integer paginaTamanho;

    private ConsoleConsultas(BufferedReader entrada, boolean interativo, Map<String, ConsultaNomeada> nomeadas) {
        this.entrada = entrada;
        this.interativo = interativo;
        this.nomeadas = nomeadas;
    }

    public static void main(String[] args) {

        LOG_HIBERNATE.setLevel(java.util.logging.Level.SEVERE);
        Console terminal = System.console();
        BufferedReader entrada = new BufferedReader(terminal != null
                ? terminal.reader()
                : new InputStreamReader(System.in, StandardCharsets.UTF_8));
        ConsoleConsultas console = new ConsoleConsultas(entrada, terminal != null, lerConsultasNomeadas());
        console.iniciar(Arrays.asList(args).contains("--recriar"));
        console.laco();
    }

    private void iniciar(boolean recriar) {
        System.out.println("Conectando ao banco...");
        abrir(false);
        if (recriar || !esquemaExiste()) {
            System.out.println(recriar ? "Recriando o banco..." : "Tabelas não encontradas: criando o esquema...");
            recriarBanco();
        }
    }

    private void laco() {
        boasVindas();
        StringBuilder buffer = new StringBuilder();
        while (true) {
            escrever(buffer.isEmpty() ? (modoSql ? "sql> " : "jpql> ") : "  -> ");
            String linha = lerLinha();
            if (linha == null) {
                break;
            }
            String limpa = linha.strip();
            if (limpa.startsWith("--") || (buffer.isEmpty() && limpa.isEmpty())) {
                continue;
            }
            if (buffer.isEmpty() && limpa.startsWith(":")) {
                if (!comando(limpa)) {
                    break;
                }
                continue;
            }
            buffer.append(linha).append('\n');
            if (limpa.endsWith(";")) {
                String texto = semPontoEVirgula(buffer.toString());
                buffer.setLength(0);
                if (modoSql) {
                    executarSqlNativo(texto);
                } else {
                    executarJpql(texto, Map.of());
                }
            }
        }
        System.out.println("Até mais!");
        emf.close();
    }

    private boolean comando(String linha) {
        String[] partes = linha.split("\\s+", 2);
        String nome = partes[0].toLowerCase(Locale.ROOT);
        String resto = partes.length > 1 ? partes[1].strip() : "";
        switch (nome) {
            case ":sair", ":q", ":quit", ":exit" -> {
                return false;
            }
            case ":ajuda", ":help", ":?" -> ajuda();
            case ":jpql" -> {
                modoSql = false;
                System.out.println("Modo JPQL.");
            }
            case ":sql" -> {
                if (resto.isEmpty()) {
                    modoSql = true;
                    System.out.println("Modo SQL nativo do PostgreSQL (use :jpql para voltar).");
                } else {
                    executarSqlNativo(semPontoEVirgula(resto));
                }
            }
            case ":consultas", ":c" -> listarNomeadas(resto);
            case ":ver", ":v" -> verNomeada(resto);
            case ":executar", ":x" -> executarNomeada(resto);
            case ":arquivos" -> listarArquivos();
            case ":arquivo", ":a" -> executarArquivo(resto);
            case ":mostrarsql" -> {
                mostrarSql = !resto.equalsIgnoreCase("off");
                System.out.println("Exibição do SQL gerado: " + (mostrarSql ? "ligada" : "desligada"));
            }
            case ":pagina" -> configurarPaginacao(resto);
            case ":recriar" -> recriarBanco();
            default -> System.out.println("Comando desconhecido: " + nome + " (digite :ajuda)");
        }
        return true;
    }

    private void executarJpql(String jpql, Map<String, String> argumentos) {
        boolean alteracao = COMANDOS_DE_ALTERACAO.contains(primeiraPalavra(jpql));
        executar(true, em -> {
            Query consulta = em.createQuery(jpql);
            vincularParametros(em, consulta, jpql, argumentos);
            if (alteracao) {
                return Resultado.alteracao(consulta.executeUpdate());
            }
            paginar(consulta);
            return tabela(consulta.getResultList(), colunasDoSelect(jpql));
        });
    }

    private void configurarPaginacao(String resto) {
        String[] partes = resto.split("\\s+");
        if (resto.isEmpty() || partes[0].equalsIgnoreCase("off")) {
            paginaNumero = null;
            paginaTamanho = null;
            System.out.println("Paginação desligada.");
            return;
        }
        try {
            paginaNumero = Integer.valueOf(partes[0]);
            paginaTamanho = partes.length > 1 ? Integer.valueOf(partes[1]) : 10;
            System.out.printf("Paginação ligada: página %d, %d linha(s) por página "
                    + "(setFirstResult(%d), setMaxResults(%d)). Use :pagina off para desligar.%n",
                    paginaNumero, paginaTamanho, paginaNumero * paginaTamanho, paginaTamanho);
        } catch (NumberFormatException e) {
            System.out.println("Uso: :pagina <número, começando em 0> [tamanho]   |   :pagina off");
        }
    }

    private void paginar(Query consulta) {
        if (paginaTamanho != null) {
            consulta.setFirstResult(paginaNumero * paginaTamanho).setMaxResults(paginaTamanho);
        }
    }

    private void executarNomeada(String resto) {
        if (resto.isEmpty()) {
            System.out.println("Uso: :x <nome> [parametro=valor ...]   (veja os nomes com :consultas)");
            return;
        }
        String[] partes = resto.split("\\s+");
        ConsultaNomeada nomeada = localizarNomeada(partes[0]);
        if (nomeada == null) {
            return;
        }
        System.out.println("-- " + nomeada.nome() + (nomeada.nativa() ? " (SQL nativo)" : " (JPQL)"));
        System.out.println(indentar(nomeada.texto()));
        Map<String, String> argumentos = argumentos(partes);
        executar(!nomeada.nativa(), em -> {
            Query consulta = em.createNamedQuery(nomeada.nome());
            vincularParametros(em, consulta, nomeada.texto(), argumentos);
            paginar(consulta);
            return tabela(consulta.getResultList(), nomeada.nativa() ? null : colunasDoSelect(nomeada.texto()));
        });
    }

    private void executarArquivo(String resto) {
        if (resto.isEmpty()) {
            listarArquivos();
            return;
        }
        String[] partes = resto.split("\\s+");
        Path arquivo = localizarArquivo(partes[0]);
        if (arquivo == null) {
            return;
        }
        String texto;
        try {
            texto = semPontoEVirgula(semComentarios(Files.readString(arquivo, StandardCharsets.UTF_8)));
            if (texto.isEmpty()) {
                System.out.println("O arquivo " + arquivo + " não contém nenhuma consulta.");
                return;
            }
        } catch (IOException e) {
            System.out.println("ERRO ao ler " + arquivo + ": " + e.getMessage());
            return;
        }
        System.out.println("-- " + arquivo.toString().replace('\\', '/'));
        System.out.println(indentar(texto));
        if (arquivo.getFileName().toString().endsWith(".sql")) {
            executarSqlNativo(texto);
        } else {
            executarJpql(texto, argumentos(partes));
        }
    }

    private void executarSqlNativo(String sql) {
        executar(false, em -> em.unwrap(Session.class).doReturningWork(conexao -> {
            SQL_CAPTURADO.add(sql);
            try (Statement comando = conexao.createStatement()) {
                if (!comando.execute(sql)) {
                    return Resultado.alteracao(comando.getUpdateCount());
                }
                try (ResultSet rs = comando.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    List<String> colunas = new ArrayList<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        colunas.add(meta.getColumnLabel(i));
                    }
                    List<List<String>> linhas = new ArrayList<>();
                    int total = 0;
                    while (rs.next()) {
                        total++;
                        if (linhas.size() < MAX_LINHAS_EXIBIDAS) {
                            List<String> linha = new ArrayList<>();
                            for (int i = 1; i <= colunas.size(); i++) {
                                linha.add(celula(formatar(rs.getObject(i))));
                            }
                            linhas.add(linha);
                        }
                    }
                    return new Resultado(colunas, linhas, total, null);
                }
            }
        }));
    }

    private void executar(boolean exibirSqlGerado, Function<EntityManager, Resultado> trabalho) {
        SQL_CAPTURADO.clear();
        Resultado resultado;
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                resultado = trabalho.apply(em);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                System.out.println("ERRO: " + mensagem(e));
                System.out.println();
                return;
            }
        }
        imprimir(resultado);
        if (exibirSqlGerado && mostrarSql && !SQL_CAPTURADO.isEmpty()) {
            System.out.println("-- SQL gerado pelo Hibernate:");
            SQL_CAPTURADO.forEach(sql -> System.out.println(indentar(FormatStyle.BASIC.getFormatter().format(sql).strip())));
        }
        String linhas = resultado.alteradas() != null
                ? resultado.alteradas() + " linha(s) alterada(s)"
                : resultado.total() + " linha(s)";
        String pagina = paginaTamanho != null && resultado.alteradas() == null && exibirSqlGerado
                ? " | página " + paginaNumero + ", tamanho " + paginaTamanho
                : "";
        System.out.printf("(%s | %d comando(s) SQL%s)%n%n", linhas, SQL_CAPTURADO.size(), pagina);
    }

    private void vincularParametros(EntityManager em, Query consulta, String texto, Map<String, String> argumentos) {
        List<Parameter<?>> parametros = new ArrayList<>(consulta.getParameters());
        parametros.sort(Comparator.comparingInt(p -> posicaoNoTexto(texto, p)));
        for (Parameter<?> p : parametros) {
            String chave = p.getName() != null ? p.getName() : String.valueOf(p.getPosition());
            Class<?> tipo = p.getParameterType();
            boolean lista = p instanceof QueryParameter<?> qp && qp.allowsMultiValuedBinding();
            String bruto = argumentos.get(chave);
            if (bruto == null) {
                escrever("   :" + chave + " (" + descricaoTipo(tipo, lista) + ") = ");
                bruto = lerLinha();
                if (bruto == null) {
                    throw new IllegalStateException("entrada encerrada antes de informar :" + chave);
                }
            }
            Object valor = lista
                    ? Arrays.stream(bruto.split(",")).map(v -> converter(em, v, tipo)).toList()
                    : converter(em, bruto, tipo);
            if (p.getName() != null) {
                consulta.setParameter(p.getName(), valor);
            } else {
                consulta.setParameter(p.getPosition(), valor);
            }
        }
    }

    private static int posicaoNoTexto(String texto, Parameter<?> p) {
        int i = p.getName() != null ? texto.indexOf(":" + p.getName()) : texto.indexOf("?" + p.getPosition());
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    private static String descricaoTipo(Class<?> tipo, boolean lista) {
        String descricao;
        if (tipo == null || tipo == Object.class) {
            descricao = "valor";
        } else if (tipo.isEnum()) {
            String valores = Arrays.stream(tipo.getEnumConstants()).map(String::valueOf).collect(Collectors.joining("|"));
            descricao = tipo.getSimpleName() + ": " + valores;
        } else if (tipo == Instant.class) {
            descricao = "Instant: 2026-08-10T07:00, agora, agora+7d";
        } else if (tipo == LocalDateTime.class) {
            descricao = "LocalDateTime: 2026-08-10T07:00";
        } else if (tipo == LocalDate.class) {
            descricao = "LocalDate: 2026-08-10, hoje";
        } else if (EntidadeBase.class.isAssignableFrom(tipo)) {
            descricao = tipo.getSimpleName() + ": informe o id";
        } else {
            descricao = tipo.getSimpleName();
        }
        return lista ? descricao + " — lista separada por vírgula" : descricao;
    }

    private static Object converter(EntityManager em, String texto, Class<?> tipo) {
        String v = tirarAspas(texto.strip());
        if (v.equalsIgnoreCase("null")) {
            return null;
        }
        if (tipo == null || tipo == Object.class) {
            return adivinhar(v);
        }
        if (tipo == String.class) {
            return v;
        }
        if (tipo == Long.class || tipo == long.class) {
            return Long.valueOf(v);
        }
        if (tipo == Integer.class || tipo == int.class) {
            return Integer.valueOf(v);
        }
        if (tipo == BigDecimal.class) {
            return new BigDecimal(v);
        }
        if (tipo == Double.class || tipo == double.class) {
            return Double.valueOf(v);
        }
        if (tipo == Boolean.class || tipo == boolean.class) {
            return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("sim") || v.equals("1");
        }
        if (tipo == LocalDate.class) {
            return v.equalsIgnoreCase("hoje") ? LocalDate.now(FUSO) : LocalDate.parse(v);
        }
        if (tipo == LocalDateTime.class) {
            return dataHora(v);
        }
        if (tipo == Instant.class) {
            return instante(v);
        }
        if (tipo.isEnum()) {
            return constanteEnum(tipo, v);
        }
        if (EntidadeBase.class.isAssignableFrom(tipo)) {
            return em.getReference(tipo, Long.valueOf(v));
        }
        return v;
    }

    private static Object adivinhar(String v) {
        if (v.matches("-?\\d+")) return Long.valueOf(v);
        if (v.matches("-?\\d+\\.\\d+")) return new BigDecimal(v);
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) return Boolean.valueOf(v);
        if (v.matches("\\d{4}-\\d{2}-\\d{2}")) return LocalDate.parse(v);
        if (v.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}.*")) return dataHora(v);
        return v;
    }

    private static LocalDateTime dataHora(String v) {
        if (v.equalsIgnoreCase("agora")) return LocalDateTime.now(FUSO);
        if (v.length() == 10) return LocalDate.parse(v).atStartOfDay();
        return LocalDateTime.parse(v.replace(' ', 'T'));
    }

    private static Instant instante(String v) {
        Matcher agora = AGORA.matcher(v);
        if (agora.matches()) {
            Instant base = Instant.now();
            if (agora.group(1) == null) return base;
            long n = Long.parseLong(agora.group(2));
            Duration d = switch (agora.group(3).toLowerCase(Locale.ROOT)) {
                case "d" -> Duration.ofDays(n);
                case "h" -> Duration.ofHours(n);
                default -> Duration.ofMinutes(n);
            };
            return agora.group(1).equals("+") ? base.plus(d) : base.minus(d);
        }
        if (v.endsWith("Z")) return Instant.parse(v);
        if (v.matches(".*[+-]\\d{2}:\\d{2}$")) return OffsetDateTime.parse(v).toInstant();
        return dataHora(v).atZone(FUSO).toInstant();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object constanteEnum(Class<?> tipo, String v) {
        try {
            return Enum.valueOf((Class) tipo, v.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("'" + v + "' não é um valor de " + tipo.getSimpleName() + ": "
                    + Arrays.toString(tipo.getEnumConstants()));
        }
    }

    private static Map<String, String> argumentos(String[] partes) {
        Map<String, String> argumentos = new LinkedHashMap<>();
        for (int i = 1; i < partes.length; i++) {
            int igual = partes[i].indexOf('=');
            if (igual > 0) {
                argumentos.put(partes[i].substring(0, igual).replaceFirst("^:", ""), partes[i].substring(igual + 1));
            } else {
                System.out.println("(ignorado: '" + partes[i] + "' — use parametro=valor)");
            }
        }
        return argumentos;
    }

    private static Resultado tabela(List<?> resultado, List<String> colunasDoTexto) {
        List<Object[]> todas = resultado.stream().map(o -> o instanceof Object[] a ? a : new Object[]{o}).toList();
        List<Object[]> exibidas = todas.subList(0, Math.min(todas.size(), MAX_LINHAS_EXIBIDAS));
        boolean umaColuna = !exibidas.isEmpty() && exibidas.stream().allMatch(l -> l.length == 1);
        if (umaColuna && exibidas.stream().allMatch(l -> l[0] instanceof Record)) {
            return deRecords(exibidas, todas.size());
        }
        if (umaColuna && exibidas.stream().allMatch(l -> l[0] instanceof EntidadeBase)) {
            return deEntidades(exibidas, todas.size());
        }
        int n = exibidas.isEmpty() ? (colunasDoTexto == null ? 0 : colunasDoTexto.size()) : exibidas.get(0).length;
        List<String> colunas = colunasDoTexto != null && colunasDoTexto.size() == n
                ? colunasDoTexto
                : IntStream.rangeClosed(1, n).mapToObj(i -> "coluna" + i).toList();
        List<List<String>> linhas = exibidas.stream()
                .map(l -> Arrays.stream(l).map(v -> celula(formatar(v))).toList())
                .toList();
        return new Resultado(colunas, linhas, todas.size(), null);
    }

    private static Resultado deRecords(List<Object[]> linhas, int total) {
        RecordComponent[] componentes = linhas.get(0)[0].getClass().getRecordComponents();
        List<String> colunas = Arrays.stream(componentes).map(RecordComponent::getName).toList();
        List<List<String>> celulas = new ArrayList<>();
        for (Object[] l : linhas) {
            List<String> linha = new ArrayList<>();
            for (RecordComponent c : componentes) {
                try {
                    linha.add(celula(formatar(c.getAccessor().invoke(l[0]))));
                } catch (ReflectiveOperationException e) {
                    linha.add("?");
                }
            }
            celulas.add(linha);
        }
        return new Resultado(colunas, celulas, total, null);
    }

    private static Resultado deEntidades(List<Object[]> linhas, int total) {
        List<Map<String, String>> registros = linhas.stream().map(l -> camposDaEntidade(l[0])).toList();
        boolean variosTipos = registros.stream().map(r -> r.get("entidade")).distinct().count() > 1;
        Set<String> colunas = new LinkedHashSet<>();
        registros.forEach(r -> colunas.addAll(r.keySet()));
        if (!variosTipos) {
            colunas.remove("entidade");
        }
        List<List<String>> celulas = registros.stream()
                .map(r -> colunas.stream().map(c -> celula(r.getOrDefault(c, ""))).toList())
                .toList();
        return new Resultado(List.copyOf(colunas), celulas, total, null);
    }

    private static Map<String, String> camposDaEntidade(Object entidade) {
        Object alvo = entidade instanceof HibernateProxy ? Hibernate.unproxy(entidade) : entidade;
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("entidade", alvo.getClass().getSimpleName());
        campos.put("id", String.valueOf(((EntidadeBase) alvo).getId()));
        List<Class<?>> hierarquia = new ArrayList<>();
        for (Class<?> c = alvo.getClass(); c != null && c != EntidadeBase.class; c = c.getSuperclass()) {
            hierarquia.add(0, c);
        }
        for (Class<?> classe : hierarquia) {
            for (Field campo : classe.getDeclaredFields()) {
                int mod = campo.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || campo.isSynthetic()
                        || campo.getName().startsWith("$$_hibernate")) {
                    continue;
                }
                try {
                    campo.setAccessible(true);
                    campos.put(campo.getName(), formatarAtributo(alvo, campo.getName(), campo.get(alvo)));
                } catch (ReflectiveOperationException | RuntimeException e) {
                    campos.put(campo.getName(), "?");
                }
            }
        }
        return campos;
    }

    private static String formatarAtributo(Object dono, String nome, Object valor) {
        if (valor instanceof Collection<?> || valor instanceof EntidadeBase) {
            return formatar(valor);
        }
        if (!Hibernate.isPropertyInitialized(dono, nome)) {
            return "(LAZY)";
        }
        return formatar(valor);
    }

    private static String formatar(Object v) {
        if (v == null) return "null";
        if (v instanceof byte[] b) return "<" + b.length + " bytes>";
        if (v instanceof BigDecimal d) return d.toPlainString();
        if (v instanceof Enum<?> e) return e.name();
        if (v instanceof Class<?> c) return c.getSimpleName();
        if (v instanceof Collection<?> c) return Hibernate.isInitialized(c) ? "[" + c.size() + " itens]" : "(LAZY)";
        if (v instanceof HibernateProxy p) {
            LazyInitializer li = p.getHibernateLazyInitializer();
            return li.getPersistentClass().getSimpleName() + "#" + li.getIdentifier() + (li.isUninitialized() ? " (LAZY)" : "");
        }
        if (v instanceof EntidadeBase e) {
            return e.getClass().getSimpleName() + "#" + e.getId() + (Hibernate.isInitialized(e) ? "" : " (LAZY)");
        }
        return String.valueOf(v);
    }

    private static String celula(String texto) {
        String uma = texto.replaceAll("\\s+", " ");
        return uma.length() <= LARGURA_MAXIMA ? uma : uma.substring(0, LARGURA_MAXIMA - 1) + "…";
    }

    private static void imprimir(Resultado r) {
        if (r.alteradas() != null || r.colunas().isEmpty()) {
            return;
        }
        int[] larguras = new int[r.colunas().size()];
        for (int i = 0; i < larguras.length; i++) {
            larguras[i] = r.colunas().get(i).length();
            for (List<String> linha : r.linhas()) {
                larguras[i] = Math.max(larguras[i], linha.get(i).length());
            }
        }
        System.out.println(linhaDaTabela(r.colunas(), larguras));
        System.out.println(Arrays.stream(larguras).mapToObj("-"::repeat).collect(Collectors.joining("-+-", "-", "-")));
        r.linhas().forEach(l -> System.out.println(linhaDaTabela(l, larguras)));
        if (r.total() > r.linhas().size()) {
            System.out.printf("... exibindo %d de %d linhas%n", r.linhas().size(), r.total());
        }
    }

    private static String linhaDaTabela(List<String> celulas, int[] larguras) {
        StringBuilder sb = new StringBuilder(" ");
        for (int i = 0; i < celulas.size(); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(celulas.get(i)).append(" ".repeat(larguras[i] - celulas.get(i).length()));
        }
        return sb.toString().stripTrailing();
    }

    private static List<String> colunasDoSelect(String jpql) {
        Matcher inicio = Pattern.compile("(?is)^\\s*select\\s+(distinct\\s+)?").matcher(jpql);
        if (!inicio.find()) {
            return null;
        }
        List<String> itens = new ArrayList<>();
        int nivel = 0;
        boolean aspas = false;
        int comeco = inicio.end();
        String minusculo = jpql.toLowerCase(Locale.ROOT);
        for (int i = comeco; i < jpql.length(); i++) {
            char c = jpql.charAt(i);
            if (c == '\'') aspas = !aspas;
            if (aspas) continue;
            if (c == '(') nivel++;
            else if (c == ')') nivel--;
            else if (nivel == 0 && c == ',') {
                itens.add(jpql.substring(comeco, i));
                comeco = i + 1;
            } else if (nivel == 0 && minusculo.startsWith("from", i) && Character.isWhitespace(jpql.charAt(i - 1))
                    && (i + 4 == jpql.length() || Character.isWhitespace(jpql.charAt(i + 4)))) {
                itens.add(jpql.substring(comeco, i));
                return itens.stream().map(ConsoleConsultas::rotulo).toList();
            }
        }
        return null;
    }

    private static String rotulo(String item) {
        String t = item.strip().replaceAll("\\s+", " ");
        Matcher alias = Pattern.compile("(?i)\\s+as\\s+(\\w+)$").matcher(t);
        return alias.find() ? alias.group(1) : t;
    }

    private static Map<String, ConsultaNomeada> lerConsultasNomeadas() {
        Map<String, ConsultaNomeada> mapa = new LinkedHashMap<>();
        try (InputStream in = ConsoleConsultas.class.getClassLoader().getResourceAsStream("META-INF/orm.xml")) {
            if (in == null) {
                return mapa;
            }
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = fabrica.newDocumentBuilder().parse(in);
            adicionarNomeadas(doc, "named-query", false, mapa);
            adicionarNomeadas(doc, "named-native-query", true, mapa);
        } catch (Exception e) {
            System.out.println("Aviso: não foi possível ler META-INF/orm.xml: " + e.getMessage());
        }
        return mapa;
    }

    private static void adicionarNomeadas(Document doc, String elemento, boolean nativa, Map<String, ConsultaNomeada> mapa) {
        NodeList nos = doc.getElementsByTagName(elemento);
        for (int i = 0; i < nos.getLength(); i++) {
            Element e = (Element) nos.item(i);
            String nome = e.getAttribute("name");
            mapa.put(nome, new ConsultaNomeada(nome, nativa, textoDoFilho(e, "description").strip(),
                    semIndentacaoComum(textoDoFilho(e, "query"))));
        }
    }

    private static String textoDoFilho(Element pai, String filho) {
        NodeList nos = pai.getElementsByTagName(filho);
        return nos.getLength() == 0 ? "" : nos.item(0).getTextContent();
    }

    private static String semIndentacaoComum(String texto) {
        List<String> linhas = new ArrayList<>(texto.lines().toList());
        while (!linhas.isEmpty() && linhas.get(0).isBlank()) linhas.remove(0);
        while (!linhas.isEmpty() && linhas.get(linhas.size() - 1).isBlank()) linhas.remove(linhas.size() - 1);
        int comum = linhas.stream().filter(l -> !l.isBlank())
                .mapToInt(l -> l.length() - l.stripLeading().length()).min().orElse(0);
        return linhas.stream().map(l -> l.isBlank() ? "" : l.substring(comum).stripTrailing())
                .collect(Collectors.joining("\n"));
    }

    private void listarNomeadas(String filtro) {
        System.out.println("Consultas externalizadas em META-INF/orm.xml (execute com :x <nome> [param=valor ...]):");
        nomeadas.values().stream()
                .filter(c -> filtro.isEmpty() || c.nome().toLowerCase(Locale.ROOT).contains(filtro.toLowerCase(Locale.ROOT)))
                .forEach(c -> {
                    String params = c.parametros().stream().map(p -> ":" + p).collect(Collectors.joining(" "));
                    System.out.printf("  %-46s %-11s %s%n", c.nome(), c.nativa() ? "[SQL nativo]" : "[JPQL]", params);
                    System.out.println("      " + c.descricao());
                });
        System.out.println();
    }

    private void verNomeada(String nome) {
        ConsultaNomeada c = localizarNomeada(nome);
        if (c != null) {
            System.out.println("-- " + c.nome() + (c.nativa() ? " (SQL nativo)" : " (JPQL)") + ": " + c.descricao());
            System.out.println(indentar(c.texto()));
            System.out.println();
        }
    }

    private ConsultaNomeada localizarNomeada(String nome) {
        if (nome.isEmpty()) {
            System.out.println("Informe o nome da consulta (veja :consultas).");
            return null;
        }
        ConsultaNomeada exata = nomeadas.get(nome);
        if (exata != null) {
            return exata;
        }
        String busca = nome.toLowerCase(Locale.ROOT);
        List<ConsultaNomeada> candidatas = nomeadas.values().stream()
                .filter(c -> c.nome().toLowerCase(Locale.ROOT).contains(busca)).toList();
        if (candidatas.size() == 1) {
            return candidatas.get(0);
        }
        System.out.println(candidatas.isEmpty()
                ? "Consulta '" + nome + "' não encontrada (veja :consultas)."
                : "Mais de uma consulta contém '" + nome + "': "
                  + candidatas.stream().map(ConsultaNomeada::nome).collect(Collectors.joining(", ")));
        return null;
    }

    private void listarArquivos() {
        if (!Files.isDirectory(PASTA_CONSULTAS)) {
            System.out.println("Pasta " + PASTA_CONSULTAS.toAbsolutePath() + " não encontrada.");
            return;
        }
        System.out.println("Consultas em arquivos (" + PASTA_CONSULTAS + "/). Edite e execute com :a <arquivo>, sem recompilar:");
        for (Path arquivo : arquivosDeConsulta()) {
            System.out.printf("  %-40s %s%n", arquivo.getFileName(), primeiroComentario(arquivo));
        }
        System.out.println();
    }

    private static List<Path> arquivosDeConsulta() {
        try (Stream<Path> arquivos = Files.list(PASTA_CONSULTAS)) {
            return arquivos.filter(p -> p.toString().endsWith(".jpql") || p.toString().endsWith(".sql"))
                    .sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path localizarArquivo(String nome) {
        Path direto = Path.of(nome);
        if (Files.isRegularFile(direto)) return direto;
        Path naPasta = PASTA_CONSULTAS.resolve(nome);
        if (Files.isRegularFile(naPasta)) return naPasta;
        if (Files.isDirectory(PASTA_CONSULTAS)) {
            List<Path> candidatos = arquivosDeConsulta().stream()
                    .filter(p -> p.getFileName().toString().startsWith(nome)).toList();
            if (candidatos.size() == 1) return candidatos.get(0);
        }
        System.out.println("Arquivo '" + nome + "' não encontrado (veja :arquivos).");
        return null;
    }

    private static String primeiroComentario(Path arquivo) {
        try (Stream<String> linhas = Files.lines(arquivo, StandardCharsets.UTF_8)) {
            return linhas.map(String::strip).filter(l -> l.startsWith("--")).findFirst()
                    .map(l -> l.substring(2).strip()).orElse("");
        } catch (IOException e) {
            return "";
        }
    }

    private void abrir(boolean recriarEsquema) {
        Map<String, Object> props = Configuracao.propriedadesDoAmbiente();
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.use_sql_comments", "false");
        props.put("hibernate.generate_statistics", "false");
        if (recriarEsquema) {
            props.putAll(Configuracao.recriacaoDoEsquema(Configuracao.SCRIPT_POS_CRIACAO));
        } else {
            props.put("jakarta.persistence.schema-generation.database.action", "none");
        }
        props.put("hibernate.session_factory.statement_inspector",
                (StatementInspector) sql -> {
                    SQL_CAPTURADO.add(sql);
                    return sql;
                });
        emf = Persistence.createEntityManagerFactory(Configuracao.UNIDADE_PERSISTENCIA, props);
    }

    private boolean esquemaExiste() {
        try (EntityManager em = emf.createEntityManager()) {
            Number n = (Number) em.createNativeQuery("select count(*) from information_schema.tables "
                    + "where table_schema = current_schema() and table_name = 'expedicao'").getSingleResult();
            return n.intValue() > 0;
        }
    }

    private void recriarBanco() {
        emf.close();
        abrir(true);
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            DadosExemplo.popular(em);
            em.getTransaction().commit();
        }
        System.out.println("Esquema recriado e dados de exemplo carregados.");
    }

    private void boasVindas() {
        System.out.println();
        System.out.println("TurmalinaPB — console de consultas (JPQL, SQL nativo e consultas externalizadas)");
        System.out.println("Banco: " + emf.getProperties().get("jakarta.persistence.jdbc.url"));
        System.out.println("Termine cada consulta com ';'. Ex.: select e.codigo, e.situacao from Expedicao e;");
        System.out.println(":ajuda lista os comandos | :consultas lista as consultas nomeadas | :sair encerra");
        System.out.println();
    }

    private static void ajuda() {
        System.out.println("""
                Consultas
                  <JPQL>;                     executa JPQL (pode ocupar várias linhas; termine com ';')
                  :sql                        passa para SQL nativo do PostgreSQL (:jpql volta)
                  :sql <comando>;             executa um único comando SQL nativo
                Consultas externalizadas
                  :consultas [filtro]         lista as consultas nomeadas de META-INF/orm.xml
                  :ver <nome>                 mostra o texto de uma consulta nomeada
                  :x <nome> [p=valor ...]     executa uma consulta nomeada
                  :arquivos                   lista as consultas da pasta consultas/
                  :a <arquivo> [p=valor ...]  executa um arquivo .jpql ou .sql (aceita só o prefixo, ex.: :a 03)
                Outros
                  :pagina <n> [tamanho]       pagina as próximas consultas (setFirstResult/setMaxResults); :pagina off
                  :mostrarsql on|off          exibe ou oculta o SQL gerado pelo Hibernate
                  :recriar                    recria o esquema e recarrega os dados de exemplo
                  :sair
                Parâmetros não informados são perguntados. Formatos: 2026-08-10 | 2026-08-10T07:00 | agora, agora+7d
                | enum pelo nome (CONCLUIDA) | listas separadas por vírgula (PLANEJADA,CONCLUIDA) | entidade pelo id.
                """);
    }

    private String lerLinha() {
        try {
            String linha = entrada.readLine();
            if (linha == null) {
                return null;
            }
            linha = semBom(linha);
            if (!interativo) {
                System.out.println(linha);
            }
            return linha;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void escrever(String texto) {
        System.out.print(texto);
        System.out.flush();
    }

    private static List<String> parametrosDoTexto(String texto) {
        Matcher m = PARAMETRO.matcher(texto);
        Set<String> nomes = new LinkedHashSet<>();
        while (m.find()) {
            nomes.add(m.group(1));
        }
        return List.copyOf(nomes);
    }

    private static String primeiraPalavra(String texto) {
        return texto.strip().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
    }

    private static String semPontoEVirgula(String texto) {
        String t = texto.strip();
        while (t.endsWith(";")) {
            t = t.substring(0, t.length() - 1).strip();
        }
        return t;
    }

    private static String semComentarios(String texto) {
        return semBom(texto).lines().filter(l -> !l.strip().startsWith("--")).collect(Collectors.joining("\n")).strip();
    }

    private static String semBom(String texto) {
        return texto.startsWith("﻿") ? texto.substring(1) : texto;
    }

    private static String tirarAspas(String v) {
        if (v.length() >= 2 && ((v.startsWith("'") && v.endsWith("'")) || (v.startsWith("\"") && v.endsWith("\"")))) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    private static String indentar(String texto) {
        return texto.lines().map(l -> "    " + l).collect(Collectors.joining("\n"));
    }

    private static String mensagem(Throwable e) {
        String msg = e.getMessage();
        for (Throwable t = e.getCause(); t != null; t = t.getCause()) {
            if (t.getMessage() != null) {
                msg = t.getMessage();
            }
        }
        return msg == null ? e.getClass().getSimpleName()
                : msg.lines().limit(3).collect(Collectors.joining(" "));
    }
}
