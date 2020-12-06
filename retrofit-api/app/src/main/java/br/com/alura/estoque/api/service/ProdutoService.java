package br.com.alura.estoque.api.service;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ProdutoService {
    @GET("produto")
    Call<List<Produto>> getProdutos();

    @POST("produto")
    Call<Produto> salva(@Body Produto produto);

    @PUT("produto/{id}")
    Call<Produto> updateProducts(
            @Query("id") long id,
            @Body Produto produto);
}
