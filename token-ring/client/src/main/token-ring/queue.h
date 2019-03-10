#ifndef TOKEN_RING_QUEUES_H
#define TOKEN_RING_QUEUES_H

#define TR_QUEUE_INIT {NULL, NULL}

typedef struct tr_queue_t {
    struct tr_queue_node_t *first;
    struct tr_queue_node_t *last;
} tr_queue_t;

typedef struct tr_queue_node_t {
    struct tr_packet_data *value;
    struct tr_queue_node_t *next;
} tr_queue_node_t;

int tr_queue_empty(tr_queue_t *q);

struct tr_packet_data *tr_queue_get(tr_queue_t *q);

void tr_queue_put(tr_queue_t *q, struct tr_packet_data *packet);

#endif
