package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createRequest(ItemRequestDto itemRequestDto, Integer userId) {
        return post("", userId, itemRequestDto);
    }

    public ResponseEntity<Object> getRequests(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getUserRequests(long userId) {
        Map<String, Object> parameters = Map.of("from", 0, "size", 20);
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getRequestById(Integer requestId, Long userId) {
        return get("/" + requestId, userId);
    }
}