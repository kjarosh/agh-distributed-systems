#include "init.h"

#include <stdlib.h>
#include <string.h>
#include "utils.h"
#include "main_thread.h"

int tr_init_udp() {
    if ((tr_read_sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        tr_error = "Error creating socket";
        return -1;
    }

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));

    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(tr_config.port);

    if (bind(tr_read_sock, (const struct sockaddr *) &server_addr, sizeof(server_addr)) < 0) {
        tr_error = "Error binding";
        return -1;
    }

    tr_write_sock = tr_read_sock;
    return 0;
}

int tr_init_tcp() {
    if ((tr_read_sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        tr_error = "Error creating socket";
        return -1;
    }

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));

    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(tr_config.port);

    if (bind(tr_read_sock, (const struct sockaddr *) &server_addr, sizeof(server_addr)) < 0) {
        tr_error = "Error binding";
        return -1;
    }

    if (listen(tr_read_sock, 10) != 0) {
        tr_error = "Error listening";
        return -1;
    }

    struct sockaddr_in client_addr;
    if (connect(tr_read_sock, &client_addr, sizeof(client_addr)) != 0) {
        tr_error = "Error connecting";
        return -1;
    }

    tr_write_sock = tr_read_sock;
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

    int rt;
    if ((rt = tr_init_socket()) != 0) {
        return rt;
    }

    tr_neighbor_addr.sin_family = AF_INET;
    tr_neighbor_addr.sin_port = htons(tr_config.neighbor_port);
    inet_pton(AF_INET, tr_config.neighbor_ip, &tr_neighbor_addr.sin_addr);

    if (pthread_create(&tr_token_thread, NULL, tr_token_thread_main, NULL)) {
        tr_error = "Error creating thread";
        return 1;
    }

    return 0;
}
