#include "init.h"

#include <stdlib.h>
#include <string.h>
#include "utils.h"
#include "main_thread.h"

int tr_init_udp() {
    if ((tr_client_sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        tr_error = "Error creating socket";
        return -1;
    }

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));

    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(tr_config.port);

    if (bind(tr_client_sock, (const struct sockaddr *) &server_addr, sizeof(server_addr)) < 0) {
        tr_error = "Error binding";
        return -1;
    }

    tr_neighbor_sock = tr_client_sock;
    return 0;
}

int tr_init_tcp_server() {
    if ((tr_server_sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        tr_error = "Error creating socket";
        return -1;
    }

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));

    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(tr_config.port);

    if (bind(tr_server_sock, (const struct sockaddr *) &server_addr, sizeof(server_addr)) < 0) {
        tr_error = "Error binding";
        return -1;
    }

    if (listen(tr_server_sock, 10) != 0) {
        tr_error = "Error listening";
        return -1;
    }

    struct sockaddr_in client_addr;
    socklen_t client_addr_len = sizeof(client_addr);
    if ((tr_client_sock = accept(tr_server_sock, &client_addr, &client_addr_len)) == -1) {
        tr_error = "Error connecting";
        return -1;
    }

    return 0;
}

int tr_init_tcp_client() {
    if ((tr_neighbor_sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        tr_error = "Error creating socket";
        return -1;
    }

    struct sockaddr_in client_addr;
    memset(&client_addr, 0, sizeof(client_addr));
    client_addr.sin_family = AF_INET;
    client_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    client_addr.sin_port = htons(0);

    if (bind(tr_neighbor_sock, (const struct sockaddr *) &client_addr, sizeof(client_addr)) < 0) {
        tr_error = "Error binding client socket";
        return -1;
    }

    if (connect(tr_neighbor_sock, &tr_neighbor_addr, sizeof(tr_neighbor_addr)) != 0) {
        tr_error = "Error connecting to neighbor";
        return -1;
    }

    return 0;
}

int tr_init_tcp() {
    int rt;
    if (tr_has_token) {
        if ((rt = tr_init_tcp_client()) != 0) return rt;
        if ((rt = tr_init_tcp_server()) != 0) return rt;
    } else {
        if ((rt = tr_init_tcp_server()) != 0) return rt;
        if ((rt = tr_init_tcp_client()) != 0) return rt;
    }

    return 0;
}

int tr_init_socket() {
    if (tr_config.proto == TR_UDP) {
        return tr_init_udp();
    } else if (tr_config.proto == TR_TCP) {
        return tr_init_tcp();
    } else {
        tr_error = "Unknown protocol";
        return -1;
    }
}

int tr_init(const struct tr_config *conf, int has_token) {
    tr_has_token = has_token;
    if (has_token) {
        valid_tid = tr_random_tid();
    }
    tr_config = *conf;

    tr_neighbor_addr.sin_family = AF_INET;
    tr_neighbor_addr.sin_port = htons(tr_config.neighbor_port);
    inet_pton(AF_INET, tr_config.neighbor_ip, &tr_neighbor_addr.sin_addr);

    int rt;
    if ((rt = tr_init_socket()) != 0) {
        return rt;
    }

    if (pthread_create(&tr_token_thread, NULL, tr_token_thread_main, NULL)) {
        tr_error = "Error creating thread";
        return 1;
    }

    return 0;
}
