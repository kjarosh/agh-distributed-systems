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

    if (bind(tr_client_sock, (const struct sockaddr *) &server_addr,
             sizeof(server_addr)) < 0) {
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

    if (bind(tr_server_sock, (const struct sockaddr *) &server_addr,
             sizeof(server_addr)) < 0) {
        tr_error = "Error binding";
        return -1;
    }

    if (listen(tr_server_sock, 10) != 0) {
        tr_error = "Error listening";
        return -1;
    }

    struct sockaddr_in client_addr;
    socklen_t client_addr_len = sizeof(client_addr);
    if ((tr_client_sock = accept(tr_server_sock, &client_addr,
                                 &client_addr_len)) == -1) {
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

    if (connect(tr_neighbor_sock, &tr_neighbor_addr,
                sizeof(tr_neighbor_addr)) != 0) {
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

int tr_join() {
    int join_serv_sock;
    if ((join_serv_sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        tr_error = "Error creating socket (join)";
        return -1;
    }

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));

    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(tr_config.port);

    if (bind(join_serv_sock, (const struct sockaddr *) &server_addr,
             sizeof(server_addr)) < 0) {
        tr_error = "Error binding (join)";
        return -1;
    }

    struct tr_packet_switch packet;
    packet.type = TRP_SWITCH;
    packet.neighbor_ip[0] = 0;
    packet.neighbor_port = 0;

    if (sendto(join_serv_sock,
               &packet, sizeof(packet), 0,
               &tr_neighbor_addr, sizeof(tr_neighbor_addr)) < 0) {
        tr_error = "Error sending switch";
        return -1;
    }

    if (recv(join_serv_sock, &packet, sizeof(packet), 0) < 0) {
        tr_error = "Error receiving switch";
        return -1;
    }

    if (packet.type != TRP_SWITCH) {
        tr_error = "Invalid packet while switching";
        return -1;
    }

    tr_neighbor_addr.sin_port = htons(packet.neighbor_port);
    tr_neighbor_addr.sin_addr.s_addr = inet_addr(packet.neighbor_ip);

    return 0;
}

int tr_init(const struct tr_config *conf, int has_token) {
    tr_has_token = has_token;
    if (has_token) {
        valid_tid = tr_random_tid();
    }
    tr_config = *conf;
    tr_log("initializing");

    tr_neighbor_addr.sin_family = AF_INET;
    tr_neighbor_addr.sin_port = htons(tr_config.neighbor_port);
    tr_neighbor_addr.sin_addr.s_addr = inet_addr(tr_config.neighbor_ip);

    int rt;
    if (tr_config.join && (rt = tr_join()) != 0) {
        return rt;
    }

    if ((rt = tr_init_socket()) != 0) {
        return rt;
    }

    if (pthread_create(&tr_token_thread, NULL, tr_thread_main, NULL)) {
        tr_error = "Error creating thread";
        return 1;
    }

    /*if (pthread_create(&tr_aux_thread, NULL, tr_thread_aux, NULL)) {
        tr_error = "Error creating aux thread";
        return 1;
    }*/

    return 0;
}
