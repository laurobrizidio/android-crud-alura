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

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        service = AppRetrofit.getProdutoService();

    }


    public void buscaProdutos(RepositoryListener<List<Produto>> listener) {
        searchInDatabase(listener);
    }

    public void searchInDatabase(RepositoryListener<List<Produto>> listener) {
        new BaseAsyncTask<>(dao::buscaTodos
                , resultado -> {
            listener.notify(resultado);
            searchProductsInAPI(listener);
        }).execute();
    }

    private void searchProductsInAPI(RepositoryListener<List<Produto>> listener) {
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
        }, listener::notify) //Notifica que o dado esta pronto
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void salva(Produto produto, RepositoryListener<Produto> listener) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                Produto produtoSalvo = response.body();
                new BaseAsyncTask<>(() -> {
                    long id = dao.salva(produtoSalvo);
                    return dao.buscaProduto(id);
                }, listener::notify)
                        .execute();
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });


    }

    public void update(Produto produto, RepositoryListener<Produto> listener) {
        Call<Produto> call = service.updateProducts(produto.getId(), produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                Produto produtoEditado = response.body();
                new BaseAsyncTask<>(() -> {
                    dao.atualiza(produtoEditado);
                    return produto;
                }, listener::notify)
                        .execute();
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });

    }

    public interface RepositoryListener<T> {
        void notify(T produtos);
    }

}
