package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.service.TipoCategoraService;
import com.npl.na_ponta_do_lapis.web.dto.CategoriaDTO;
import com.npl.na_ponta_do_lapis.web.exception.ApiExceptionHandler;
import com.npl.na_ponta_do_lapis.web.exception.TipoCategoriaIdNaoExisteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TipoCategoriaControllerTest {

    private MockMvc mockMvc;

        @Mock
    private TipoCategoraService tipoCategoriaService;

        @InjectMocks
        private TipoCategoriaController tipoCategoriaController;

        @BeforeEach
        void setUp() {
                LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
                validator.afterPropertiesSet();

                mockMvc = MockMvcBuilders.standaloneSetup(tipoCategoriaController)
                                .setControllerAdvice(new ApiExceptionHandler())
                                .setValidator(validator)
                                .build();
        }

    @Test
    void shouldListCategories() throws Exception {
        when(tipoCategoriaService.listarCategorias())
                .thenReturn(List.of(new CategoriaDTO(7L, "Alimentação")));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].nome").value("Alimentação"));
    }

    @Test
    void shouldFindCategoryById() throws Exception {
        TipoCategoria categoria = new TipoCategoria();
        categoria.setId(7L);
        categoria.setNome("Alimentação");
        when(tipoCategoriaService.buscarPorId(7L)).thenReturn(categoria);

        mockMvc.perform(get("/categorias/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Alimentação"));
    }

    @Test
    void shouldReturnNotFoundWhenCategoryIdDoesNotExist() throws Exception {
        when(tipoCategoriaService.buscarPorId(99L))
                .thenThrow(new TipoCategoriaIdNaoExisteException("Categoria de ID:99 não encontrado!"));

        mockMvc.perform(get("/categorias/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Categoria de ID:99 não encontrado!"));
    }

    @Test
    void shouldCreateCategory() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Alimentação\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Alimentação"));

        verify(tipoCategoriaService).criarCategoria(argThat(categoria ->
                "Alimentação".equals(categoria.getNome())));
    }

    @Test
    void shouldRejectCategoryWithBlankName() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nome").value("O nome da categoria é obrigatório"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/categorias/{id}", 7L))
                .andExpect(status().isNoContent());

        verify(tipoCategoriaService).deletarCategoria(7L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownCategory() throws Exception {
        org.mockito.Mockito.doThrow(
                        new TipoCategoriaIdNaoExisteException("Categoria de ID: 99 não existe!"))
                .when(tipoCategoriaService).deletarCategoria(99L);

        mockMvc.perform(delete("/categorias/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Categoria de ID: 99 não existe!"));
    }
}
