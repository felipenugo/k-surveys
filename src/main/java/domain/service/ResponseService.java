package domain.service;

import data.ResponseRepository;

public class ResponseService {
    private final ResponseRepository responseRepository;
    public ResponseService(ResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }
}
