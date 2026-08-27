package com.rmfarma.pharmahub.infrastructure.mapper;

import com.rmfarma.pharmahub.core.model.QueryDefinition;
import com.rmfarma.pharmahub.core.port.QueryRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garante que toda query com mapper declarado no metadata.yaml resolve para o mapper
 * específico — e não para o GenericMapMapper, que devolve chaves posicionais
 * (field_0, field_1, ...) em vez dos nomes reais das colunas.
 */
@QuarkusTest
class MapperResolverTest {

    @Inject
    MapperResolver mapperResolver;

    @Inject
    QueryRepository queryRepository;

    @Test
    void resolveMapperEspecificoParaTodasAsQueriesComMapperDeclarado() {
        List<QueryDefinition> definitions = queryRepository.findAll();
        assertFalse(definitions.isEmpty(), "Nenhuma query carregada do classpath");

        for (QueryDefinition definition : definitions) {
            if (definition.mapperClassName() == null || definition.mapperClassName().isBlank()) {
                continue;
            }
            RowMapper<?> mapper = mapperResolver.resolve(definition);
            assertNotNull(mapper, "Mapper nulo para " + definition.key());
            // O bean chega como client proxy do ArC, então a classe real é a superclasse.
            String resolvedClass = mapper.getClass().getName();
            assertTrue(resolvedClass.startsWith(definition.mapperClassName()),
                    "Query '" + definition.key() + "' deveria resolver para "
                            + definition.mapperClassName() + " mas resolveu para " + resolvedClass);
        }
    }

    /**
     * Documenta a causa raiz: o bean chega como client proxy do ArC e a subclasse gerada
     * não carrega o @Named do bean original — por isso a resolução via
     * {@code instancia.getClass().getAnnotation(Named.class)} sempre devolvia null e caía
     * no mapper genérico. Se este teste falhar, a resolução por anotação voltou a ser viável.
     */
    @Test
    void clientProxyNaoExpoeAnotacaoNamed() {
        QueryDefinition definition = queryRepository.findByKey("abc-curve-products").orElseThrow();
        RowMapper<?> mapper = mapperResolver.resolve(definition);
        assertNull(mapper.getClass().getAnnotation(Named.class),
                "Proxy passou a expor @Named — revisar a estratégia de resolução");
    }
}
