#include "token_ring.h"

#include <pthread.h>
#include <string.h>

#include <sys/time.h>
#include <errno.h>
#include <sys/socket.h>
#include <arpa/inet.h>

char *tr_error = NULL;

pthread_t tr_token_thread;
struct tr_config tr_config;

int tr_sock;

struct sockaddr_in *tr_neighbor_addr;

/**
 * This value is true if this machine currently owns the token,
 * false otherwise.
 */
int tr_has_token;

/**
 * Last round trip length, read from the token,
 * if no information is available this value should be 0.
 */
uint16_t last_rtl = 0;

/**
 * TTL currently in use, upon receiving the token
 * this value should be recalculated.
 */
uint16_t current_ttl = 64;

/**
 * Current valid token ID, if any token with ID less than this
 * value is encountered, it's considered phony.
 */
uint64_t valid_tid = 0;

/**
 * Queue of packets to pass to the neighbor.
 */
tr_queue_t trq_to_pass = TR_QUEUE_INIT;

/**
 * Queue of packets meant to be delivered to this machine.
 */
tr_queue_t trq_to_recv = TR_QUEUE_INIT;

pthread_mutex_t tr_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t trq_to_pass_cond = PTHREAD_COND_INITIALIZER;
pthread_cond_t trq_to_recv_cond = PTHREAD_COND_INITIALIZER;

struct timespec calc_wakeup_point();

void recv_from_neighbor();

int send_to_neighbor(struct timespec *wakeup_point);

void *tr_token_thread_main(void *arg) {
    pthread_mutex_lock(&tr_mutex);
    while (!tr_has_token) {
        recv_from_neighbor();
    }

    struct timespec wakeup_point = calc_wakeup_point();

    while (send_to_neighbor(&wakeup_point) == 0);

    pthread_mutex_unlock(&tr_mutex);
    return NULL;
}

void recv_from_neighbor() {
    char buf[INT16_MAX + sizeof(struct tr_packet_data)];
    size_t buf_len = sizeof(buf) / sizeof(buf[0]);
    ssize_t rt = recv(tr_sock, buf, buf_len, 0);
    if (rt != 0) {
        return;
    }

    uint8_t type = *(uint8_t *) &buf[0];
    switch (type) {
        case TRP_TOKEN:;
            struct tr_packet_token *packet_token = (struct tr_packet_token *) &buf[0];
            if (packet_token->tid > valid_tid) {
                valid_tid = packet_token->tid;
            } else if (packet_token->tid < valid_tid) {
                // drop packet, invalid token
                return;
            }

            tr_has_token = 1;
            return;

        case TRP_DATA:;
            struct tr_packet_data *packet_data = (struct tr_packet_data *) &buf[0];
            size_t packet_size = sizeof(struct tr_packet_data) + packet_data->data_length;
            packet_data = malloc(packet_size);
            memcpy(packet_data, buf, packet_size);

            if (strcmp(tr_config.identifier, packet_data->recipient) == 0) {
                tr_queue_put(&trq_to_recv, packet_data);
                pthread_cond_broadcast(&trq_to_recv_cond);
            } else {
                tr_queue_put(&trq_to_pass, packet_data);
            }
    }
}

int send_to_neighbor(struct timespec *wakeup_point) {
    while (tr_queue_empty(&trq_to_pass)) {
        int rt = pthread_cond_timedwait(&trq_to_pass_cond, &tr_mutex, wakeup_point);
        if (rt != 0 && errno == ETIMEDOUT) {
            return -1;
        }
    }

    struct tr_packet_data *packet = tr_queue_get(&trq_to_pass);

    size_t packet_len = sizeof(struct tr_packet_data) + packet->data_length;
    ssize_t rt = sendto(tr_sock, packet, packet_len, 0,
                        tr_neighbor_addr, sizeof(tr_neighbor_addr));
    if (rt != 0) {
        // TODO log error
    }

    free(packet);

    return 0;
}

struct timespec get_now() {
    struct timespec wakeup_point;
    struct timeval now;
    gettimeofday(&now, NULL);
    TIMEVAL_TO_TIMESPEC(&now, &wakeup_point);
    return wakeup_point;
}

struct timespec calc_wakeup_point() {
    struct timespec wakeup_point = get_now();
    wakeup_point.tv_sec += 1;
    return wakeup_point;
}

int tr_init(const struct tr_config *conf, int has_token) {
    tr_has_token = has_token;
    if (has_token) {
        valid_tid = (uint64_t) rand();
    }
    tr_config = *conf;

    tr_neighbor_addr->sin_family = AF_INET;
    tr_neighbor_addr->sin_port = htons(tr_config.neighbor_port);
    inet_pton(AF_INET, tr_config.neighbor_ip, &tr_neighbor_addr->sin_addr);

    if (pthread_create(&tr_token_thread, NULL, tr_token_thread_main, NULL)) {
        tr_error = "Error creating thread";
        return 1;
    }

    return 0;
}

int tr_send(const void *buf, size_t len, tr_identifier *to) {
    if (len > INT16_MAX) {
        tr_error = "Data length too big";
        return -1;
    }

    struct tr_packet_data *packet = malloc(sizeof(struct tr_packet_data) + len);
    packet->type = TRP_DATA;
    packet->ttl = current_ttl;
    memcpy(packet->sender, tr_config.identifier, 256);
    memcpy(packet->recipient, *to, 256);
    packet->data_length = (uint16_t) len;
    memcpy(packet->data, buf, len);

    pthread_mutex_lock(&tr_mutex);
    tr_queue_put(&trq_to_pass, packet);
    pthread_cond_broadcast(&trq_to_pass_cond);
    pthread_mutex_unlock(&tr_mutex);

    return 0;
}

int tr_recv(void *buf, size_t len, int flags) {
    pthread_mutex_lock(&tr_mutex);

    // wait for a packet
    while (tr_queue_empty(&trq_to_recv)) {
        if (flags & TR_DONTWAIT) {
            struct timespec now = get_now();
            int rt = pthread_cond_timedwait(&trq_to_recv_cond, &tr_mutex, &now);
            if (rt != 0 && errno == ETIMEDOUT) {
                pthread_mutex_unlock(&tr_mutex);
                return -1;
            }
        } else {
            pthread_cond_wait(&trq_to_recv_cond, &tr_mutex);
        }
    }

    struct tr_packet_data *packet = tr_queue_get(&trq_to_recv);
    size_t to_copy = packet->data_length < len ? packet->data_length : len;
    memcpy(buf, &packet->data, to_copy);
    free(packet);

    pthread_mutex_unlock(&tr_mutex);
    return 0;
}
