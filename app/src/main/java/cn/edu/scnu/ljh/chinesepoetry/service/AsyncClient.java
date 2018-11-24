package cn.edu.scnu.ljh.chinesepoetry.service;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AsyncClient {
    private static final String BASE_URL_POETRY = "http://10.242.18.127/api/poetry";
    private static final String BASE_URL_POETRY_AUTHOR = "http://10.242.18.127/api/poetry-author";
    private static final String BASE_URL_POEM = "http://10.242.18.127/api/poem";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void getPoetry(RequestParams params, AsyncHttpResponseHandler handler, Integer order) {
        client.addHeader("order", order.toString());
        client.get(BASE_URL_POETRY, params, handler);
    }

    public static void getPoetryAuthor(RequestParams params, AsyncHttpResponseHandler handler, Integer order) {
        client.addHeader("order", order.toString());
        client.get(BASE_URL_POETRY_AUTHOR, params, handler);
    }

    public static void getPoem(RequestParams params, AsyncHttpResponseHandler handler, Integer order) {
        client.addHeader("order", order.toString());
        client.get(BASE_URL_POEM, params, handler);
    }
}
