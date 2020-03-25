package com.server.nameserver;

class NodeNotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    NodeNotFoundException(Long id) {
        super("Could not find customer " + id);
    }
}