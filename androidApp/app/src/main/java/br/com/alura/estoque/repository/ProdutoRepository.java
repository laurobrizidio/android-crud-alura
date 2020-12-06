package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.api.AppRetrofit;
import br.com.alura.estoque.api.service.ProdutoService;
import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        service = AppRetrofit.getProdutoService();

    }


    public void buscaProdutos(Result<List<Produto>> listener) {
        searchInDatabase(listener);
    }

    public void searchInDatabase(Result<List<Produto>> listener) {
        new BaseAsyncTask<>(dao::buscaTodos
                , resultado -> {
            listener.Sucess(resultado);
            searchProductsInAPI(listener);
        }).execute();
    }

    private void searchProductsInAPI(Result<List<Produto>> listener) {
        service = AppRetrofit.getProdutoService();
        Call<List<Produto>> call = service.getProdutos();
        new BaseAsyncTask<>(() -> {
            try {
                Response<List<Produto>> listResponse = call.execute();
                dao.salva(listResponse.body());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dao.buscaTodos();
        }, listener::Sucess) //Notifica que o dado esta pronto
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void salva(Produto produto, Result<Produto> listener) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                Produto produtoSalvo = response.body();
                new BaseAsyncTask<>(() -> {
                    long id = dao.salva(produtoSalvo);
                    return dao.buscaProduto(id);
                }, listener::Sucess)
                        .execute();
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });


    }

    public void update(Produto produto, Result<Produto> listener) {
        Call<Produto> call = service.updateProducts(produto.getId(), produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                Produto produtoEditado = response.body();
                if (produtoEditado != null) {
                    new BaseAsyncTask<>(() -> {
                        dao.atualiza(produtoEditado);
                        return produtoEditado;
                    }, listener::Sucess)
                            .execute();
                }

            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Produto> call, Throwable t) {

                listener.onError(null);
            }
        });
    }

    public void remove(Produto produtoRemovido, Result<Boolean> listener) {

        Call<Void> call = service.remove(produtoRemovido.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    new BaseAsyncTask<>(() -> {
                        dao.remove(produtoRemovido);
                        return produtoRemovido;
                    }, resultado -> listener.Sucess(true))
                            .execute();
                } else {
                    listener.onError(false);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Void> call, Throwable t) {
                listener.onError(false);
            }
        });

    }

    public interface Result<T> {
        void Sucess(T data);

        void onError(T data);
    }

}
