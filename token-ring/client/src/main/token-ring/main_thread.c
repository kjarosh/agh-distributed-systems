#include "main_thread.h"

#include <pthread.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <stdio.h>
#include "common.h"
#include "utils.h"

void tr_handle_switch(struct tr_packet_switch *packet_switch,
                      struct sockaddr_in from) {
    tr_log("switching");

    int addr_specified = packet_switch->neighbor_port != 0 &&
                         strlen(packet_switch->neighbor_ip) > 0;
    struct sockaddr_in tr_neighbor_addr_old = tr_neighbor_addr;
    if (addr_specified) {
        tr_neighbor_addr.sin_port = htons(packet_switch->neighbor_port);
        tr_neighbor_addr.sin_addr.s_addr =
                inet_addr(packet_switch->neighbor_ip);
    } else {
        tr_neighbor_addr = from;
    }

    if (tr_config.proto == TR_TCP) {
        // in TCP we need to reconnect

        int tr_neighbor_sock;
        if ((tr_neighbor_sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
            tr_log("error creating socket");
            return;
        }

        if (connect(tr_neighbor_sock, &tr_neighbor_addr,
                    sizeof(tr_neighbor_addr)) != 0) {
            tr_log("error connecting to neighbor");
            return;
        }
    }

    if (!addr_specified) {
        // we need to reply with our old neighbor's ip
        packet_switch->neighbor_port = ntohs(tr_neighbor_addr_old.sin_port);
        strcpy(packet_switch->neighbor_ip,
               inet_ntoa(tr_neighbor_addr_old.sin_addr));

        sendto(tr_neighbor_sock,
               &packet_switch, sizeof(packet_switch), 0,
               &tr_neighbor_addr, sizeof(tr_neighbor_addr));
        return;
    }
}

void tr_handle_reset() {
    if (tr_config.proto == TR_UDP) {
        // we do not need resetting for UDP
        return;
    }

    tr_log("resetting client connection");
    close(tr_client_sock);
    struct sockaddr_in client_addr;
    socklen_t client_addr_len = sizeof(client_addr);
    if ((tr_client_sock = accept(tr_server_sock, &client_addr,
                                 &client_addr_len)) == -1) {
        tr_log("failed reconnecting");
        return;
    }
}

void tr_handle_data(struct tr_packet_data *packet_data) {
    if (strcmp(tr_config.identifier, packet_data->recipient) == 0) {
        tr_log("received data, for me");
        tr_queue_put(&trq_to_recv, packet_data);
        pthread_cond_broadcast(&trq_to_recv_cond);
    } else if (packet_data->ttl == 0) {
        tr_log("received data, packet expired, dropping");
    } else {
        char log_buf[1024];
        sprintf(log_buf, "received data, not for me, for %s, ttl=%d",
                packet_data->recipient, packet_data->ttl);
        tr_log(log_buf);

        packet_data->ttl -= 1;
        tr_queue_put(&trq_to_pass, packet_data);
    }
}

void tr_handle_token(struct tr_packet_token *packet_token,
                     struct sockaddr_in from) {
    tr_log("received the token");

    if (packet_token->tid < valid_tid) {
        tr_log("token is invalid, dropping");
        return;
    } else if (packet_token->rtc == last_rtc) {
        tr_log("token is duplicated, dropping");
        return;
    } else if (packet_token->tid >= valid_tid) {
        valid_tid = packet_token->tid;
        if (last_rtc != 0 && packet_token->rtc != 0) {
            uint16_t rtc_diff = (uint16_t) (packet_token->rtc -
                                            last_rtc);
            current_ttl = (uint16_t) (rtc_diff + 16);
        }

        tr_has_token = 1;
        tr_logger_send("received the token");
    }

    tr_log("sending ack");
    struct tr_packet_token_ack packet;
    packet.type = TRP_TOKEN_ACK;
    sendto(tr_client_sock, &packet, sizeof(packet), 0,
           &from, sizeof(from));
}

// =============================================================================

void recv_from_client();

int send_to_neighbor(struct timespec *wakeup_point);

void pass_token_to_neighbor();

void *tr_thread_main(void *arg) {
    pthread_mutex_lock(&tr_mutex);
    while (tr_running) {
        while (!tr_has_token) {
            recv_from_client();
        }

        struct timespec wakeup_point = tr_calc_wakeup_point();

        tr_log("sending packets");
        while (send_to_neighbor(&wakeup_point) == 0);

        pass_token_to_neighbor();
    }
    pthread_mutex_unlock(&tr_mutex);
    return NULL;
}

void *tr_thread_aux(void *arg) {
    //if (tr_config.proto == TR_UDP) {
        // for UDP we do not need to create a separate thread
        return NULL;
    //}


    int aux_server_sock;
    if ((aux_server_sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        tr_log("error creating aux socket");
        return NULL;
    }

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));

    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(tr_config.port);

    if (bind(aux_server_sock, (const struct sockaddr *) &server_addr,
             sizeof(server_addr)) < 0) {
        tr_log("error binding aux socket");
        return NULL;
    }

    while (tr_running) {
        tr_log("aux: receiving a packet");

        char buf[sizeof(struct tr_packet_switch)];
        size_t buf_len = sizeof(buf) / sizeof(buf[0]);
        struct sockaddr_in from;
        socklen_t from_len = sizeof(from);
        ssize_t rt = recvfrom(tr_client_sock, buf, buf_len, 0,
                              &from, &from_len);
        if (rt < 0) {
            tr_log("aux: failed to receive");
            usleep(1000);
            continue;
        }

        uint8_t type = *(uint8_t *) &buf[0];
        if (type == TRP_SWITCH) {
            tr_handle_switch((struct tr_packet_switch *) &buf[0], from);
        }
    }

    return NULL;
}

void recv_from_client() {
    tr_log("receiving a packet");

    char buf[INT16_MAX + sizeof(struct tr_packet_data)];
    size_t buf_len = sizeof(buf) / sizeof(buf[0]);
    struct sockaddr_in from;
    socklen_t from_len = sizeof(from);
    ssize_t rt = recvfrom(tr_client_sock, buf, buf_len, 0,
                          &from, &from_len);
    if (rt < 0) {
        tr_log("failed to receive");
        usleep(1000);
        return;
    }

    uint8_t type = *(uint8_t *) &buf[0];
    switch (type) {
        case TRP_TOKEN:
            tr_handle_token((struct tr_packet_token *) &buf[0], from);
            return;

        case TRP_DATA:;
            struct tr_packet_data *packet_data = (struct tr_packet_data *) &buf[0];
            size_t packet_size =
                    sizeof(struct tr_packet_data) + packet_data->data_length;
            packet_data = malloc(packet_size);
            memcpy(packet_data, buf, packet_size);
            tr_handle_data(packet_data);
            return;

        case TRP_RESET:
            tr_handle_reset();
            return;

        case TRP_SWITCH:
            tr_handle_switch((struct tr_packet_switch *) &buf[0], from);
            return;

    }
}

int send_to_neighbor(struct timespec *wakeup_point) {
    while (tr_queue_empty(&trq_to_pass)) {
        int rt = pthread_cond_timedwait(&trq_to_pass_cond, &tr_mutex,
                                        wakeup_point);
        if (rt == ETIMEDOUT) {
            tr_log("waiting timed out");
            return -1;
        }
    }

    struct tr_packet_data *packet = tr_queue_get(&trq_to_pass);

    size_t packet_len = sizeof(struct tr_packet_data) + packet->data_length;
    ssize_t rt = sendto(tr_neighbor_sock, packet, packet_len, 0,
                        &tr_neighbor_addr, sizeof(tr_neighbor_addr));
    free(packet);

    if (rt < 0) {
        tr_log("failed to send a packet");
        return -1;
    }

    return 0;
}

void pass_token_to_neighbor() {
    char log_buf[1024];
    sprintf(log_buf, "passing the token to %s:%d",
            inet_ntoa(tr_neighbor_addr.sin_addr),
            ntohs(tr_neighbor_addr.sin_port));
    tr_log(log_buf);

    struct tr_packet_token packet;
    packet.type = TRP_TOKEN;
    packet.rtc = (uint16_t) (last_rtc + 1);
    packet.tid = valid_tid;

    int token_ackd = 0;
    while (!token_ackd) {
        size_t packet_len = sizeof(struct tr_packet_token);
        ssize_t rt = sendto(tr_neighbor_sock, &packet, packet_len, 0,
                            &tr_neighbor_addr, sizeof(tr_neighbor_addr));
        usleep(100);
        if (rt < 0) {
            tr_log("failed to pass the token");
            continue;
        }

        struct tr_packet_token_ack packet_ack;
        if (recv(tr_neighbor_sock, &packet_ack, sizeof(packet_ack),
                 MSG_DONTWAIT) > 0 &&
            packet_ack.type == TRP_TOKEN_ACK) {
            token_ackd = 1;
        } else {
            usleep(500);
        }
    }

    tr_log("token passed");
    tr_has_token = 0;
}
