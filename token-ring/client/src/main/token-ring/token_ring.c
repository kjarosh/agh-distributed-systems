#include "token_ring.h"

#include <string.h>
#include <errno.h>
#include <stdlib.h>

#include "utils.h"

int tr_send(const void *buf, size_t len, const tr_identifier *to) {
    if (len > INT16_MAX) {
        tr_error = "Data too long";
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

int tr_recv(void *buf, size_t len, int flags, tr_identifier *from) {
    pthread_mutex_lock(&tr_mutex);

    // wait for a packet
    while (tr_queue_empty(&trq_to_recv)) {
        if (flags & TR_DONTWAIT) {
            struct timespec now = tr_get_now();
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
    size_t copy_len = packet->data_length < len ? packet->data_length : len;
    memcpy(buf, &packet->data, copy_len);
    if (from != NULL) {
        memcpy(from, packet->sender, 256);
    }
    free(packet);

    pthread_mutex_unlock(&tr_mutex);
    return 0;
}
